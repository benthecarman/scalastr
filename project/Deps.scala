import sbt._

object Deps {

  object V {
    val bitcoinsV = "1.9.2-195-2cae3f80-SNAPSHOT"

    val grizzledSlf4jV = "1.3.4"
  }

  object Compile {

    val grizzledSlf4j =
      "org.clapper" %% "grizzled-slf4j" % V.grizzledSlf4jV withSources () withJavadoc ()

    val bitcoinsKeyManager =
      "org.bitcoin-s" %% "bitcoin-s-key-manager" % V.bitcoinsV withSources () withJavadoc ()

    val bitcoinsTestkit =
      "org.bitcoin-s" %% "bitcoin-s-testkit" % V.bitcoinsV withSources () withJavadoc ()

    val bitcoinsAppCommons =
      "org.bitcoin-s" %% "bitcoin-s-app-commons" % V.bitcoinsV withSources () withJavadoc ()

    val bitcoinsDbCommons =
      "org.bitcoin-s" %% "bitcoin-s-db-commons" % V.bitcoinsV withSources () withJavadoc ()
  }

  val client: List[ModuleID] = List(
    Compile.bitcoinsKeyManager,
    Compile.bitcoinsAppCommons,
    Compile.bitcoinsDbCommons
  )

  val testkit: List[ModuleID] =
    List(Compile.bitcoinsTestkit, Compile.grizzledSlf4j)

}
