import sbt.util

name := "matsuri"

version in ThisBuild := "0.1"

scalaVersion in ThisBuild := "2.11.8"

val sparkVersion = "2.2.0"
val kafkaVersion = "1.0.0"
val catsVersion = "1.0.1"
val monixVersion = "3.0.0-M3"

val commonDeps = Seq(
  "com.sksamuel.avro4s" %% "avro4s-core" % "1.8.1",
  "org.apache.logging.log4j" % "log4j-core" % "2.10.0",
  "org.apache.logging.log4j" % "log4j-api" % "2.10.0",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.10.0"
)

val matsuriDeps = Seq(
  "org.apache.bahir" %% "spark-streaming-twitter" % sparkVersion,
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "io.monix" %% "monix" % monixVersion,
  "com.typesafe.akka" %% "akka-stream" % "2.5.9",
  "org.twitter4j" % "twitter4j-core" % "4.0.6",
  "org.twitter4j" % "twitter4j-stream" % "4.0.6",
  "org.twitter4j" % "twitter4j-async" % "4.0.6",
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0" classifier "models"
)

val playDeps = Seq(
  "org.apache.kafka" %% "kafka" % kafkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.18",
  "com.typesafe.play" %% "play" % "2.6.11",
  "com.typesafe.play" %% "play-ws" % "2.6.11",
  "com.typesafe.akka" %% "akka-actor" % "2.5.9"
)

val miniClusterDeps = Seq(
  "org.apache.kafka" %% "kafka" % kafkaVersion,
  "org.apache.curator" % "curator-test" % "2.12.0",
  "org.slf4j" % "slf4j-api" % "1.7.10"
)

assemblyMergeStrategy in ThisBuild := {
  case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.last
  case "UnusedStubClass.class"                                => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "module-info.class" => MergeStrategy.last
  case "module-info.class"                                => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

lazy val root = project.in(new File("."))

lazy val matsuri = project.in(new File("matsuri"))
  .settings(
    name := "Matsuri",
    libraryDependencies ++= matsuriDeps ++ commonDeps,
    logLevel := util.Level.Error
  )

lazy val webserver = project.in(new File("webserver"))
  .settings(
    name := "playServer",
    libraryDependencies ++= playDeps ++ commonDeps,
    libraryDependencies += guice
  )
  .enablePlugins(PlayScala)
  .dependsOn(matsuri)

lazy val miniCluster = project.in(new File("miniCluster"))
  .settings(
    name := "miniCluster",
    libraryDependencies ++= miniClusterDeps
  )

fork in run := true

javaOptions += "-Dlog4j.configuration=log4j.properties"