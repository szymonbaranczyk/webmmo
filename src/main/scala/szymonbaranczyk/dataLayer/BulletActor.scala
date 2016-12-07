package szymonbaranczyk.dataLayer

import akka.actor.{Actor, ActorRef, PoisonPill}
import com.typesafe.scalalogging.LazyLogging
import szymonbaranczyk.exitFlow.PlayerData

/**
  * Created by Szymon Barańczyk.
  */
class BulletActor(id: Int, var state: BulletState, xSpeed: Int, ySpeed: Int, owner: String) extends Actor with LazyLogging {
  val size = 4000

  override def receive = {
    case CheckCollisions(players) =>
      if (state.x < 0 || state.y < 0 || state.x > size || state.y > size) {
        self ! PoisonPill
      }
      else for (p <- players) p._2 ! GetDataWithoutCalc()
    case CalculateStep() =>
      state = BulletState(state.x + xSpeed, state.y + ySpeed, id)
      sender ! state
    case p: PlayerData =>
      if (Math.abs(p.x - state.x) < 45 && Math.abs(p.y - state.y) < 45 && !p.id.equals(owner)) {
        context.parent ! Collide(p.id)
        self ! PoisonPill
      }

  }

  override def postStop(): Unit = {
    context.parent ! DeleteBullet(id)
  }
}

case class CalculateStep()

case class BulletState(x: Int, y: Int, id: Int)

case class CheckCollisions(players : Map[String,ActorRef])

case class DeleteBullet(id:Int)

case class Collide(id: String)
