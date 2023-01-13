package org.scalastr.core

import org.bitcoins.core.util.TimeUtil
import org.bitcoins.crypto.ECPrivateKey
import org.bitcoins.testkitcore.util.BitcoinSUnitTest
import org.scalastr.core.NostrEvent.getAesKey
import play.api.libs.json._

class NostrMessageTest extends BitcoinSUnitTest {

  it must "parse an event" in {
    val json =
      "{\"id\":\"2be17aa3031bdcb006f0fce80c146dea9c1c0268b0af2398bb673365c6444d45\",\"pubkey\":\"f86c44a2de95d9149b51c6a29afeabba264c18e2fa7c49de93424a0c56947785\",\"created_at\":1640839235,\"kind\":4,\"tags\":[[\"p\",\"13adc511de7e1cfcf1c6b7f6365fb5a03442d7bcacf565ea57fa7770912c023d\"]],\"content\":\"uRuvYr585B80L6rSJiHocw==?iv=oh6LVqdsYYol3JfFnXTbPA==\",\"sig\":\"a5d9290ef9659083c490b303eb7ee41356d8778ff19f2f91776c8dc4443388a64ffcf336e61af4c25c05ac3ae952d1ced889ed655b67790891222aaa15b99fdd\"}\""

    val event = Json.parse(json).as[NostrEvent]
    assert(
      event.id.hex == "2be17aa3031bdcb006f0fce80c146dea9c1c0268b0af2398bb673365c6444d45")
    assert(event.verify)
  }

  it must "encrypt and decrypt a DM" in {
    val key1 = ECPrivateKey.freshPrivateKey
    val key2 = ECPrivateKey.freshPrivateKey

    val message = "hello world"
    val dm = NostrEvent.encryptedDM(message,
                                    key1,
                                    TimeUtil.currentEpochSecond,
                                    JsArray.empty,
                                    key2.schnorrPublicKey)
    assert(dm.verify)

    val decrypted = NostrEvent.decryptDM(dm, key2)
    assert(decrypted == message)
  }

  it must "pass encrypted dm unit test" in {
    val key1 = ECPrivateKey(
      "6b911fd37cdf5c81d4c0adb1ab7fa822ed253ab0ad9aa18d77257c88b29b718e")
    val key2 = ECPrivateKey(
      "7b911fd37cdf5c81d4c0adb1ab7fa822ed253ab0ad9aa18d77257c88b29b718e")

    val aesKey1 = getAesKey(key1, key2.schnorrPublicKey)
    val aesKey2 = getAesKey(key2, key1.schnorrPublicKey)

    assert(aesKey1 == aesKey2)

    val message = "Saturn, bringer of old age"
    val dm1 = NostrEvent.encryptedDM(message,
                                     key1,
                                     TimeUtil.currentEpochSecond,
                                     JsArray.empty,
                                     key2.schnorrPublicKey)
    assert(dm1.verify)

    val encrypted =
      "dJc+WbBgaFCD2/kfg1XCWJParplBDxnZIdJGZ6FCTOg=?iv=M6VxRPkMZu7aIdD+10xPuw=="
    val dm2 =
      NostrEvent.build(key1,
                       TimeUtil.currentEpochSecond,
                       NostrKind.EncryptedDM,
                       Json.arr(Json.arr("p", key2.schnorrPublicKey.hex)),
                       encrypted)

    val decrypted = NostrEvent.decryptDM(dm1, key2)
    assert(decrypted == message)

    val decrypted2 = NostrEvent.decryptDM(dm2, key2)
    assert(decrypted2 == message)
  }
}
