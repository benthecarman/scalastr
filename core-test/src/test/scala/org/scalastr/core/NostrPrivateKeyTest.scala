package org.scalastr.core

import org.bitcoins.crypto.ECPrivateKey
import org.bitcoins.testkitcore.util.BitcoinSUnitTest

class NostrPrivateKeyTest extends BitcoinSUnitTest {

  it must "parse a nsec" in {
    val npub = "nsec17f89xznamnkljw6l8ft5ehvahsgfhu6qjcy7kv286hmvkwf42qjqgagrfs"
    val key = "f24e530a7ddcedf93b5f3a574cdd9dbc109bf3409609eb3147d5f6cb39355024"

    val parsed = NostrPrivateKey.fromString(npub)
    assert(parsed.key.hex == key)
  }

  it must "create a nsec" in {
    val npub = "nsec17f89xznamnkljw6l8ft5ehvahsgfhu6qjcy7kv286hmvkwf42qjqgagrfs"
    val key = "f24e530a7ddcedf93b5f3a574cdd9dbc109bf3409609eb3147d5f6cb39355024"

    val nostrKey = NostrPrivateKey(ECPrivateKey(key))
    assert(nostrKey.toString == npub)
  }
}
