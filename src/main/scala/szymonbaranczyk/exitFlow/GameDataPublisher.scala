package szymonbaranczyk.exitFlow

import akka.actor.Props
import akka.event.EventBus
import akka.stream.actor.ActorPublisher

import scala.concurrent.ExecutionContext

/**
  * Created by Szymon Barańczyk.
  */
class GameDataPublisher(val eventBus: EventBus) extends ActorPublisher[GameData] {

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
  def props(implicit ctx: ExecutionContext, eventBus: EventBus): Props = Props(new GameDataPublisher(eventBus))
}

case class GameData(playersData: Seq[PlayerData])

case class PlayerData(x: Int, y: Int, rotation: Int, turretRotation: Int, id: String)