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
  var bullets = Map.empty[Int, ActorRef]
  var gameData = GameData(Seq(),Seq())
  var bulletId = 0
  override def receive = ExpectingCalculationRequest

  def ExpectingCalculationRequest: Receive = {
    case CalculateGameState() =>
      if(players.nonEmpty) {
        context.become(CalculatingState)
        context.system.scheduler.scheduleOnce(0.1 second, self, Timeout())
        for ((key, ref) <- players) {
          ref ! GetData()
        }
        for ((key, ref) <- bullets) {
          ref ! CalculateStep()
          ref ! CheckCollisions(players)
        }
      }
    case _:PlayerData | BulletState => logger.debug("received late PlayerData")
    case a => defaultReceive(a)
  }

  def CalculatingState: Receive = {
    case playerData: PlayerData =>
      gameData = GameData(gameData.playersData :+ playerData, gameData.bulletData)
      if (gameData.playersData.length == players.size && gameData.bulletData.length == bullets.size) {
        context.become(ExpectingCalculationRequest)
        gameDataBus.publish(GameDataEnvelope(gameData,id))
        gameData=GameData(Seq(), Seq())
      }
    case bulletState: BulletState =>
      gameData = GameData(gameData.playersData, gameData.bulletData:+bulletState)
      if (gameData.playersData.length == players.size && gameData.bulletData.length == bullets.size) {
        context.become(ExpectingCalculationRequest)
        gameDataBus.publish(GameDataEnvelope(gameData,id))
        gameData=GameData(Seq(), Seq())
      }
    case Timeout() =>
      context.become(ExpectingCalculationRequest)
      gameDataBus.publish(GameDataEnvelope(gameData,id))
      gameData=GameData(Seq(),Seq())
      logger.debug("timeouted")
    case a => defaultReceive(a)
  }

  def defaultReceive: Receive = {
    case CreatePlayer(gameId, asker, playerId) =>
      val ref = context.actorOf(Props(new PlayerActor(playerId)))
      players += (playerId -> ref)
      sender ! PlayerInRandomGameWithAsker(PlayerInRandomGame(gameId, ref), asker)
    case DeletePlayer(playerId) => players -= playerId
    case DeleteBullet(bId) => bullets -= bId
    case Timeout() => //logger.debug(s"Game $id - GameState calculated before Timeout")
    case CalculateGameState() => logger.debug("can't calculate state now")
    case Collide(pId) => players.get(pId) match {
      case Some(ref) => ref ! Hit()
      case None =>
    }
  }
  def createBullet(x:Int,y:Int,xSpeed:Int,ySpeed:Int) = {

  }
}

case class CalculateGameState()
case class Timeout()
case class DeletePlayer(id:String)

case class Hit()

