package szymonbaranczyk.dataLayer

import akka.actor.{Actor, ActorRef, Props}
import szymonbaranczyk.enterFlow.{PlayerInRandomGame, PlayerInRandomGameWithAsker, RelayPlayer}

/**
  * Created by User1 on 17/09/2016.
  */
class GameActor extends Actor {
  var players = Map.empty[String, ActorRef]

  override def receive = {
    case RelayPlayer(gameId, asker) =>
      val ref = context.actorOf(Props[GameActor])
      //TODO random strings
      players += ("lol" -> ref)
      sender ! PlayerInRandomGameWithAsker(PlayerInRandomGame(gameId, ref), asker)
  }
}
