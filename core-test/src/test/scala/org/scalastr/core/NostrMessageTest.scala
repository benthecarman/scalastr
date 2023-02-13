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
                                    Vector.empty,
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
                                     Vector.empty,
                                     key2.schnorrPublicKey)
    assert(dm1.verify)

    val encrypted =
      "dJc+WbBgaFCD2/kfg1XCWJParplBDxnZIdJGZ6FCTOg=?iv=M6VxRPkMZu7aIdD+10xPuw=="
    val dm2 =
      NostrEvent.build(key1,
                       TimeUtil.currentEpochSecond,
                       NostrKind.EncryptedDM,
                       Vector(Json.arr("p", key2.schnorrPublicKey.hex)),
                       encrypted)

    val decrypted = NostrEvent.decryptDM(dm1, key2)
    assert(decrypted == message)

    val decrypted2 = NostrEvent.decryptDM(dm2, key2)
    assert(decrypted2 == message)
  }

  it must "validate a zap request" in {
    val str =
      "{\"pubkey\":\"32e1827635450ebb3c5a7d12c1f8e7b2b514439ac10a67eef3d9fd9c5c68e245\",\"content\":\"\",\"id\":\"d9cc14d50fcb8c27539aacf776882942c1a11ea4472f8cdec1dea82fab66279d\",\"created_at\":1674164539,\"sig\":\"77127f636577e9029276be060332ea565deaf89ff215a494ccff16ae3f757065e2bc59b2e8c113dd407917a010b3abd36c8d7ad84c0e3ab7dab3a0b0caa9835d\",\"kind\":9734,\"tags\":[[\"e\",\"3624762a1274dd9636e0c552b53086d70bc88c165bc4dc0f9e836a1eaf86c3b8\"],[\"p\",\"32e1827635450ebb3c5a7d12c1f8e7b2b514439ac10a67eef3d9fd9c5c68e245\"],[\"relays\",\"wss://relay.damus.io\",\"wss://nostr-relay.wlvs.space\",\"wss://nostr.fmt.wiz.biz\",\"wss://relay.nostr.bg\",\"wss://nostr.oxtr.dev\",\"wss://nostr.v0l.io\",\"wss://brb.io\",\"wss://nostr.bitcoiner.social\",\"ws://monad.jb55.com:8080\",\"wss://relay.snort.social\"]]}"
    val event = Json.parse(str).as[NostrEvent]
    assert(event.verify)
    assert(event.kind == NostrKind.ZapRequest)
    assert(event.tags.exists(_.value.head.asOpt[String].contains("p")))
    assert(event.tags.count(_.value.head.asOpt[String].contains("e")) < 2)
    assert(event.taggedRelays.nonEmpty)
    assert(NostrEvent.isValidZapRequest(event))
  }
}
