import sbt.Keys.excludeLintKeys
import xerial.sbt.Sonatype.GitHubHosting

import scala.util.Properties

val scala2_12 = "2.12.15"
val scala2_13 = "2.13.12"

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

ThisBuild / licenses := List(
  "MIT" -> new URL("https://opensource.org/licenses/MIT"))

ThisBuild / homepage := Some(url("https://github.com/benthecarman/scalastr"))

ThisBuild / sonatypeProfileName := "org.scalastr"

ThisBuild / sonatypeProjectHosting := Some(
  GitHubHosting("benthecarman", "scalastr", "benthecarman@live.com"))

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local"

ThisBuild / scalafmtOnCompile := !Properties.envOrNone("CI").contains("true")

ThisBuild / scalaVersion := scala2_13

ThisBuild / crossScalaVersions := List(scala2_13, scala2_12)

ThisBuild / dynverSeparator := "-"

//https://github.com/sbt/sbt/pull/5153
//https://github.com/bitcoin-s/bitcoin-s/pull/2194
Global / excludeLintKeys ++= Set(
  com.typesafe.sbt.packager.Keys.maintainer,
  Keys.mainClass
)
