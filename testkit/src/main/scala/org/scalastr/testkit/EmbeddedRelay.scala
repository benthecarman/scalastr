package org.scalastr.testkit

import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer}
import org.bitcoins.testkit.util.BitcoinSAsyncTest
import org.testcontainers.containers.wait.strategy.Wait

trait EmbeddedRelay extends BitcoinSAsyncTest with ForAllTestContainer {

  override val container: GenericContainer =
    GenericContainer("scsibug/nostr-rs-relay:latest",
                     exposedPorts = Seq(8080),
                     waitStrategy = Wait.forListeningPort())
}
