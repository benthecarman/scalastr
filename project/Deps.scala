import sbt._

object Deps {

  object V {
    val akkaV = "10.2.10"
    val akkaStreamV = "2.6.20"
    val akkaActorV: String = akkaStreamV

    val bitcoinsV = "1.9.7-31-f8247c42-SNAPSHOT"

    val playV = "2.9.4"

    val testContainersV = "0.40.15"

    val grizzledSlf4jV = "1.3.4"
  }

  object Compile {

    val playJson =
      "com.typesafe.play" %% "play-json" % V.playV withSources () withJavadoc ()

    val akkaHttp =
      "com.typesafe.akka" %% "akka-http" % V.akkaV withSources () withJavadoc ()

    val akkaStream =
      "com.typesafe.akka" %% "akka-stream" % V.akkaStreamV withSources () withJavadoc ()

    val akkaActor =
      "com.typesafe.akka" %% "akka-actor" % V.akkaStreamV withSources () withJavadoc ()

    val akkaSlf4j =
      "com.typesafe.akka" %% "akka-slf4j" % V.akkaStreamV withSources () withJavadoc ()

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
    Compile.akkaActor,
    Compile.akkaHttp,
    Compile.akkaStream,
    Compile.akkaSlf4j
  )

  val nip5Test: List[ModuleID] = List(
    Compile.bitcoinsTestkit
  )

  val client: List[ModuleID] = List(
    Compile.bitcoinsTor,
    Compile.akkaActor,
    Compile.akkaHttp,
    Compile.akkaStream,
    Compile.akkaSlf4j
  )

  val testkit: List[ModuleID] = List(
    Compile.bitcoinsTestkit,
    Compile.grizzledSlf4j,
    Compile.testContainers
  )

}
