package szymonbaranczyk

/**
  * Created by Szymon Barańczyk.
  */

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import org.scalatest.{FlatSpec, Matchers, _}
import play.api.libs.json.Json
import szymonbaranczyk.boot.{ClosingHandle, Server}
import szymonbaranczyk.enterFlow.PlayerInput
import szymonbaranczyk.helper.InputJsonParser

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
    WS("/greeter/lol", wsClient.flow) ~> Server.route ~>
      check {
        // check response for WS Upgrade headers
        isWebSocketUpgrade shouldEqual true
        Thread.sleep(10000)
        wsClient.expectCompletion()
      }
  }

  "InputJsonParser" should "parse correctly" in {
    class TestClass extends InputJsonParser {
      def parseJSON(string:String) = Json.parse(string).as[PlayerInput]
    }
    val test = new TestClass().parseJSON("{\n\"acceleration\": 1,\n\"rotation\": 1,\n\"turretRotation\": 1,\n\"shot\": false\n}")
  }

  override def beforeAll: Unit = {
    stopHandlers = Some(Server.startServer())
  }

  override def afterAll: Unit = {
    Server.stopServer(stopHandlers.getOrElse(fail()))
  }


}
