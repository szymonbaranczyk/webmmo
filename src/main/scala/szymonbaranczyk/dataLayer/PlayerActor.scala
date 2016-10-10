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
case class GetData()

class PlayerActor(id: String) extends Actor with InputJsonParser {
  val size = 4000
  val speed = 5
  val speedReverse = -3
  val rotationSpeed = 2
  val turretRotationSpeed = 2
  var queue = Queue.empty[PlayerData]
  var data = PlayerData(Random.nextInt(size), Random.nextInt(size), 0, 0, id)

  override def receive: Receive = {
    case tm: TextMessage.Strict =>
      val playerInput = Json.parse(tm.text).as[PlayerInput]
      data = move(data,playerInput)
      queue = queue :+ data
    case GetData() => queue.dequeueOption match {
      case Some((popped, newQueue)) =>
        queue = newQueue
        sender ! popped
      case None => sender() ! data
    }
  }

  def move(playerData: PlayerData, playerInput: PlayerInput) = {
    val moveDir = playerInput.acceleration match {
      case 1 => speed
      case -1 => speedReverse
      case 0 => 0
    }
    val rotationDir = playerInput.acceleration match {
      case 1 => rotationSpeed
      case -1 => -rotationSpeed
      case 0 => 0
    }
    val turretDir = playerInput.acceleration match {
      case 1 => turretRotationSpeed
      case -1 => -turretRotationSpeed
      case 0 => 0
    }
    PlayerData(
      x = (Math.sin(Math.toRadians(playerData.rotation)) * moveDir + playerData.x).toInt,
      y = (Math.cos(Math.toRadians(playerData.rotation)) * moveDir + playerData.y).toInt,
      rotation = playerData.rotation + rotationDir,
      turretRotation = playerData.rotation + turretDir,
      id
    )
  }
}
