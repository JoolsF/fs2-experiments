import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration

import Util._
import cats.effect.IO
import cats.effect.IO._
import cats.effect.Timer
import cats.implicits._
import fs2.Stream

object KafkaIOMock {

  def error: IO[Boolean] = IO(scala.util.Random.nextInt(5) == 4)

  val i = new AtomicInteger(1)

  def commitOffset(): IO[Unit] = {
    val commit = IO(i.getAndIncrement())

    error
      .flatMap {
        case true =>
          IO.raiseError(
            new RuntimeException(s"Commit message failure")
          )
        case false =>
          commit.flatMap(i => IO(println(s"Committed message $i\n")))
      }
  }.logErrorAndThrow()

  def kafkaIO(implicit t: Timer[IO]): IO[String] = {
    error
      .flatMap {
        case true =>
          IO.raiseError(
            new RuntimeException(s"Read message $i failure")
          )
        case false =>
          IO(println(s"Read message: $i")) >> IO(s"Record: $i")

      }

  }.logErrorAndThrow()

  

}
