package szymonbaranczyk.dataLayer

import akka.actor.{Actor, ActorRef, Props}
import szymonbaranczyk.enterFlow.{PlayerInRandomGame, PlayerInRandomGameWithAsker, RelayPlayer}
import szymonbaranczyk.exitFlow.{GameData, GameDataBus, PlayerData}
import szymonbaranczyk.helper.OutputJsonParser

/**
  * Created by Szymon Barańczyk.
  */

case class CalculateGameState()

case class Timeout()

class GameActor(gameDataBus: GameDataBus) extends Actor with OutputJsonParser {
  var players = Map.empty[String, ActorRef]
  var gameData = GameData(Seq())

  override def receive = ExpectingCalculationRequest

  def ExpectingCalculationRequest: Receive = {
    case CalculateGameState() =>
      context.become(CalculatingState)
      //TODO set send Timeout() message after some time
      for ((key, ref) <- players) {
        ref ! GetData()
      }
    case a => defaultReceive(a)
  }

  def CalculatingState: Receive = {
    case data: PlayerData =>
      gameData = GameData(gameData.playersData :+ data)
      if (gameData.playersData.length == players.size) {
        context.become(ExpectingCalculationRequest)
        //TODO send gameData
      }
    case Timeout() =>
      context.become(ExpectingCalculationRequest)
    //TODO send gameData
    case a => defaultReceive(a)
  }

  def defaultReceive: Receive = {
    case RelayPlayer(gameId, asker, playerId) =>
      val ref = context.actorOf(Props(new PlayerActor(playerId)))
      players += ("playerId" -> ref)
      sender ! PlayerInRandomGameWithAsker(PlayerInRandomGame(gameId, ref), asker)
  }
}

