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
    client,
    clientTest,
    testkit
  )
  .dependsOn(
    client,
    clientTest,
    testkit
  )
  .settings(CommonSettings.settings: _*)
  .settings(
    name := "scalastr",
    publish / skip := true
  )

lazy val client = project
  .in(file("client"))
  .settings(CommonSettings.settings: _*)
  .settings(name := "client", libraryDependencies ++= Deps.client)

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
