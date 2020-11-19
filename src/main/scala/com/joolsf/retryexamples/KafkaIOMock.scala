package com.joolsf.retryexamples

import java.util.concurrent.atomic.AtomicInteger

import cats.Applicative.ops.toAllApplicativeOps
import com.joolsf.Util._
import cats.effect.{IO, Timer}

object KafkaIOMock {

  private def error: IO[Boolean] = IO(scala.util.Random.nextInt(7) == 6)

  private val i = new AtomicInteger(1)

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
          IO(println(s"Read message: $i")) *> IO(s"Record: $i")

      }

  }.logErrorAndThrow()

}
