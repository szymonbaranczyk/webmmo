package szymonbaranczyk.enterFlow

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import szymonbaranczyk.dataLayer.{CalculateGameState, GameActor}
import szymonbaranczyk.exitFlow.GameDataBus

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random
/**
  * Created by Szymon Barańczyk.
  */


class GameManager(prefferedNumberOfPlayers: Int, eventBus: GameDataBus) extends Actor with LazyLogging {
  import context._
  var games = Map.empty[Int, ActorRef]
  context.system.scheduler.schedule(0.1 seconds, 0.1 seconds, self, Calculate())
  context.system.scheduler.schedule(0 seconds, 10 seconds, self, BalanceGames())
  var gameId = 0

  def logUsedMemory() = {
    val mb = 1024 * 1024

    //Getting the runtime reference from system
    val runtime = Runtime.getRuntime

    logger.debug("##### Heap utilization statistics [MB] #####")

    //Print used memory
    logger.debug("Used Memory:"
      + (runtime.totalMemory() - runtime.freeMemory()) / mb)

    //Print free memory
    logger.debug("Free Memory:"
      + runtime.freeMemory() / mb)

    //Print total available memory
    logger.debug("Total Memory:" + runtime.totalMemory() / mb)
    //Print Maximum available memory
    logger.debug("Max Memory:" + runtime.maxMemory() / mb)
  }

  override def receive = {
    case CreateGame(id, gameDataBus) =>
      val ref = context.actorOf(Props(new GameActor(gameDataBus, id)))
      games += (id -> ref)
      sender ! CreatedGame(id, ref)
    case PutPlayerInRandomGame(playerId) => val random = Random.nextInt(games.size)
      games.get(games.keys.toList(random)) match {
        case Some(a) => a ! CreatePlayer(games.keys.toList(random), sender(), playerId)
        case None => logger.error("Game does not exist!")
      }
    case PlayerInRandomGameWithAsker(player, asker) => asker ! player
    case Calculate() => for ((key, ref) <- games) {
      ref ! CalculateGameState()
    }
    case BalanceGames() =>
      implicit val timeout = Timeout(1 second)

      val playersCount: Seq[Int] = games.map(pair => Await.result((pair._2 ? GetGameInfo()).mapTo[GameInfo], 1 second).players).toSeq
      if (games.nonEmpty) {
        val average = playersCount.sum / games.size
        if (average > prefferedNumberOfPlayers) {
          self ! CreateGame(gameId, eventBus)
          gameId = gameId + 1
        }
      } else {
        self ! CreateGame(gameId, eventBus)
        gameId = gameId + 1
      }
      logUsedMemory()
  }


}

case class BalanceGames()

case class CreateGame(id: Int, gameDataBus: GameDataBus)

case class CreatedGame(id: Int, ref: ActorRef)

case class PutPlayerInRandomGame(playerId: String)

case class PlayerInRandomGame(gameId: Int, player: ActorRef)

case class PlayerInRandomGameWithAsker(playerInRandomGame: PlayerInRandomGame, asker: ActorRef)

case class CreatePlayer(gameId: Int, asker: ActorRef, playerId: String)

case class Game(id: Int, ref: ActorRef)

case class PlayerInput(acceleration: Int, rotation: Int, turretRotation: Int, shot: Boolean)

case class Calculate()

case class GetGameInfo()

case class GameInfo(players: Int)
