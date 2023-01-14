package org.scalastr.client

import org.bitcoins.core.util.{EnvUtil, TimeUtil}
import org.bitcoins.crypto._
import org.bitcoins.testkit.async.TestAsyncUtil
import org.scalastr.core._
import org.scalastr.testkit.EmbeddedRelay
import play.api.libs.json._

import java.net.URL
import scala.collection.mutable
import scala.concurrent._
import scala.concurrent.duration.{DurationInt, FiniteDuration}

class NostrClientTest extends EmbeddedRelay {

  val timeout: FiniteDuration = if (EnvUtil.isCI) 10.seconds else 3.seconds

  it must "publish an event and get the subscription" in {
    val privateKey: ECPrivateKey = ECPrivateKey.freshPrivateKey
    val eventPromise = Promise[NostrEvent]()

    val url =
      s"ws://${container.containerIpAddress}:${container.mappedPort(8080)}"

    val client: NostrClient = new NostrClient(url, None) {

      override def processEvent(
          subscriptionId: String,
          event: NostrEvent): Future[Unit] = {
        eventPromise.trySuccess(event)
        Future.unit
      }

      override def processNotice(notice: String): Future[Unit] = Future.unit
    }

    val event1 = NostrEvent.build(privateKey = privateKey,
                                  created_at = TimeUtil.currentEpochSecond,
                                  kind = NostrKind.TextNote,
                                  tags = JsArray.empty,
                                  content = "test")
    assert(event1.verify)

    val event2 = NostrEvent.build(privateKey = privateKey,
                                  created_at = TimeUtil.currentEpochSecond,
                                  kind = NostrKind.TextNote,
                                  tags = JsArray.empty,
                                  content = "test2")
    assert(event2.verify)

    val event3 = NostrEvent.build(privateKey = privateKey,
                                  created_at = TimeUtil.currentEpochSecond,
                                  kind = NostrKind.TextNote,
                                  tags = JsArray.empty,
                                  content = "test3")
    assert(event3.verify)

    val events = Vector(event1, event2, event3)

    val filter =
      NostrFilter(ids = None,
                  authors = Some(Vector(privateKey.schnorrPublicKey)),
                  kinds = None,
                  `#e` = None,
                  `#p` = None,
                  since = None,
                  until = None,
                  limit = None)

    for {
      _ <- client.start()
      _ <- Future.sequence(events.map(client.publishEvent))
      subscriptionId <- client.subscribe(filter)

      subEvent = Await.result(eventPromise.future, timeout)

      _ <- client.unsubscribe(subscriptionId)
    } yield assert(events.contains(subEvent))
  }

  it must "publish a metadata event and retrieve it" in {
    val privateKey: ECPrivateKey = ECPrivateKey.freshPrivateKey
    val eventPromise = Promise[NostrEvent]()

    val url =
      s"ws://${container.containerIpAddress}:${container.mappedPort(8080)}"

    val client: NostrClient = new NostrClient(url, None) {

      override def processEvent(
          subscriptionId: String,
          event: NostrEvent): Future[Unit] = {
        eventPromise.success(event)
        Future.unit
      }

      override def processNotice(notice: String): Future[Unit] = Future.unit
    }

    val metadata = Metadata.create(
      Some("scalastr"),
      Some("scalastr"),
      Some("scalastr is a scala nostr implementation"),
      Some("me@scalastr.org"),
      Some("me@scalastr.org"),
      Some(new URL("https://scalastr.org")),
      Some(new URL("https://scalastr.org/assets/images/scalastr.png")),
      Some(new URL("https://scalastr.org/assets/images/scalastr.png"))
    )

    val event = NostrEvent.build(privateKey = privateKey,
                                 created_at = TimeUtil.currentEpochSecond,
                                 tags = JsArray.empty,
                                 metadata = metadata)

    assert(event.verify)

    val filter =
      NostrFilter(ids = None,
                  authors = Some(Vector(privateKey.schnorrPublicKey)),
                  kinds = None,
                  `#e` = None,
                  `#p` = None,
                  since = None,
                  until = None,
                  limit = None)

    for {
      _ <- client.start()
      _ <- client.publishEvent(event)
      subscriptionId <- client.subscribe(filter)

      subEvent = Await.result(eventPromise.future, timeout)

      _ <- client.unsubscribe(subscriptionId)
    } yield assert(event == subEvent)
  }

  it must "handle restarts" in {
    val privateKey: ECPrivateKey = ECPrivateKey.freshPrivateKey
    val receivedEvents = mutable.Set.empty[NostrEvent]

    val url =
      s"ws://${container.containerIpAddress}:${container.mappedPort(8080)}"

    val client: NostrClient = new NostrClient(url, None) {

      override def processEvent(
          subscriptionId: String,
          event: NostrEvent): Future[Unit] = {
        receivedEvents.addOne(event)
        Future.unit
      }

      override def processNotice(notice: String): Future[Unit] = Future.unit
    }

    val event1 = NostrEvent.build(privateKey = privateKey,
                                  created_at = TimeUtil.currentEpochSecond,
                                  kind = NostrKind.TextNote,
                                  tags = JsArray.empty,
                                  content = "test")
    assert(event1.verify)

    val event2 = NostrEvent.build(privateKey = privateKey,
                                  created_at = TimeUtil.currentEpochSecond,
                                  kind = NostrKind.TextNote,
                                  tags = JsArray.empty,
                                  content = "test2")
    assert(event2.verify)

    val event3 = NostrEvent.build(privateKey = privateKey,
                                  created_at = TimeUtil.currentEpochSecond,
                                  kind = NostrKind.TextNote,
                                  tags = JsArray.empty,
                                  content = "test3")
    assert(event3.verify)

    val events = Vector(event1, event2, event3)

    val filter =
      NostrFilter(ids = None,
                  authors = Some(Vector(privateKey.schnorrPublicKey)),
                  kinds = None,
                  `#e` = None,
                  `#p` = None,
                  since = None,
                  until = None,
                  limit = None)

    for {
      _ <- client.start()
      _ <- client.publishEvent(event1)
      _ <- client.subscribe(filter)
      _ <- TestAsyncUtil.awaitCondition(() => receivedEvents.contains(event1))
      _ <- client.stop()

      _ <- client.start()
      _ <- client.publishEvent(event2)
      _ <- client.subscribe(filter)
      _ <- TestAsyncUtil.awaitCondition(() => receivedEvents.contains(event2))
      _ <- client.stop()

      _ <- client.start()
      _ <- client.publishEvent(event3)
      _ <- client.subscribe(filter)
      _ <- TestAsyncUtil.awaitCondition(() => receivedEvents.contains(event3))
      _ <- client.stop()
    } yield assert(receivedEvents == events.toSet)
  }
}
