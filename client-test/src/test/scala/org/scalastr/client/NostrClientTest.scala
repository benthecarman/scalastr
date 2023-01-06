package org.scalastr.client

import org.bitcoins.core.util.TimeUtil
import org.bitcoins.crypto._
import org.scalastr.core._
import org.scalastr.testkit.EmbeddedRelay
import play.api.libs.json._

import java.net.URL
import scala.concurrent._
import scala.concurrent.duration.DurationInt

class NostrClientTest extends EmbeddedRelay {

  it must "publish an event and get the subscription" in {
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

    val event = NostrEvent.build(privateKey = privateKey,
                                 created_at = TimeUtil.currentEpochSecond,
                                 kind = NostrKind.TextNote,
                                 tags = JsArray.empty,
                                 content = "test")

    assert(event.verify())

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

      subEvent = Await.result(eventPromise.future, 10.seconds)

      _ <- client.unsubscribe(subscriptionId)
    } yield assert(event == subEvent)
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
      Some(new URL("https://scalastr.org/assets/images/scalastr.png"))
    )

    val event = NostrEvent.build(privateKey = privateKey,
                                 created_at = TimeUtil.currentEpochSecond,
                                 tags = JsArray.empty,
                                 metadata = metadata)

    assert(event.verify())

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

      subEvent = Await.result(eventPromise.future, 10.seconds)

      _ <- client.unsubscribe(subscriptionId)
    } yield assert(event == subEvent)
  }
}
