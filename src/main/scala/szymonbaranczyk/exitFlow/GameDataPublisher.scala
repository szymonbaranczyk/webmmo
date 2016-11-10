package szymonbaranczyk.exitFlow

import akka.actor.{ActorRef, Props}
import akka.stream.actor.ActorPublisher
import com.typesafe.scalalogging.LazyLogging
import szymonbaranczyk.dataLayer.{BulletState, CloseHandle}

import scala.concurrent.ExecutionContext

/**
  * Created by Szymon Barańczyk.
  */
case class CloseConnection()
class GameDataPublisher(val gameDataBus: GameDataBus, val gameId: Int, playerActor: ActorRef) extends ActorPublisher[GameData] with LazyLogging {

  override def preStart = {
    gameDataBus.subscribe(self, gameId)
    playerActor ! CloseHandle(self)
  }

  override def receive: Receive = {

    case msg: GameData =>
      if (isActive && totalDemand > 0) {
        onNext(msg)
      }
    case CloseConnection() =>
      gameDataBus.unsubscribe(self, gameId)
      onCompleteThenStop()
  }
}

object GameDataPublisher {
  def props(implicit ctx: ExecutionContext, gameDataBus: GameDataBus, gameId: Int, playerActor:ActorRef): Props = Props(new GameDataPublisher(gameDataBus, gameId,playerActor))
}

case class GameData(playersData: Seq[PlayerData], bulletData: Seq[BulletState])

case class PlayerData(x: Int,y: Int,rotation: Int,turretRotation: Int,id: String, meta:String)
