package org.scalastr.core

import org.bitcoins.crypto.{
  SchnorrDigitalSignature,
  SchnorrPublicKey,
  Sha256Digest
}
import play.api.libs.json._

trait SerializerUtil {

  def processJsNumberBigInt[T](numFunc: BigInt => T)(
      json: JsValue): JsResult[T] =
    json match {
      case JsNumber(nDecimal) =>
        val nOpt = nDecimal.toBigIntExact
        nOpt match {
          case Some(t) => JsSuccess(numFunc(t))
          case None =>
            JsError(s"Could not parsed expected t from given string $nDecimal")
        }
      case err @ (JsNull | _: JsBoolean | _: JsString | _: JsArray |
          _: JsObject) =>
        buildJsErrorMsg("jsnumber", err)
    }

  def buildJsErrorMsg(expected: String, err: JsValue): JsError = {
    JsError(s"error.expected.$expected, got ${Json.toJson(err).toString()}")
  }

  def buildErrorMsg(expected: String, err: Any): JsError = {
    JsError(s"error.expected.$expected, got ${err.toString}")
  }

  // For use in implementing reads method of Reads[T] where T is constructed from a JsNumber via numFunc
  def processJsNumber[T](numFunc: BigDecimal => T)(json: JsValue): JsResult[T] =
    json match {
      case JsNumber(n) => JsSuccess(numFunc(n))
      case err @ (JsNull | _: JsBoolean | _: JsString | _: JsArray |
          _: JsObject) =>
        SerializerUtil.buildJsErrorMsg("jsnumber", err)
    }

  def processJsObject[T](f: JsObject => T)(json: JsValue): JsResult[T] = {
    json match {
      case obj: JsObject => JsSuccess(f(obj))
      case err @ (JsNull | _: JsBoolean | _: JsString | _: JsArray |
          _: JsNumber) =>
        SerializerUtil.buildJsErrorMsg("jsobject", err)
    }
  }

  // For use in implementing reads method of Reads[T] where T is constructed from a JsString via strFunc
  def processJsString[T](strFunc: String => T)(json: JsValue): JsResult[T] =
    json match {
      case JsString(s) => JsSuccess(strFunc(s))
      case err @ (JsNull | _: JsBoolean | _: JsNumber | _: JsArray |
          _: JsObject) =>
        SerializerUtil.buildJsErrorMsg("jsstring", err)
    }

  def processJsStringOpt[T](f: String => Option[T])(
      jsValue: JsValue): JsResult[T] = {
    jsValue match {
      case JsString(key) =>
        val tOpt = f(key)
        tOpt match {
          case Some(t) => JsSuccess(t)
          case None => SerializerUtil.buildErrorMsg("invalid jsstring", jsValue)
        }
      case err @ (_: JsNumber | _: JsObject | _: JsArray | JsNull |
          _: JsBoolean) =>
        SerializerUtil.buildErrorMsg("jsstring", err)
    }
  }

  implicit object Sha256DigestReads extends Reads[Sha256Digest] {

    override def reads(json: JsValue): JsResult[Sha256Digest] =
      SerializerUtil.processJsString[Sha256Digest](Sha256Digest.fromHex)(json)
  }

  implicit object SchnorrPublicKeyReads extends Reads[SchnorrPublicKey] {

    override def reads(json: JsValue): JsResult[SchnorrPublicKey] =
      SerializerUtil.processJsString[SchnorrPublicKey](
        SchnorrPublicKey.fromHex)(json)
  }

  implicit object SchnorrDigitalSignatureReads
      extends Reads[SchnorrDigitalSignature] {

    override def reads(json: JsValue): JsResult[SchnorrDigitalSignature] =
      SerializerUtil.processJsString[SchnorrDigitalSignature](
        SchnorrDigitalSignature.fromHex)(json)
  }

  implicit object SchnorrPublicKeyWrites extends Writes[SchnorrPublicKey] {
    override def writes(o: SchnorrPublicKey): JsValue = JsString(o.hex)
  }

  implicit object Sha256DigestWrites extends Writes[Sha256Digest] {
    override def writes(o: Sha256Digest): JsValue = JsString(o.hex)
  }

  implicit object SchnorrDigitalSignatureWrites
      extends Writes[SchnorrDigitalSignature] {
    override def writes(o: SchnorrDigitalSignature): JsValue = JsString(o.hex)
  }
}

object SerializerUtil extends SerializerUtil
