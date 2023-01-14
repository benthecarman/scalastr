package org.scalastr.core

import play.api.libs.json._

import java.net.URL

case class Metadata private (
    display_name: Option[String],
    name: Option[String],
    about: Option[String],
    nip05: Option[String],
    lud16: Option[String],
    website: Option[String],
    banner: Option[String],
    picture: Option[String]
)

object Metadata {
  implicit val metadataReads: Reads[Metadata] = Json.reads[Metadata]
  implicit val metadataWrites: Writes[Metadata] = Json.writes[Metadata]

  def create(
      displayName: Option[String] = None,
      name: Option[String] = None,
      about: Option[String] = None,
      nip05: Option[String] = None,
      lud16: Option[String] = None,
      website: Option[URL] = None,
      banner: Option[URL] = None,
      picture: Option[URL] = None): Metadata = {
    Metadata(displayName,
             name,
             about,
             nip05,
             lud16,
             website.map(_.toString),
             banner.map(_.toString),
             picture.map(_.toString))
  }
}
