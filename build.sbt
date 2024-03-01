ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

libraryDependencies ++= Seq(

  "com.typesafe.akka" %% "akka-http" % "10.2.6",
  "com.typesafe.akka" %% "akka-stream" % "2.6.16",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.6",

  "com.typesafe.slick" %% "slick" % "3.3.3",
  "org.postgresql" % "postgresql" % "42.3.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
  "com.github.tminglei" %% "slick-pg" % "0.20.3",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.20.3",

  "com.typesafe.akka" %% "akka-http-testkit" % "10.2.6" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.6.16" % Test,
  "org.scalatest" %% "scalatest" % "3.2.18" % Test,
)

lazy val root = (project in file("."))
  .settings(
    name := "cinema-world"
  )
