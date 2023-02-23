package org.scalastr.nip5

import org.bitcoins.testkit.util.BitcoinSAsyncTest
import org.scalastr.core.NostrPublicKey

class Nip5ClientTest extends BitcoinSAsyncTest {

  behavior of "Nip5Client"

  it must "get the public key from a nip5 address" in {
    val nip5 = Nip5Address("_@benthecarman.com")

    val client = new Nip5Client()

    client.getPublicKey(nip5).map { key =>
      assert(
        key == NostrPublicKey.fromString(
          "npub1u8lnhlw5usp3t9vmpz60ejpyt649z33hu82wc2hpv6m5xdqmuxhs46turz"))
    }
  }
}
