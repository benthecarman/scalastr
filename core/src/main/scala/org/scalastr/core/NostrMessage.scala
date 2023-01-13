package org.scalastr.core

import org.bitcoins.crypto._
import play.api.libs.json._

sealed abstract class NostrMessage

case class NostrEvent(
    id: Sha256Digest,
    pubkey: SchnorrPublicKey,
    created_at: Long,
    kind: NostrKind,
    tags: JsArray,
    content: String,
    sig: SchnorrDigitalSignature)
    extends NostrMessage {

  private lazy val payload: String =
    NostrEvent.createPayload(pubkey, created_at, kind, tags, content)

  private lazy val hash = CryptoUtil.sha256(payload)

  lazy val verify: Boolean = hash == id && pubkey.verify(hash, sig)
}

object NostrEvent extends SerializerUtil {

  implicit val nostrEventReads: Reads[NostrEvent] = Json.reads[NostrEvent]

  implicit val nostrEventWrites: OWrites[NostrEvent] = Json.writes[NostrEvent]

  private def createPayload(
      pubkey: SchnorrPublicKey,
      created_at: Long,
      kind: NostrKind,
      tags: JsArray,
      content: String): String = {
    val updatedTags = removeJsNulls(tags)
    val array =
      Json.arr(0, pubkey.hex, created_at, kind.int, updatedTags, content)
    array.toString()
  }

  def build(
      privateKey: ECPrivateKey,
      created_at: Long,
      kind: NostrKind,
      tags: JsArray,
      content: String): NostrEvent = {
    val pubkey = privateKey.schnorrPublicKey
    val payload = createPayload(pubkey, created_at, kind, tags, content)

    val id = CryptoUtil.sha256(payload)
    val sig = privateKey.schnorrSign(id.bytes)

    NostrEvent(id, pubkey, created_at, kind, tags, content, sig)
  }

  def build(
      privateKey: ECPrivateKey,
      created_at: Long,
      tags: JsArray,
      metadata: Metadata): NostrEvent = {
    build(privateKey,
          created_at,
          NostrKind.Metadata,
          tags,
          Json.toJson(metadata).toString)
  }
}

case class NostrFilter(
    ids: Option[Vector[Sha256Digest]],
    authors: Option[Vector[SchnorrPublicKey]],
    kinds: Option[Vector[Int]],
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
