package szymonbaranczyk.dataLayer

import akka.actor.Actor
import szymonbaranczyk.enterFlow.PlayerInputEnvelope

/**
  * Created by User1 on 17/09/2016.
  */
class GameActor extends Actor {
  override def receive = {
    case PlayerInputEnvelope(_, _, id) =>
  }
}
