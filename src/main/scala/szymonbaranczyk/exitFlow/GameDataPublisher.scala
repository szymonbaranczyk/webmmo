package szymonbaranczyk.exitFlow

import akka.actor.Props
import akka.stream.actor.ActorPublisher

import scala.concurrent.ExecutionContext

/**
  * Created by Szymon Barańczyk.
  */
class GameDataPublisher extends ActorPublisher[GameData] {

  override def preStart = {
    context.system.eventStream.subscribe(self, classOf[GameData])
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
  def props(implicit ctx: ExecutionContext): Props = Props(new GameDataPublisher())
}

case class GameData(playersData: Seq[PlayerData], gameId: Int)

case class PlayerData(x: Int, y: Int, rotation: Int, turretRotation: Int)