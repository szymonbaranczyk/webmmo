package szymonbaranczyk.exitFlow


import akka.actor.ActorRef
import akka.event.{EventBus, LookupClassification}
import com.typesafe.scalalogging.LazyLogging

final case class GameDataEnvelope(gameData: GameData, gameId: Int)

/**
  * Created by Szymon Barańczyk.
  *
  * Publishes the payload of the MsgEnvelope when the topic of the
  * MsgEnvelope equals the String specified when subscribing.
  */
class GameDataBus extends EventBus with LookupClassification with LazyLogging {
  type Event = GameDataEnvelope
  type Classifier = Int
  type Subscriber = ActorRef

  // is used for extracting the classifier from the incoming events
  override protected def classify(event: Event): Classifier = event.gameId

  // will be invoked for each event for all subscribers which registered themselves
  // for the event’s classifier
  override protected def publish(event: Event, subscriber: Subscriber): Unit = {
    subscriber ! event.gameData
  }

  // must define a full order over the subscribers, expressed as expected from
  // `java.lang.Comparable.compare`
  override protected def compareSubscribers(a: Subscriber, b: Subscriber): Int =
  a.compareTo(b)

  // determines the initial size of the index data structure
  // used internally (i.e. the expected number of different classifiers)
  override protected def mapSize: Int = 128

  override def subscribe(subscriber: ActorRef, to: Int): Boolean = {logger.debug(s"$subscriber subscribed to $to")
    super.subscribe(subscriber, to)}

  override def publish(event: GameDataEnvelope): Unit = super.publish(event)

}
