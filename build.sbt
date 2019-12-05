/*
import java.awt.Desktop
import sbt._
import sbt.Keys._
*/

lazy val ossSnapshots = Resolver.sonatypeRepo("snapshots")
lazy val ossStaging = Resolver.sonatypeRepo("staging")

lazy val buildSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "org.jscala",
  version := "0.5-SNAPSHOT",
  crossScalaVersions := Seq("2.11.12", "2.12.8"),
  scalaVersion := "2.12.8",
  resolvers += Resolver.sonatypeRepo("snapshots"),
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
  publishTo := Some(if (version.value.trim endsWith "SNAPSHOT") ossSnapshots else ossStaging),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := (_ => false),
  pomExtra := extraPom,
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    //      "-Ystatistics",
    //      "-verbose",
    "-language:_"
  )
)

def extraPom =
  <url>https://jscala.org</url>
    <licenses>
      <license>
        <name>MIT</name>
        <url>https://opensource.org/licenses/MIT</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:nau/jscala.git</url>
      <connection>scm:git:git@github.com:nau/jscala.git</connection>
    </scm>
    <developers>
      <developer>
        <id>nau</id>
        <name>Alexander Nemish</name>
        <url>https://github.com/nau</url>
      </developer>
    </developers>

/*val tetris = TaskKey[Unit]("tetris", "Translates tetris Scala code to Javascript and runs the game")

val tetrisTask = tetris := {
  runner.value.run(
    "org.jscalaexample.Tetris",
    Attributed.data(fullClasspath.value),
    Seq((baseDirectory.value / "javascript-tetris" / "tetris.js").toString), streams.value.log)
  Desktop.getDesktop.browse(baseDirectory.value / "javascript-tetris" / "index.html" toURI)
}*/

lazy val root = {
  (project in file("."))
    .settings(
      buildSettings,
      name := "jscala"
    )
    .aggregate(jscala, jscalaAnnots, examples)
}

lazy val jscala = {
  (project in file("jscala"))
    .settings(
      buildSettings,
      name := "jscala-macros",
      libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
      libraryDependencies += "org.scala-js" % "scalajs-library_2.12" % "0.6.28",
      libraryDependencies += "com.yahoo.platform.yui" % "yuicompressor" % "2.4.8" % "provided"
    )
}

lazy val jscalaAnnots = {
  (project in file("jscala-annots"))
    .settings(
      buildSettings,
      name := "jscala-annots",
      libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
    )
    .dependsOn(jscala)
}

lazy val examples = {
  (project in file("jscala-examples"))
    .settings(
      buildSettings,
      name := "jscala-examples",
      /*tetrisTask,*/
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test",
      libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
      libraryDependencies += "org.scala-js" %% "scalajs-dom_sjs0.6" % "0.9.7",
      libraryDependencies += "be.doeraene" %% "scalajs-jquery_sjs0.6" % "0.9.5",
      /*libraryDependencies += "org.querki" %% "jquery-facade_sjs0.6" % "1.2",*/
      libraryDependencies += "com.typesafe.play" %% "play-json" % "2.7.4",
      libraryDependencies += "com.yahoo.platform.yui" % "yuicompressor" % "2.4.8"
    )
    .dependsOn(jscalaAnnots)
}
