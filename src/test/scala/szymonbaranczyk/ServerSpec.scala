package szymonbaranczyk

/**
  * Created by User1 on 20/07/2016.
  */

import org.scalatest._
import szymonbaranczyk.boot.Server

class ServerSpec extends FlatSpec {

  "A Server" should "start and stop without failure" in {
    val stopHandlers = Server.startServer()
    Server.stopServer(stopHandlers._1, stopHandlers._2)
  }
}
