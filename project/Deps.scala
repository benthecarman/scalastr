import sbt._

object Deps {

  object V {
    val pekkoHttpV = "1.3.0"
    val pekkoV = "1.6.0"

    val bitcoinsV = "1.9.12"

    val playV = "2.9.4"

    val testContainersV = "0.44.1"

    val grizzledSlf4jV = "1.3.4"
  }

  object Compile {

    val playJson =
      "com.typesafe.play" %% "play-json" % V.playV withSources () withJavadoc ()

    val pekkoHttp =
      "org.apache.pekko" %% "pekko-http" % V.pekkoHttpV withSources () withJavadoc ()

    val pekkoStream =
      "org.apache.pekko" %% "pekko-stream" % V.pekkoV withSources () withJavadoc ()

    val pekkoActor =
      "org.apache.pekko" %% "pekko-actor" % V.pekkoV withSources () withJavadoc ()

    val pekkoSlf4j =
      "org.apache.pekko" %% "pekko-slf4j" % V.pekkoV withSources () withJavadoc ()

    val grizzledSlf4j =
      "org.clapper" %% "grizzled-slf4j" % V.grizzledSlf4jV withSources () withJavadoc ()

    val bitcoinsTor =
      "org.bitcoin-s" %% "bitcoin-s-tor" % V.bitcoinsV withSources () withJavadoc ()

    val bitcoinsTestkitCore =
      "org.bitcoin-s" %% "bitcoin-s-testkit-core" % V.bitcoinsV withSources () withJavadoc ()

    val bitcoinsTestkit =
      "org.bitcoin-s" %% "bitcoin-s-testkit" % V.bitcoinsV withSources () withJavadoc ()

    val bitcoinsCrypto =
      "org.bitcoin-s" %% "bitcoin-s-crypto" % V.bitcoinsV withSources () withJavadoc ()

    val bitcoinsCore =
      "org.bitcoin-s" %% "bitcoin-s-core" % V.bitcoinsV withSources () withJavadoc ()

    val testContainers =
      "com.dimafeng" %% "testcontainers-scala-scalatest" % V.testContainersV withSources () withJavadoc ()
  }

  val core: List[ModuleID] = List(
    Compile.playJson,
    Compile.bitcoinsCrypto,
    Compile.bitcoinsCore
  )

  val coreTest: List[ModuleID] = List(
    Compile.bitcoinsTestkitCore
  )

  val nip5: List[ModuleID] = List(
    Compile.bitcoinsTor,
    Compile.pekkoActor,
    Compile.pekkoHttp,
    Compile.pekkoStream,
    Compile.pekkoSlf4j,
    Compile.grizzledSlf4j
  )

  val nip5Test: List[ModuleID] = List(
    Compile.bitcoinsTestkit
  )

  val client: List[ModuleID] = List(
    Compile.bitcoinsTor,
    Compile.pekkoActor,
    Compile.pekkoHttp,
    Compile.pekkoStream,
    Compile.pekkoSlf4j,
    Compile.grizzledSlf4j
  )

  val testkit: List[ModuleID] = List(
    Compile.bitcoinsTestkit,
    Compile.grizzledSlf4j,
    Compile.testContainers
  )

}
