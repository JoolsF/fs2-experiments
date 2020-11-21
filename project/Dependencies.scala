import sbt._

object Dependencies {
  lazy val fs2Kafka = "com.github.fd4s" %% "fs2-kafka" % "1.1.0"
  lazy val vulcan = "com.github.fd4s" %% "fs2-kafka-vulcan" % "1.1.0"
  lazy val fs2Io = "co.fs2" %% "fs2-io" % "2.4.4"
}
