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
object Server {

  def main(args: Array[String]) {
    val closingHandlers = startServer()
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    stopServer(closingHandlers._1, closingHandlers._2)
  }

  def startServer() = {
    implicit val system = ActorSystem("system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val greeterWebSocketService =
      Flow[Message]
        .mapConcat {
          case tm: TextMessage => TextMessage(Source.single("Hello ") ++ tm.textStream) :: Nil
          case bm: BinaryMessage =>
            // ignore binary messages but drain content to avoid the stream being clogged
            bm.dataStream.runWith(Sink.ignore)
            Nil
        }

    val route =
      pathEndOrSingleSlash {
        get {
          getFromResource("web/index.html")
        }
      } ~ {
        getFromResourceDirectory("web")
      } ~ path("greeter") {
        get {
          handleWebSocketMessages(greeterWebSocketService)
        }
      }
    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    (bindingFuture, system)
  }

  def stopServer(future: Future[ServerBinding], system: ActorSystem): Unit = {
    implicit val executionContext = system.dispatcher
    future
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
