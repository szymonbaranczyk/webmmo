package szymonbaranczyk

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import szymonbaranczyk.dataLayer.GameActor
import szymonbaranczyk.enterFlow.{PlayerInRandomGameWithAsker, RelayPlayer}
import szymonbaranczyk.exitFlow.GameDataBus

import scala.concurrent.duration._

/**
  * Created by Szymon Barańczyk.
  */

class GameActorSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "Actor" should {
    "create a player" in {
      val actorRef = TestActorRef(new GameActor(new GameDataBus()))
      actorRef ! RelayPlayer(1, actorRef, "player1")
      expectMsgClass(0.1 second, classOf[PlayerInRandomGameWithAsker])
    }
  }

}
