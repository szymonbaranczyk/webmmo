package szymonbaranczyk.managers

import akka.actor.Actor

/**
  * Created by Szymon Barańczyk.
  */
case class getId()

class GameIdService extends Actor {
  override def receive = {
    case getId() => sender ! 1
  }
}
