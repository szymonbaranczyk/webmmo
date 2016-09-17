package szymonbaranczyk.enterFlow

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.scalalogging.LazyLogging
import szymonbaranczyk.dataLayer.GameActor

/**
  * Created by Szymon Barańczyk.
  */

case class CreateGame(id: Int)

case class CreatedGame(id: Int)

case class PlayerInput(move: String, shot: Boolean)

case class PlayerInputEnvelope(playerInput: PlayerInput, gameId: Int, playerId: String)

class GameRouter extends Actor with LazyLogging {
  var games = Map.empty[Int, ActorRef]

  override def receive = {
    case CreateGame(id) => games += (id -> context.actorOf(Props[GameActor]))
      sender ! CreatedGame(id)
    case PlayerInputEnvelope(playerInput, gameId, playerId) => games.get(gameId) match {
      case Some(a) => a ! PlayerInputEnvelope(playerInput, gameId, playerId)
      case None => logger.error("Game does not exist!")
    }
  }
}
