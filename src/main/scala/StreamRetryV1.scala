import java.util.concurrent.atomic.AtomicInteger

import cats.effect.IO._
import cats.effect.{IO, Timer}
import cats.implicits._
import fs2.Stream

import scala.concurrent.duration.DurationInt

object StreamRetryV1 {

  def error: IO[Boolean] = IO(scala.util.Random.nextInt(3) == 2)

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
      .handleErrorWith { e =>
        IO(println(s"Error: $e")) >> IO.raiseError(e)
      }

  }

  def kafkaStream: Stream[IO, String] = {

    val readIO: IO[String] =
      error
        .flatMap {
          case true =>
            IO.raiseError(
              new RuntimeException(s"Read message $i failure")
            )
          case false =>
            IO(println(s"Read message: $i")) >> IO(s"Record: $i")

        }
        .handleErrorWith { e =>
          IO(println(s"Error: $e")) >> IO.raiseError(e)
        }

    Stream.repeatEval {
      readIO
    }
  }

  def stream(implicit timer: Timer[IO]): Stream[IO, Either[Throwable, String]] =
    kafkaStream
      .evalTap(_ => commitOffset())
      .attempts(Stream(500.milliseconds))
      .repeat

}
