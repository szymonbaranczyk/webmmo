package szymonbaranczyk.exitFlow

import akka.actor.Props
import akka.stream.actor.ActorPublisher

import scala.concurrent.ExecutionContext

/**
  * Created by Szymon Barańczyk.
  */
class GameDataPublisher(val gameDataBus: GameDataBus, val gameId: Int) extends ActorPublisher[GameData] {

  override def preStart = {
    gameDataBus.subscribe(self, gameId)
  }

  override def receive: Receive = {

    case msg: GameData =>
      if (isActive && totalDemand > 0) {
        // Pushes the message onto the stream
        onNext(msg)
      }
  }
}

object GameDataPublisher {
  def props(implicit ctx: ExecutionContext, gameDataBus: GameDataBus, gameId: Int): Props = Props(new GameDataPublisher(gameDataBus, gameId))
}

case class GameData(playersData: Seq[PlayerData])

case class PlayerData(x: Int, y: Int, rotation: Int, turretRotation: Int, id: String)