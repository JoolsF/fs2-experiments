import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1"
  lazy val fs2Kafka = "com.github.fd4s" %% "fs2-kafka" % "1.0.0"
  lazy val vulcan = "com.github.fd4s" %% "fs2-kafka-vulcan" % "1.0.0"
}