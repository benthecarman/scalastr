import com.typesafe.sbt.packager.Keys.maintainer
import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype.autoImport._

import java.nio.file._
import scala.util.Properties

object CommonSettings {

  lazy val settings: Vector[Setting[_]] = Vector(
    scalaVersion := "2.13.12",
    organization := "org.scalastr",
    homepage := Some(url("https://github.com/benthecarman/scalastr")),
    maintainer.withRank(
      KeyRanks.Invisible) := "benthecarman <benthecarman@live.com>",
    developers := List(
      Developer(
        "benthecarman",
        "Ben Carman",
        "benthecarman@live.com",
        url("https://twitter.com/benthecarman")
      )
    ),
    publishTo := {
      val nexus = "https://s01.oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org",
    ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    Compile / scalacOptions ++= compilerOpts(scalaVersion = scalaVersion.value),
    Test / scalacOptions ++= testCompilerOpts(scalaVersion =
      scalaVersion.value),
    Compile / console / scalacOptions ~= (_ filterNot (s =>
      s == "-Ywarn-unused-import"
        || s == "-Ywarn-unused"
        || s == "-Xfatal-warnings"
        // for 2.13 -- they use different compiler opts
        || s == "-Xlint:unused")),
    Test / console / scalacOptions ++= (Compile / console / scalacOptions).value,
    Test / scalacOptions ++= testCompilerOpts(scalaVersion.value),
    licenses += ("MIT", url("https://opensource.org/licenses/MIT")),
    resolvers ++= Resolver.sonatypeOssRepos("snapshots")
  )

  private val commonCompilerOpts = {
    List(
      // https://stackoverflow.com/a/43103038/967713
      "-release",
      "8"
    )
  }

  /** Linting options for scalac */
  private val scala2_13CompilerLinting = {
    Seq(
      "-Xlint:unused",
      "-Xlint:adapted-args",
      "-Xlint:nullary-unit",
      "-Xlint:inaccessible",
      "-Xlint:infer-any",
      "-Xlint:missing-interpolator",
      "-Xlint:eta-sam"
    )
  }

  /** Compiler options for source code */
  private val scala2_13SourceCompilerOpts = {
    Seq("-Xfatal-warnings") ++ scala2_13CompilerLinting
  }

  private val nonScala2_13CompilerOpts = Seq(
    "-Xmax-classfile-name",
    "128",
    "-Ywarn-unused",
    "-Ywarn-unused-import"
  )

  // https://docs.scala-lang.org/overviews/compiler-options/index.html
  def compilerOpts(scalaVersion: String): Seq[String] = {
    Seq(
      "-unchecked",
      "-feature",
      "-deprecation",
      "-Ywarn-dead-code",
      "-Ywarn-value-discard",
      "-Ywarn-unused",
      "-unchecked",
      "-deprecation",
      "-feature",
      "-Ypatmat-exhaust-depth",
      "off"
    ) ++ commonCompilerOpts ++ {
      if (scalaVersion.startsWith("2.13")) {
        scala2_13SourceCompilerOpts
      } else nonScala2_13CompilerOpts
    }
  }

  def testCompilerOpts(scalaVersion: String): Seq[String] = {
    (commonCompilerOpts ++
      // initialization checks: https://docs.scala-lang.org/tutorials/FAQ/initialization-order.html
      Vector("-Xcheckinit") ++
      compilerOpts(scalaVersion))
      .filterNot(_ == "-Xfatal-warnings")
  }

  lazy val testSettings: Seq[Setting[_]] = Seq(
    // show full stack trace (-oF) of failed tests and duration of tests (-oD)
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"),
    Test / logBuffered := false,
    skip / publish := true
  ) ++ settings

  lazy val prodSettings: Seq[Setting[_]] = settings

  lazy val binariesPath: Path =
    Paths.get(Properties.userHome, ".bitcoin-s", "binaries")
}
