package szymonbaranczyk.enterFlow

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.scalalogging.LazyLogging
import szymonbaranczyk.dataLayer.{CalculateGameState, GameActor}
import szymonbaranczyk.exitFlow.GameDataBus

import scala.util.Random
import scala.concurrent.duration._
/**
  * Created by Szymon Barańczyk.
  */

case class CreateGame(id: Int, gameDataBus: GameDataBus)

case class CreatedGame(id: Int, ref: ActorRef)

case class PutPlayerInRandomGame(playerId: String)

case class PlayerInRandomGame(gameId: Int, player: ActorRef)

case class PlayerInRandomGameWithAsker(playerInRandomGame: PlayerInRandomGame, asker: ActorRef)

case class RelayPlayer(gameId: Int, asker: ActorRef, playerId: String)

case class Game(id: Int, ref: ActorRef)
case class PlayerInput(acceleration: Int, rotation:Int, turretRotation:Int, shot: Boolean)
case class Calculate()

class GameManager extends Actor with LazyLogging {
  import context._
  var games = Map.empty[Int, ActorRef]
  context.system.scheduler.schedule(3 seconds, 3 seconds, self, Calculate())
  override def receive = {
    case CreateGame(id, gameDataBus) =>
      val ref = context.actorOf(Props(new GameActor(gameDataBus, id)))
      games += (id -> ref)
      sender ! CreatedGame(id, ref)
    case PutPlayerInRandomGame(playerId) => val random = Random.nextInt(games.size)
      games.get(games.keys.toList(random)) match {
        case Some(a) => a ! RelayPlayer(games.keys.toList(random), sender(), playerId)
        case None => logger.error("Game does not exist!")
      }
    case PlayerInRandomGameWithAsker(player, asker) => asker ! player
    case Calculate() => for ((key, ref) <- games) {
      ref ! CalculateGameState()
    }
  }

}


