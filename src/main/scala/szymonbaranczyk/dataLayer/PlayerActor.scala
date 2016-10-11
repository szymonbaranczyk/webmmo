package szymonbaranczyk.dataLayer

import akka.actor.{Actor, ActorRef, ReceiveTimeout}
import akka.http.scaladsl.model.ws.TextMessage
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.Json
import szymonbaranczyk.enterFlow.PlayerInput
import szymonbaranczyk.exitFlow.{CloseConnection, PlayerData}
import szymonbaranczyk.helper.InputJsonParser

import scala.collection.immutable.Queue
import scala.util.Random
import scala.concurrent.duration._
/**
  * Created by Szymon Barańczyk.
  */
case class GetData()

class PlayerActor(id: String) extends Actor with InputJsonParser with LazyLogging {
  val size = 4000
  val speed = 2
  val speedReverse = -1
  val rotationSpeed = 1
  val turretRotationSpeed = 2
  var queue = Queue.empty[PlayerData]
  var data = PlayerData(Random.nextInt(size), Random.nextInt(size), 0, 0, id)
  var closeHandle: Option[ActorRef] = None
  context.setReceiveTimeout(5 seconds)
  override def receive: Receive = {
    case tm: TextMessage.Strict =>
      val playerInput = Json.parse(tm.text).as[PlayerInput]
      data = move(data,playerInput)
      queue = queue :+ data
      context.setReceiveTimeout(5 seconds)
    case GetData() => queue.dequeueOption match {
      case Some((popped, newQueue)) =>
        queue = newQueue
        sender ! popped
      case None => sender() ! data
    }
    case CloseHandle(ref) =>
      logger.debug("received handle")
      closeHandle = Some(ref)
    case ReceiveTimeout => closeHandle match {
      case Some(ref) => ref ! CloseConnection()
        logger.debug("closing publisher")
      case None => logger.error("can't close GameDataPublisher")
    }
  }

  def move(playerData: PlayerData, playerInput: PlayerInput) = {
    val moveDir = playerInput.acceleration match {
      case 1 => speed
      case -1 => speedReverse
      case 0 => 0
    }
    val rotationDir = playerInput.rotation match {
      case 1 => rotationSpeed
      case -1 => -rotationSpeed
      case 0 => 0
    }
    val turretDir = playerInput.turretRotation match {
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

case class CloseHandle(ref:ActorRef)
