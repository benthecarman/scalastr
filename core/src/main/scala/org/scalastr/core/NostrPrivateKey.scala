package org.scalastr.core

import org.bitcoins.core.number.UInt8
import org.bitcoins.core.util.{Bech32, Bech32Encoding, NumberUtil}
import org.bitcoins.crypto._
import org.scalastr.core.NostrPrivateKey.expandedHrp
import scodec.bits.ByteVector

import scala.util.{Failure, Success}

case class NostrPrivateKey(key: ECPrivateKey) extends NetworkElement {

  def publicKey: NostrPublicKey = NostrPublicKey(key.schnorrPublicKey)

  override def toString: String = {
    val uint8s = UInt8.toUInt8s(key.bytes)
    val encoded = Bech32.from8bitTo5bit(uint8s)
    val checksum =
      Bech32.createChecksum(expandedHrp ++ encoded, Bech32Encoding.Bech32)
    val encoding = Bech32.encode5bitToString(encoded ++ checksum)

    NostrPrivateKey.hrp + Bech32.separator + encoding
  }

  override def bytes: ByteVector = key.bytes
}

object NostrPrivateKey
    extends Factory[NostrPrivateKey]
    with StringFactory[NostrPrivateKey] {

  final val hrp = "nsec"

  final val expandedHrp = Bech32.hrpExpand(hrp)

  def fromString(str: String): NostrPrivateKey = {
    fromHexT(str).orElse {
      Bech32.splitToHrpAndData(str, Bech32Encoding.Bech32).map {
        case (hrp, data) =>
          require(hrp.equalsIgnoreCase(this.hrp),
                  s"nostr secret key must start with nsec")
          val converted = NumberUtil.convertUInt5sToUInt8(data)
          val bytes = UInt8.toBytes(converted)
          val key = ECPrivateKey(bytes)
          NostrPrivateKey(key)
      }
    } match {
      case Success(key) => key
      case Failure(err) =>
        throw new IllegalArgumentException(
          s"Could not parse $str as a NostrPrivateKey, got: $err")
    }
  }

  override def fromBytes(bytes: ByteVector): NostrPrivateKey = NostrPrivateKey(
    ECPrivateKey(bytes))
}
