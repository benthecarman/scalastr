import sbt.Keys.excludeLintKeys

import scala.util.Properties

val scala2_13 = "2.13.18"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/benthecarman/scalastr"),
    "scm:git@github.com:benthecarman/scalastr.git"
  )
)

ThisBuild / developers := List(
  Developer(
    "benthecarman",
    "benthecarman",
    "benthecarman@live.com",
    url("https://twitter.com/benthecarman")
  )
)

ThisBuild / organization := "org.scalastr"

ThisBuild / organizationName := "scalastr"

ThisBuild / organizationHomepage := Some(
  url("https://github.com/benthecarman/scalastr"))

ThisBuild / licenses := List(
  "MIT" -> url("https://opensource.org/licenses/MIT"))

ThisBuild / homepage := Some(url("https://github.com/benthecarman/scalastr"))

ThisBuild / description := "A barebones Scala Nostr library"

// Maven Central POMs must not refer consumers to additional repositories.
ThisBuild / pomIncludeRepository := { _ => false }

ThisBuild / scalafmtOnCompile := !Properties.envOrNone("CI").contains("true")

ThisBuild / scalaVersion := scala2_13

ThisBuild / crossScalaVersions := List(scala2_13)

ThisBuild / dynverSeparator := "-"

//https://github.com/sbt/sbt/pull/5153
//https://github.com/bitcoin-s/bitcoin-s/pull/2194
Global / excludeLintKeys ++= Set(
  Keys.mainClass
)
