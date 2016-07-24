package szymonbaranczyk.boot

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future
import scala.io.StdIn

/**
  * Created by Szymon on 17/07/2016.
  */
//should all these things be defined seperately? Is it only way to expose them for testing?
case class ClosingHandle(future: Future[ServerBinding], system: ActorSystem)
object Server {
  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  def main(args: Array[String]) {
    val handle = startServer()
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    stopServer(handle)
  }

  def startServer() = {


    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)


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
        handleWebSocketMessages(greeterWebSocketService)
      }
    }

  def greeterWebSocketService =
    Flow[Message]
      .mapConcat {
        case tm: TextMessage => TextMessage(Source.single("Hello ") ++ tm.textStream) :: Nil
        case bm: BinaryMessage =>
          // ignore binary messages but drain content to avoid the stream being clogged
          bm.dataStream.runWith(Sink.ignore)
          Nil
      }

  def stopServer(handle: ClosingHandle): Unit = {
    implicit val executionContext = handle.system.dispatcher
    handle.future
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => handle.system.terminate()) // and shutdown when done
  }
}
