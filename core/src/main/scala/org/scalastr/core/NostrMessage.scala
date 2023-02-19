package org.scalastr.core

import org.bitcoins.core.protocol.ln.currency.MilliSatoshis
import org.bitcoins.crypto.{AesCrypt => _, _}
import org.scalastr.core.crypto.AesCrypt
import play.api.libs.json._
import scodec.bits.ByteVector

sealed abstract class NostrMessage

case class NostrEvent(
    id: Sha256Digest,
    pubkey: SchnorrPublicKey,
    created_at: Long,
    kind: NostrKind,
    tags: Vector[JsArray],
    content: String,
    sig: SchnorrDigitalSignature)
    extends NostrMessage {

  private lazy val payload: String =
    NostrEvent.createPayload(pubkey, created_at, kind, tags, content)

  private lazy val hash = CryptoUtil.sha256(payload)

  def taggedRelays: Vector[String] = {
    tags
      .find(_.value.headOption.contains(JsString("relays")))
      .map { jsArray =>
        jsArray.value.tail.collect { case JsString(str) =>
          str
        }.toVector
      }
      .getOrElse(Vector.empty)
  }

  lazy val verify: Boolean = hash == id && pubkey.verify(hash, sig)
}

object NostrEvent extends SerializerUtil {

  implicit val nostrEventReads: Reads[NostrEvent] = Json.reads[NostrEvent]

  implicit val nostrEventWrites: OWrites[NostrEvent] = Json.writes[NostrEvent]

  private def createPayload(
      pubkey: SchnorrPublicKey,
      created_at: Long,
      kind: NostrKind,
      tags: Vector[JsArray],
      content: String): String = {
    val updatedTags = tags.map(removeJsNulls)
    val array =
      Json.arr(0, pubkey.hex, created_at, kind.int, updatedTags, content)
    array.toString()
  }

  def build(
      privateKey: ECPrivateKey,
      created_at: Long,
      kind: NostrKind,
      tags: Vector[JsArray],
      content: String): NostrEvent = {
    val pubkey = privateKey.schnorrPublicKey
    val payload = createPayload(pubkey, created_at, kind, tags, content)

    val id = CryptoUtil.sha256(payload)
    val sig = privateKey.schnorrSign(id.bytes)

    NostrEvent(id, pubkey, created_at, kind, tags, content, sig)
  }

  def build(
      privateKey: NostrPrivateKey,
      created_at: Long,
      kind: NostrKind,
      tags: Vector[JsArray],
      content: String): NostrEvent = {
    build(privateKey.key, created_at, kind, tags, content)
  }

  def build(
      privateKey: ECPrivateKey,
      created_at: Long,
      tags: Vector[JsArray],
      metadata: Metadata): NostrEvent = {
    build(privateKey,
          created_at,
          NostrKind.Metadata,
          tags,
          Json.toJson(metadata).toString)
  }

  def build(
      privateKey: NostrPrivateKey,
      created_at: Long,
      tags: Vector[JsArray],
      metadata: Metadata): NostrEvent = {
    build(privateKey.key,
          created_at,
          NostrKind.Metadata,
          tags,
          Json.toJson(metadata).toString)
  }

  private[core] def getAesKey(
      privateKey: ECPrivateKey,
      publicKey: SchnorrPublicKey): AesKey = {
    val sharedPoint =
      CryptoUtil.tweakMultiply(publicKey.publicKey,
                               privateKey.schnorrKey.fieldElement)
    AesKey.fromValidBytes(sharedPoint.bytes.tail)
  }

  def encryptedDM(
      message: String,
      privateKey: ECPrivateKey,
      created_at: Long,
      tags: Vector[JsArray],
      recipient: SchnorrPublicKey): NostrEvent = {
    val aesKey = getAesKey(privateKey, recipient)

    val encrypted = AesCrypt.encrypt(ByteVector(message.getBytes), aesKey)
    val content =
      encrypted.cipherText.toBase64 + "?iv=" + encrypted.iv.bytes.toBase64

    val pTag = Json.arr("p", recipient.hex)
    val withP = if (tags.contains(pTag)) tags else tags :+ pTag
    build(privateKey, created_at, NostrKind.EncryptedDM, withP, content)
  }

  def decryptDM(event: NostrEvent, privateKey: ECPrivateKey): String = {
    require(event.kind == NostrKind.EncryptedDM, "Event must be encrypted DM")
    require(event.tags.contains(Json.arr("p", privateKey.schnorrPublicKey.hex)),
            "Event must be encrypted DM for this user")
    require(event.verify, "Event must be valid")

    val aesKey = getAesKey(privateKey, event.pubkey)

    val content = event.content
    val split = content.split("\\?iv=", 2)
    val cipherText = split.head
    val iv = split.last
    val ivBytes = ByteVector.fromValidBase64(iv)
    val encrypted = AesEncryptedData(ByteVector.fromValidBase64(cipherText),
                                     AesIV.fromValidBytes(ivBytes))

    AesCrypt.decrypt(encrypted, aesKey) match {
      case Left(err)    => throw err
      case Right(bytes) => bytes.decodeUtf8Lenient
    }
  }

  def isValidZapRequest(event: NostrEvent, amount: MilliSatoshis): Boolean = {
    event.kind == NostrKind.ZapRequest &&
    event.tags.exists(_.value.head.asOpt[String].contains("p")) &&
    event.tags
      .find(_.value.headOption.contains(JsString("amount")))
      .forall(
        _.value.lastOption.flatMap(_.asOpt[Long]).contains(amount.toLong)) &&
    event.tags.count(_.value.head.asOpt[String].contains("e")) < 2 &&
    event.taggedRelays.nonEmpty &&
    event.verify
  }
}

case class NostrFilter(
    ids: Option[Vector[Sha256Digest]],
    authors: Option[Vector[SchnorrPublicKey]],
    kinds: Option[Vector[NostrKind]],
    `#e`: Option[Vector[Sha256Digest]],
    `#p`: Option[Vector[SchnorrPublicKey]],
    since: Option[Long],
    until: Option[Long],
    limit: Option[Int]
) extends NostrMessage

object NostrFilter extends SerializerUtil {

  implicit val nostrFilterReads: Reads[NostrFilter] = Json.reads[NostrFilter]

  implicit val nostrFilterWrites: OWrites[NostrFilter] =
    Json.writes[NostrFilter]
}
