name := "xlspaceship"

version := "0.1.0"

scalaVersion := "2.13.13"

enablePlugins(PlayScala)

import play.sbt.PlayImport._

libraryDependencies ++= Seq(
  guice, // ✅ Enables dependency injection with Guice (required!)
  ws,    // ✅ Play WS client
  "com.typesafe.akka" %% "akka-actor" % "2.6.21",
  "com.typesafe.akka" %% "akka-stream" % "2.6.21",
  "javax.inject" % "javax.inject" % "1",
  "org.scala-lang.modules" %% "scala-xml" % "2.2.0",
  specs2 % Test
)

resolvers ++= Seq(
  "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/",
  Resolver.mavenCentral
)

// Optional: avoid errors from version scheme mismatches
ThisBuild / evictionErrorLevel := Level.Warn
libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % "always"
