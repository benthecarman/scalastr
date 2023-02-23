package org.scalastr.nip5

import java.net.URLEncoder

case class Nip5Address(value: String) extends StringValue {

  val (user, domain): (Nip5Address.User, Nip5Address.Domain) =
    value match {
      case Nip5Address.validNip5(m, d) =>
        (Nip5Address.User(m), Nip5Address.Domain(d))
      case invalid =>
        throw new IllegalArgumentException(
          s"'$invalid' is not a valid nip5 address")
    }

  def verifyUrl: String =
    s"https://$domain/.well-known/nostr.json?user=${URLEncoder.encode(user, "UTF-8")}"

  override def equals(nip5: Any): Boolean = {
    nip5 match {
      case p: Nip5Address => p.value.toLowerCase == this.value.toLowerCase
      case p: String      => p.toLowerCase == this.value.toLowerCase
      case _              => false
    }
  }
}

object Nip5Address {

  import play.api.libs.json._

  implicit val nip5AddressReads: Reads[Nip5Address] = (js: JsValue) =>
    js.validate[String].flatMap {
      case s if Nip5Address.isValid(s) => JsSuccess(Nip5Address(s))
      case _                           => JsError("not a valid nip5 address")
    }

  implicit val nip5AddressWrites: Writes[Nip5Address] = (e: Nip5Address) =>
    JsString(e.value)

  final private[nip5] val validDomain =
    """^([a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*)$""".r

  final private[nip5] val validNip5 =
    """^([a-zA-Z0-9.!#$%&’'*+/=?^_`{|}~-]+)@([a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*)$""".r

  def isValid(nip5: String): Boolean = nip5 match {
    case validNip5(_, _) => true
    case _               => false
  }

  case class User private[Nip5Address] (value: String) extends StringValue {

    override def equals(email: Any): Boolean = {
      email match {
        case p: User   => p.value.toLowerCase == this.value.toLowerCase
        case p: String => p.toLowerCase == this.value.toLowerCase
        case _         => false
      }
    }
  }

  case class Domain(value: String) extends StringValue {

    value match {
      case Nip5Address.validDomain(_) => ()
      case invalidDomain =>
        throw new IllegalArgumentException(
          s"'$invalidDomain' is not a valid nip5 address")
    }

    override def equals(email: Any): Boolean = {
      email match {
        case p: Domain => p.value.toLowerCase == this.value.toLowerCase
        case p: String => p.toLowerCase == this.value.toLowerCase
        case _         => false
      }
    }
  }
}
