package org.scalastr.client

import org.bitcoins.core.util.TimeUtil
import org.bitcoins.crypto._
import org.bitcoins.testkit.util.BitcoinSAsyncTest
import org.scalastr.core.NostrEvent
import play.api.libs.json.JsArray

import scala.concurrent._

class NostrClientTest extends BitcoinSAsyncTest {

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

  it must "publish an event" in {
    val privateKey = ECPrivateKey.freshPrivateKey

    client.start().flatMap { _ =>
      val event = NostrEvent.build(privateKey = privateKey,
                                   created_at = TimeUtil.currentEpochSecond,
                                   kind = 1,
                                   tags = JsArray.empty,
                                   content = "test")

      client.publishEvent(event).map { _ =>
        succeed
      }
    }
  }
}
