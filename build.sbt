name := "hello-scalatest-scala"

version := "0.3"

scalaVersion := "3.3.3"

scalacOptions += "@.scalacOptions.txt"

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "4.1.0",
  "org.slf4j" % "slf4j-api" % "1.7.36",
  "org.scalatest" %% "scalatest" % "3.2.16" % Test,
  "ch.qos.logback" % "logback-classic" % "1.4.11",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "org.openjfx" % "javafx-controls" % "22.0.1",
  "org.openjfx" % "javafx-fxml" % "22.0.1"
)

// Run in a forked JVM (needed for JavaFX)
fork in run := true

// Set the main class for your project
mainClass in (Compile, run) := Some("TopWordsFunctional.TopWordsFunctional")

// Point to the logback.xml in resources
javaOptions ++= Seq(
  "-Dlogback.configurationFile=src/main/resources/logback.xml"
)

// Enable Java App Packaging
enablePlugins(JavaAppPackaging)

