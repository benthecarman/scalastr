package org.scalastr.client

import org.bitcoins.core.util.TimeUtil
import org.bitcoins.crypto._
import org.bitcoins.testkit.util.BitcoinSAsyncTest
import org.scalastr.core._
import play.api.libs.json._

import scala.concurrent._
import scala.concurrent.duration.DurationInt

class NostrClientTest extends BitcoinSAsyncTest {

  val privateKey: ECPrivateKey = ECPrivateKey.freshPrivateKey
  private val eventPromise = Promise[NostrEvent]()

  val client: NostrClient = new NostrClient("ws://localhost:7000", None) {

    override def processEvent(
        subscriptionId: String,
        event: NostrEvent): Future[Unit] = {
      eventPromise.success(event)
      Future.unit
    }

    override def processNotice(notice: String): Future[Unit] = Future.unit
  }

  it must "publish an event and get the subscription" in {
    val event = NostrEvent.build(privateKey = privateKey,
                                 created_at = TimeUtil.currentEpochSecond,
                                 kind = 1,
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
}
