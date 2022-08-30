package org.scalastr.testkit

import com.dimafeng.testcontainers.GenericContainer
import org.bitcoins.testkit.fixtures.BitcoinSFixture
import org.scalatest.FutureOutcome
import org.testcontainers.containers.wait.strategy.Wait

import scala.concurrent.Future

trait EmbeddedRelay extends BitcoinSFixture {

  override type FixtureParam = GenericContainer

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {
    makeDependentFixture[GenericContainer](
      () => {
        val container: GenericContainer =
          GenericContainer("scsibug/nostr-rs-relay:latest",
                           exposedPorts = Seq(8080),
                           waitStrategy = Wait.forListeningPort())
        container.start()
        Future.successful(container)
      },
      { container =>
        container.stop()
        Future.unit
      }
    )(test)
  }
}
