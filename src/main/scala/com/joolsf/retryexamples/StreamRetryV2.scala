package com.joolsf.retryexamples

import cats.effect.{IO, Timer}
import cats.implicits._
import fs2._

import scala.concurrent.duration.DurationInt

object StreamRetryV2 {
  def logAndRestartOnError[A](implicit t: Timer[IO]): Pipe[IO, A, A] = { in =>
    def restart: Stream[IO, A] = {
      in.handleErrorWith { e =>
        Stream.eval(
          IO(println(s"Restarting stream in 2 seconds due to error: $e"))
        ) *>
          Stream.eval(IO.sleep(2.seconds)) *>
          restart
      }
    }

    restart
  }

  def stream(implicit t: Timer[IO]): Stream[IO, String] =
    Stream
      .repeatEval(KafkaIOMock.kafkaIO)
      .evalTap(_ => KafkaIOMock.commitOffset())
      .through(logAndRestartOnError)

}
