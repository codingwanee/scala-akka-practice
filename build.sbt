ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.4"

lazy val root = (project in file("."))
  .settings(
    name := "untitled"
  )

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.9"

libraryDependencies  ++= Seq (
  // akka actor
  "com.typesafe.akka" %% "akka-actor" % "2.5.29",
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.29",
  "com.typesafe.akka" %% "akka-testkit" % "2.6.8" % Test,
  // akka stream
  "com.typesafe.akka" %% "akka-stream" % "2.6.8",
  // akka http
  "com.typesafe.akka" %% "akka-http" % "10.2.9",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.2.9" % Test,
  // akka-typed
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.8"
)  ++ Seq(
  // h2base
  "com.h2database" % "h2" % "1.3.148",
  // slick
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3"
) ++ Seq(
  // test
  "org.scalatest" %% "scalatest" % "3.2.2" % Test,
) ++ Seq (
  //sprayjson
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.9"
)

//////////////////////////////////////////////////////
