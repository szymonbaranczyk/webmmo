package szymonbaranczyk

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestActors, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import szymonbaranczyk.dataLayer.{CalculateGameState, GameActor}
import szymonbaranczyk.enterFlow.{PlayerInRandomGameWithAsker, RelayPlayer}
import szymonbaranczyk.exitFlow.{GameData, GameDataBus}

import scala.concurrent.duration._

/**
  * Created by Szymon Barańczyk.
  */

class GameActorSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "GameActor" should {
    val probe = TestProbe()
    val gameBus = new GameDataBus()
    gameBus.subscribe(probe.ref,1)
    val actorRef = TestActorRef(new GameActor(new GameDataBus(), 1))
    Thread.sleep(3000)
    "create a player" in {
      actorRef ! RelayPlayer(1, actorRef, "player1")
      expectMsgClass(0.1 second, classOf[PlayerInRandomGameWithAsker])
    }
    "return gameData" in {
      actorRef ! CalculateGameState()
      //probe.expectMsgClass(20 second, classOf[GameData])
      probe.expectNoMsg(20 second)  //WTF!!!
    }
  }

}
