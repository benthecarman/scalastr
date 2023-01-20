package org.scalastr.core

import org.bitcoins.testkitcore.util.BitcoinSUnitTest
import org.bitcoins.crypto.SchnorrPublicKey

class NostrPublicKeyTest extends BitcoinSUnitTest {

  it must "parse a npub" in {
    val npub = "npub1u8lnhlw5usp3t9vmpz60ejpyt649z33hu82wc2hpv6m5xdqmuxhs46turz"
    val key = "e1ff3bfdd4e40315959b08b4fcc8245eaa514637e1d4ec2ae166b743341be1af"

    val parsed = NostrPublicKey.fromString(npub)
    assert(parsed.key.hex == key)
  }

  it must "create a npub" in {
    val npub = "npub1u8lnhlw5usp3t9vmpz60ejpyt649z33hu82wc2hpv6m5xdqmuxhs46turz"
    val key = "e1ff3bfdd4e40315959b08b4fcc8245eaa514637e1d4ec2ae166b743341be1af"

    val nostrKey = NostrPublicKey(SchnorrPublicKey(key))
    assert(nostrKey.toString == npub)
  }
}
