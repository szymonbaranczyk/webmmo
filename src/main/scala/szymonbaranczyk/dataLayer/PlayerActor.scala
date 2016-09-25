package szymonbaranczyk.dataLayer

import akka.actor.Actor
import akka.http.scaladsl.model.ws.TextMessage
import play.api.libs.json.Json
import szymonbaranczyk.enterFlow.PlayerInput
import szymonbaranczyk.exitFlow.PlayerData
import szymonbaranczyk.helper.InputJsonParser

import scala.collection.immutable.Queue
import scala.util.Random

/**
  * Created by Szymon Barańczyk.
  */
case class CalculateState()

case class PlayerData()

class PlayerActor(id: String) extends Actor with InputJsonParser {
  val size = 4000
  var queue = Queue.empty[PlayerInput]
  var data = PlayerData(Random.nextInt(size), Random.nextInt(size), 0, 0, id)
  override def receive: Receive = {
    case tm: TextMessage.Strict =>
      queue = queue :+ Json.parse(tm.text).as[PlayerInput]
    case _: CalculateState => data
  }
}
