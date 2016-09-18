package szymonbaranczyk.boot

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Broadcast, Flow, Sink, Source}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json._
import szymonbaranczyk.enterFlow._
import szymonbaranczyk.exitFlow._
import szymonbaranczyk.helper.OutputJsonParser

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.StdIn

/**
  * Created by Szymon Barańczyk.
  */
//should all these things be defined separately? Is it only way to expose them for testing?
case class ClosingHandle(future: Future[ServerBinding], system: ActorSystem)

object Server extends LazyLogging with OutputJsonParser {
  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val conf = ConfigFactory.load()
  val port = conf.getInt("server.port")

  val eventBus = new GameDataBus()

  val gameManager = system.actorOf(Props[GameManager])
  //TODO delete mock game
  gameManager ! CreateGame(1)

  val loggingSink = Sink.foreach((x: Message) => x match {
    case tm: TextMessage.Strict =>
      logger.info("you got mail!")
    case tm: TextMessage.Streamed =>
      logger.error("There is TextMessage streamed!")
    case bm: BinaryMessage =>
      logger.error("There is BinaryMessage sent or streamed!")
  }
  )


  def main(args: Array[String]) {
    val handle = startServer()
    println(s"Server online at http://localhost:$port/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    stopServer(handle)
  }

  def startServer() = {
    val bindingFuture = Http().bindAndHandle(route, "localhost", port)
    ClosingHandle(bindingFuture, system)
  }

  def route =
    pathEndOrSingleSlash {
      get {
        getFromResource("web/html/index.html")
      }
    } ~ pathPrefix("resources") {
      getFromResourceDirectory("web")
    } ~ path("greeter") {
      get {
        handleWebSocketMessages(constructWebSocketService())
      }
    }

  //TODO delete mock gameData
  system.scheduler.schedule(10 seconds, 10 seconds) {
    eventBus.publish(
      GameDataEnvelope(
        GameData(Seq(PlayerData(1, 2, 3, 4, "player1")))
        , 1
      )
    )
  }

  def constructWebSocketService() = {
    implicit val timeout = Timeout(1 second)
    val game: Game = Await.result((gameManager ? PutPlayerInRandomGame()).mapTo[Game], 1 second)

    val dataSource = Source.actorPublisher[GameData](GameDataPublisher.props(executionContext, eventBus, game.id))

    val sink = Sink.combine(Sink.foreach(println), loggingSink)(Broadcast[Message](_))

    Flow.fromSinkAndSource(sink,
      dataSource map { d => TextMessage.Strict(Json.prettyPrint(Json.toJson(d))) })
  }

  def stopServer(handle: ClosingHandle): Unit = {
    implicit val executionContext = handle.system.dispatcher
    logger.info("shuting down")
    handle.future
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => handle.system.terminate()) // and shutdown when done
  }

}
