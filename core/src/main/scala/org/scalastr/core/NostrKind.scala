package org.scalastr.core

import play.api.libs.json._

sealed abstract class NostrKind(val int: Int)

object NostrKind {
  case object Metadata extends NostrKind(0)
  case object TextNote extends NostrKind(1)
  case object RecommendServer extends NostrKind(2)
  case object Contacts extends NostrKind(3)
  case object EncryptedDM extends NostrKind(4)
  case object EventDeletion extends NostrKind(5)
  case object Reaction extends NostrKind(7)
  case object ZapRequest extends NostrKind(9734)
  case object Zap extends NostrKind(9735)
  case class Unknown(override val int: Int) extends NostrKind(int)

  val known: Vector[NostrKind] = Vector(Metadata,
                                        TextNote,
                                        RecommendServer,
                                        Contacts,
                                        EncryptedDM,
                                        EventDeletion,
                                        Reaction,
                                        ZapRequest,
                                        Zap)

  def fromInt(int: Int): NostrKind = {
    known.find(_.int == int).getOrElse(Unknown(int))
  }

  implicit val nostrKindReads: Reads[NostrKind] = Reads { js =>
    js.validate[Int].map(fromInt)
  }

  implicit val nostrKindWrites: Writes[NostrKind] = Writes { kind =>
    JsNumber(kind.int)
  }
}
