package org.scalastr.core

import org.bitcoins.crypto._
import play.api.libs.json._
import org.bitcoins.commons.serializers.JsonWriters._
import org.bitcoins.commons.serializers.JsonReaders._
import org.bitcoins.commons.serializers.JsonSerializers._

sealed abstract class NostrMessage

case class NostrEvent(
    id: Sha256Digest,
    pubkey: SchnorrPublicKey,
    created_at: Long,
    kind: Int,
    tags: JsArray,
    content: String,
    sig: SchnorrDigitalSignature)
    extends NostrMessage {

  val payload: String =
    NostrEvent.createPayload(pubkey, created_at, kind, tags, content)

  def verify(): Boolean = {
    val hash = CryptoUtil.sha256(payload)
    pubkey.verify(hash, sig)
  }
}

object NostrEvent {

  implicit val nostrEventReads: Reads[NostrEvent] = Json.reads[NostrEvent]

  implicit val nostrEventWrites: OWrites[NostrEvent] = Json.writes[NostrEvent]

  def removeJsNulls[T <: JsValue](json: T): T = json match {
    case JsObject(fields) =>
      JsObject(fields.flatMap {
        case (_, JsNull) => None
        case (name, JsArray(arr)) =>
          val noNulls = arr.map(removeJsNulls)
          Some(name -> JsArray(noNulls))
        case (name, value) =>
          Some(name -> removeJsNulls(value))
      }).asInstanceOf[T]
    case JsArray(arr) =>
      val noNulls = arr.map(removeJsNulls)
      JsArray(noNulls).asInstanceOf[T]
    case other => other
  }

  def createPayload(
      pubkey: SchnorrPublicKey,
      created_at: Long,
      kind: Int,
      tags: JsArray,
      content: String): String = {
    val updatedTags = removeJsNulls(tags)
    val array = Json.arr(0, pubkey.hex, created_at, kind, updatedTags, content)
    array.toString()
  }

  def build(
      privateKey: ECPrivateKey,
      created_at: Long,
      kind: Int,
      tags: JsArray,
      content: String): NostrEvent = {
    val pubkey = privateKey.schnorrPublicKey
    val payload = createPayload(pubkey, created_at, kind, tags, content)

    val id = CryptoUtil.sha256(payload)
    val sig = privateKey.schnorrSign(id.bytes)

    NostrEvent(id, pubkey, created_at, kind, tags, content, sig)
  }
}

case class NostrFilter(
    ids: Vector[Sha256Digest],
    authors: Vector[SchnorrPublicKey],
    kinds: Vector[Int],
    `#e`: Vector[Sha256Digest],
    `#p`: Vector[SchnorrPublicKey],
    since: Long,
    until: Long,
    limit: Int
) extends NostrMessage

object NostrFilter {
  implicit val nostrFilterReads: Reads[NostrFilter] = Json.reads[NostrFilter]

  implicit val nostrFilterWrites: OWrites[NostrFilter] =
    Json.writes[NostrFilter]
}
