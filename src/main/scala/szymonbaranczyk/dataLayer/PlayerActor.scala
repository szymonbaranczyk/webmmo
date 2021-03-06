package szymonbaranczyk.dataLayer

import akka.actor.{Actor, ActorRef, PoisonPill}
import akka.http.scaladsl.model.ws.TextMessage
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.Json
import szymonbaranczyk.enterFlow.PlayerInput
import szymonbaranczyk.exitFlow.{CloseConnection, PlayerData}
import szymonbaranczyk.helper.InputJsonParser

import scala.collection.immutable.Queue
import scala.concurrent.duration._
import scala.math._
import scala.util.Random

/**
  * Created by Szymon Barańczyk.
  */


class PlayerActor(id: String) extends Actor with InputJsonParser with LazyLogging {

  import context._

  val size = 4000
  val speed = 10
  val speedReverse = -6
  val rotationSpeed = 4
  val turretRotationSpeed = 4
  val turretLength = 52
  val bulletSpeed = 20
  var queue = Queue.empty[PlayerData]
  var lastDequeued: Option[PlayerData] = None
  var data = PlayerData(Random.nextInt(size), Random.nextInt(size), 0, 0, id, "")
  var closeHandle: Option[ActorRef] = None
  var cancellable = system.scheduler.scheduleOnce(5 seconds, self, ReceiveTimeout())
  var lives = 3
  var nextMeta = lives.toString
  var canShootCounter = 0

  override def receive: Receive = {
    case tm: TextMessage.Strict =>
      val playerInput = Json.parse(tm.text).as[PlayerInput]
      data = move(data, playerInput)
      queue = queue :+ data
      if (playerInput.shot) lastDequeued match {
        case Some(d) =>
          if (canShootCounter == 0) {
            parent ! CreateBullet(
              (sin(toRadians(d.turretRotation)) * turretLength + d.x).toInt,
              (cos(toRadians(d.turretRotation)) * turretLength + d.y).toInt,
              (sin(toRadians(d.turretRotation)) * bulletSpeed).toInt,
              (cos(toRadians(d.turretRotation)) * bulletSpeed).toInt,
              id
            )
            canShootCounter = 5
          }
        case None =>
      }

      cancellable.cancel()
      cancellable = system.scheduler.scheduleOnce(5 seconds, self, ReceiveTimeout())
    case GetData() => {
      if (canShootCounter > 0) canShootCounter = canShootCounter - 1
      queue.dequeueOption match {
        case Some((popped, newQueue)) =>
          queue = newQueue
          sender ! popped
          lastDequeued = Some(popped)
        case None => sender() ! data
      }
    }
    case Hit() =>
      lives = lives - 1
    case GetDataWithoutCalc() =>
      lastDequeued match {
        case Some(p) => sender() ! p
        case None => sender() ! data
      }
    case CloseHandle(ref) =>
      logger.debug(s"  ${self.path.toSerializationFormat} received handle")
      closeHandle = Some(ref)
    case ReceiveTimeout() => self ! PoisonPill
  }

  def reset() = {
    logger.debug("reset")
    queue = Queue.empty[PlayerData] :+ data
    lives = 3
  }

  def boundCheck(x: Int) = x match {
    case i if i < 0 => 0
    case i if i > size => size
    case i => i
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

    val ret = if (lives > 0) PlayerData(
      x = boundCheck((Math.sin(Math.toRadians(playerData.rotation)) * moveDir + playerData.x).toInt),
      y = boundCheck((Math.cos(Math.toRadians(playerData.rotation)) * moveDir + playerData.y).toInt),
      rotation = playerData.rotation + rotationDir,
      turretRotation = playerData.turretRotation + turretDir + rotationDir,
      id,
      nextMeta)
    else
      PlayerData(Random.nextInt(size), Random.nextInt(size), 0, 0, id, "")
    if (lives <= 0) reset()
    nextMeta = lives.toString
    ret
  }

  override def postStop() = {
    closeHandle match {
      case Some(ref) => ref ! CloseConnection()
        logger.debug("closing publisher")
        parent ! DeletePlayer(id)
      case None => logger.error(s"${self.path.toSerializationFormat} can't close GameDataPublisher")
    }
  }
}

case class CloseHandle(ref: ActorRef)

case class ReceiveTimeout()

case class GetData()

case class GetDataWithoutCalc()