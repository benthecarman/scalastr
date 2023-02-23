package org.scalastr.core

import org.bitcoins.core.number.UInt8
import org.bitcoins.core.util.{Bech32, Bech32Encoding, NumberUtil}
import org.bitcoins.crypto._
import org.scalastr.core.NostrNoteId.expandedHrp
import scodec.bits.ByteVector

import scala.util.{Failure, Success}

case class NostrNoteId(id: Sha256Digest) extends NetworkElement {

  override def toString: String = {
    val uint8s = UInt8.toUInt8s(id.bytes)
    val encoded = Bech32.from8bitTo5bit(uint8s)
    val checksum =
      Bech32.createChecksum(expandedHrp ++ encoded, Bech32Encoding.Bech32)
    val encoding = Bech32.encode5bitToString(encoded ++ checksum)

    NostrNoteId.hrp + Bech32.separator + encoding
  }

  override def bytes: ByteVector = id.bytes
}

object NostrNoteId
    extends Factory[NostrNoteId]
    with StringFactory[NostrNoteId] {

  final val hrp = "note"

  final val expandedHrp = Bech32.hrpExpand(hrp)

  override def apply(string: String): NostrNoteId =
    NostrNoteId.fromString(string)

  def fromString(str: String): NostrNoteId = {
    fromHexT(str).orElse {
      Bech32.splitToHrpAndData(str, Bech32Encoding.Bech32).map {
        case (hrp, data) =>
          require(hrp.equalsIgnoreCase(this.hrp),
                  s"nostr note id must start with note")
          val converted = NumberUtil.convertUInt5sToUInt8(data)
          val bytes = UInt8.toBytes(converted)
          val hash = Sha256Digest(bytes)
          NostrNoteId(hash)
      }
    } match {
      case Success(key) => key
      case Failure(err) =>
        throw new IllegalArgumentException(
          s"Could not parse $str as a NostrNoteId, got: $err")
    }
  }

  override def fromBytes(bytes: ByteVector): NostrNoteId = NostrNoteId(
    Sha256Digest(bytes))
}
