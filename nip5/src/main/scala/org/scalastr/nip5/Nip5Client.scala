package org.scalastr.nip5

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.HttpRequest
import akka.util.ByteString
import grizzled.slf4j.Logging
import org.bitcoins.crypto.SchnorrPublicKey
import org.scalastr.core.NostrPublicKey
import play.api.libs.json.Json

import scala.concurrent._

class Nip5Client()(implicit system: ActorSystem) extends Logging {
  implicit val ec: ExecutionContext = system.dispatcher
  private val http: HttpExt = Http(system)

  private def sendRequest(request: HttpRequest): Future[String] = {
    http
      .singleRequest(request)
      .flatMap(response =>
        response.entity.dataBytes
          .runFold(ByteString.empty)(_ ++ _))
      .map(payload => payload.decodeString(ByteString.UTF_8))
  }

  def getPublicKey(nip5: Nip5Address): Future[NostrPublicKey] = {
    sendRequest(Get(nip5.verifyUrl)).map { str =>
      val json = Json.parse(str)
      val res = json \ "names" \ nip5.user

      NostrPublicKey(SchnorrPublicKey(res.as[String]))
    }
  }
}
