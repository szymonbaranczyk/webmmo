package szymonbaranczyk

/**
  * Created by User1 on 20/07/2016.
  */

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.ws.BinaryMessage
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import akka.util.ByteString
import org.scalatest.{FlatSpec, Matchers, _}
import szymonbaranczyk.boot.{ClosingHandle, Server}

import scala.concurrent.duration.DurationInt

class ServerSpec extends FlatSpec with BeforeAndAfterAll with ScalatestRouteTest with Matchers {
  var stopHandlers: Option[ClosingHandle] = None

  "Server" should "serve the index page on /" in {
    Get("/") ~> Server.route ~> check {
      status shouldBe OK
    }
  }

  "it" should "serve the index page on resources/html/index.html" in {
    Get("/resources/html/index.html") ~> Server.route ~> check {
      status shouldBe OK
    }
  }

  "it" should "expose Websocket" in {
    val wsClient = WSProbe()

    // WS creates a WebSocket request for testing
    WS("/greeter", wsClient.flow) ~> Server.route ~>
      check {
        // check response for WS Upgrade headers
        isWebSocketUpgrade shouldEqual true

        // manually run a WS conversation
        wsClient.sendMessage("Peter")
        wsClient.expectMessage("Hello Peter")

        wsClient.sendMessage(BinaryMessage(ByteString("abcdef")))
        wsClient.expectNoMessage(100.millis)

        wsClient.sendMessage("John")
        wsClient.expectMessage("Hello John")

        wsClient.sendCompletion()
        wsClient.expectCompletion()
      }
  }

  override def beforeAll: Unit = {
    stopHandlers = Some(Server.startServer())
  }

  override def afterAll: Unit = {
    Server.stopServer(stopHandlers.getOrElse(fail()))
  }


}
