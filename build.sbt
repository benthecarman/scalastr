enablePlugins(ReproducibleBuildsPlugin,
              JavaAppPackaging,
              GraalVMNativeImagePlugin,
              DockerPlugin,
              WindowsPlugin)

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local"

lazy val scalastr = project
  .in(file("."))
  .aggregate(
    core,
    coreTest,
    client,
    clientTest,
    testkit
  )
  .dependsOn(
    core,
    coreTest,
    nip5,
    nip5Test,
    client,
    clientTest,
    testkit
  )
  .settings(CommonSettings.settings: _*)
  .settings(
    name := "scalastr",
    publish / skip := true
  )

lazy val core = project
  .in(file("core"))
  .settings(CommonSettings.settings: _*)
  .settings(name := "core", libraryDependencies ++= Deps.core)

lazy val coreTest = project
  .in(file("core-test"))
  .settings(CommonSettings.testSettings: _*)
  .settings(name := "core-test", libraryDependencies ++= Deps.coreTest)
  .dependsOn(core)

lazy val nip5 = project
  .in(file("nip5"))
  .settings(CommonSettings.settings: _*)
  .settings(name := "nip5", libraryDependencies ++= Deps.nip5)
  .dependsOn(core)

lazy val nip5Test = project
  .in(file("nip5-test"))
  .settings(CommonSettings.testSettings: _*)
  .settings(name := "nip5-test", libraryDependencies ++= Deps.nip5Test)
  .dependsOn(nip5)

lazy val client = project
  .in(file("client"))
  .settings(CommonSettings.settings: _*)
  .settings(name := "client", libraryDependencies ++= Deps.client)
  .dependsOn(core)

lazy val clientTest = project
  .in(file("client-test"))
  .settings(CommonSettings.testSettings: _*)
  .settings(name := "client-test")
  .dependsOn(client, testkit)

lazy val testkit = project
  .in(file("testkit"))
  .settings(CommonSettings.testSettings: _*)
  .settings(name := "testkit", libraryDependencies ++= Deps.testkit)
  .dependsOn(client)
