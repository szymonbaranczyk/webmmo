package szymonbaranczyk.dataLayer

import akka.actor.{Actor, ActorRef, PoisonPill}
import akka.actor.Actor.Receive
import com.typesafe.scalalogging.LazyLogging
import szymonbaranczyk.exitFlow.PlayerData
import szymonbaranczyk.helper.InputJsonParser

/**
  * Created by SBARANCZ on 2016-11-07.
  */
class BulletActor(id: Int, var state: BulletState, xSpeed: Int, ySpeed: Int, owner: String) extends Actor with LazyLogging {
  val size = 1000

  override def receive = {
    case CheckCollisions(players) =>
      for (p <- players) p._2 ! GetDataWithoutCalc()
      if (state.x < 0 || state.y < 0 || state.x > size || state.y > size) {
        self ! PoisonPill
      }
    case CalculateStep() =>
      state = BulletState(state.x + xSpeed, state.y + ySpeed)
      sender ! state
    case p: PlayerData =>
      if (Math.abs(p.x - state.x) < 100 && Math.abs(p.y - state.y) < 100 && !p.id.equals(owner)) {
        context.parent ! Collide(p.id)
        self ! PoisonPill
      }

  }
  override def postStop(): Unit = context.parent ! DeleteBullet(id)
}

case class CalculateStep()
case class BulletState(x:Int,y:Int)

case class CheckCollisions(players : Map[String,ActorRef])

case class DeleteBullet(id:Int)

case class Collide(id: String)
