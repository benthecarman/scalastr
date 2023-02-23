package org.scalastr.core

import org.bitcoins.core.number.UInt8
import org.bitcoins.core.util.{Bech32, Bech32Encoding, NumberUtil}
import org.bitcoins.crypto._
import org.scalastr.core.NostrPublicKey.expandedHrp
import scodec.bits.ByteVector

import scala.util.{Failure, Success}

case class NostrPublicKey(key: SchnorrPublicKey) extends NetworkElement {

  override def toString: String = {
    val uint8s = UInt8.toUInt8s(key.bytes)
    val encoded = Bech32.from8bitTo5bit(uint8s)
    val checksum =
      Bech32.createChecksum(expandedHrp ++ encoded, Bech32Encoding.Bech32)
    val encoding = Bech32.encode5bitToString(encoded ++ checksum)

    NostrPublicKey.hrp + Bech32.separator + encoding
  }

  override def bytes: ByteVector = key.bytes
}

object NostrPublicKey
    extends Factory[NostrPublicKey]
    with StringFactory[NostrPublicKey] {

  final val hrp = "npub"

  final val expandedHrp = Bech32.hrpExpand(hrp)

  override def apply(string: String): NostrPublicKey =
    NostrPublicKey.fromString(string)

  def fromString(str: String): NostrPublicKey = {
    fromHexT(str).orElse {
      Bech32.splitToHrpAndData(str, Bech32Encoding.Bech32).map {
        case (hrp, data) =>
          require(hrp.equalsIgnoreCase(this.hrp),
                  s"nostr public key must start with npub")
          val converted = NumberUtil.convertUInt5sToUInt8(data)
          val bytes = UInt8.toBytes(converted)
          val key = SchnorrPublicKey(bytes)
          NostrPublicKey(key)
      }
    } match {
      case Success(key) => key
      case Failure(err) =>
        throw new IllegalArgumentException(
          s"Could not parse $str as a NostrPublicKey, got: $err")
    }
  }

  override def fromBytes(bytes: ByteVector): NostrPublicKey = NostrPublicKey(
    SchnorrPublicKey(bytes))
}
