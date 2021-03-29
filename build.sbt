
// Building both for JVM and JavaScript runtimes.

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val scalaVer = "2.13.4"
val crossScalaVer = Seq(scalaVer)

lazy val commonSettings = Seq(
  name         := "dutch-election",
  description  := "Analysis of the 2021 Dutch 'Tweede Kamer' election data",
  organization := "eu.cdevreeze.dutch-election",
  version      := "0.1.0-SNAPSHOT",

  scalaVersion       := scalaVer,
  crossScalaVersions := crossScalaVer,

  scalacOptions ++= Seq("-Wconf:cat=unused-imports:w,cat=unchecked:w,cat=deprecation:w,cat=feature:w,cat=lint:w"),

  Test / publishArtifact := false,
  publishMavenStyle := true,

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value) {
      Some("snapshots" at nexus + "content/repositories/snapshots")
    } else {
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
  },

  pomExtra := pomData,
  pomIncludeRepository := { _ => false },

  libraryDependencies += "eu.cdevreeze.yaidom2" %%% "yaidom2" % "0.13.0"
)

lazy val root = project.in(file("."))
  .aggregate(dutchElectionJVM, dutchElectionJS)
  .settings(commonSettings: _*)
  .settings(
    name                 := "dutch-election",
    // Thanks, scala-java-time, for showing us how to prevent any publishing of root level artifacts:
    // No, SBT, we don't want any artifacts for root. No, not even an empty jar.
    publish              := {},
    publishLocal         := {},
    publishArtifact      := false,
    Keys.`package`       := file(""))

lazy val dutchElection = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .settings(commonSettings: _*)
  .jvmSettings(
    libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.1",
    libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.7"
  )
  .jsSettings(
    // Do we need this jsEnv?
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(),

    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.0.0",

    Test / parallelExecution := false,
  )

lazy val dutchElectionJVM = dutchElection.jvm

lazy val dutchElectionJS = dutchElection.js

lazy val pomData =
  <url>https://github.com/dvreeze/dutch-election</url>
  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
      <comments>Dutch-election is licensed under Apache License, Version 2.0</comments>
    </license>
  </licenses>
  <scm>
    <connection>scm:git:git@github.com:dvreeze/dutch-election.git</connection>
    <url>https://github.com/dvreeze/dutch-election.git</url>
    <developerConnection>scm:git:git@github.com:dvreeze/dutch-election.git</developerConnection>
  </scm>
  <developers>
    <developer>
      <id>dvreeze</id>
      <name>Chris de Vreeze</name>
      <email>chris.de.vreeze@caiway.net</email>
    </developer>
  </developers>
