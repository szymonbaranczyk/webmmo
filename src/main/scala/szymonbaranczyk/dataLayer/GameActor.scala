package szymonbaranczyk.dataLayer

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.scalalogging.LazyLogging
import szymonbaranczyk.enterFlow.{PlayerInRandomGame, PlayerInRandomGameWithAsker, CreatePlayer}
import szymonbaranczyk.exitFlow.{GameData, GameDataBus, GameDataEnvelope, PlayerData}
import szymonbaranczyk.helper.OutputJsonParser

import scala.concurrent.duration._
/**
  * Created by Szymon Barańczyk.
  */



class GameActor(gameDataBus: GameDataBus, id: Int) extends Actor with OutputJsonParser with LazyLogging {
  import context._
  implicit val exContext = context
  var players = Map.empty[String, ActorRef]
  var gameData = GameData(Seq())
  override def receive = ExpectingCalculationRequest

  def ExpectingCalculationRequest: Receive = {
    case CalculateGameState() =>
      if(players.nonEmpty) {
        context.become(CalculatingState)
        context.system.scheduler.scheduleOnce(0.1 second, self, Timeout())
        for ((key, ref) <- players) {
          ref ! GetData()
        }
      }
    case data:PlayerData => logger.debug("received late PlayerData")
    case a => defaultReceive(a)
  }

  def CalculatingState: Receive = {
    case data: PlayerData =>
      gameData = GameData(gameData.playersData :+ data)
      if (gameData.playersData.length == players.size) {
        context.become(ExpectingCalculationRequest)
        gameDataBus.publish(GameDataEnvelope(gameData,id))
        gameData=GameData(Seq())
      }
    case Timeout() =>
      context.become(ExpectingCalculationRequest)
      gameDataBus.publish(GameDataEnvelope(gameData,id))
      gameData=GameData(Seq())
    case a => defaultReceive(a)
  }

  def defaultReceive: Receive = {
    case CreatePlayer(gameId, asker, playerId) =>
      val ref = context.actorOf(Props(new PlayerActor(playerId)))
      players += (playerId -> ref)
      sender ! PlayerInRandomGameWithAsker(PlayerInRandomGame(gameId, ref), asker)
    case DeletePlayer(playerId) => players -= playerId
    case Timeout() => //logger.debug(s"Game $id - GameState calculated before Timeout")
  }

}

case class CalculateGameState()
case class Timeout()
case class DeletePlayer(id:String)

