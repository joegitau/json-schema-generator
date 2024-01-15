import scala.collection.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.3"

lazy val pekkoVersion = "1.0.1"
lazy val pekkoHttpVersion = "1.0.0"
lazy val playJsonVersion = "3.0.0"

lazy val root = (project in file("."))
  .settings(
    name := "generic-playground",
    libraryDependencies ++= Seq(
      // akka
      "org.apache.pekko"       %% "pekko-actor"                 % pekkoVersion,
      "org.apache.pekko"       %% "pekko-stream"                % pekkoVersion,
      "org.apache.pekko"       %% "pekko-slf4j"                 % pekkoVersion,
      "org.apache.pekko"       %% "pekko-serialization-jackson" % pekkoVersion,
      "org.apache.pekko"       %% "pekko-http"                  % pekkoHttpVersion,
      // serializers
      "org.playframework"      %% "play-json"                   % playJsonVersion,
      "org.typelevel"          %% "cats-core"                   % "2.10.0",
      // tests
      "org.scalatest"          %% "scalatest"                   % "3.2.17" % Test,
      "org.scalatestplus.play" %% "scalatestplus-play"          % "7.0.0" % Test,
    )
  )
