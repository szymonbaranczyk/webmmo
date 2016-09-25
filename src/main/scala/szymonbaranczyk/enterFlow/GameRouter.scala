package szymonbaranczyk.enterFlow

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.scalalogging.LazyLogging
import szymonbaranczyk.dataLayer.GameActor

import scala.util.Random

/**
  * Created by Szymon Barańczyk.
  */

case class CreateGame(id: Int)

case class CreatedGame(id: Int, ref: ActorRef)

case class PutPlayerInRandomGame()

case class PlayerInRandomGame(gameId: Int, player: ActorRef)

case class PlayerInRandomGameWithAsker(playerInRandomGame: PlayerInRandomGame, asker: ActorRef)

case class RelayPlayer(gameId: Int, asker: ActorRef)

case class Game(id: Int, ref: ActorRef)
case class PlayerInput(move: String, shot: Boolean)

class GameManager extends Actor with LazyLogging {
  var games = Map.empty[Int, ActorRef]

  override def receive = {
    case CreateGame(id) =>
      val ref = context.actorOf(Props[GameActor])
      games += (id -> ref)
      sender ! CreatedGame(id, ref)
    case PutPlayerInRandomGame() => val random = Random.nextInt(games.size)
      games.get(games.keys.toList(random)) match {
        case Some(a) => a ! RelayPlayer(games.keys.toList(random), sender())
        case None => logger.error("Game does not exist!")
      }
    case PlayerInRandomGameWithAsker(player, asker) => asker ! player

  }

}
