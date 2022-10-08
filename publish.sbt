ThisBuild / organization := "com.scalastr"
ThisBuild / sonatypeProfileName := "com.scalastr"
ThisBuild / organizationName := "benthecarman"

ThisBuild / organizationHomepage := Some(
  url("https://github.com/benthecarman/scalastr/"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/benthecarman/scalastr"),
    "scm:git@github.com:benthecarman/scalastr.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "benthecarman",
    name = "benthecarman",
    email = "benthecarman@live.com",
    url = url("https://github.com/benthecarman/scalastr/")
  )
)

ThisBuild / description := "A barebones scala nostr library"

ThisBuild / licenses := List(
  "MIT" -> new URL("https://opensource.org/licenses/MIT"))
ThisBuild / homepage := Some(url("https://github.com/benthecarman/scalastr/"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }

ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

ThisBuild / dynverSeparator := "-"

ThisBuild / publishMavenStyle := true
