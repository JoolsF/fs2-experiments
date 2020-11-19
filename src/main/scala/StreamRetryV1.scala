import java.util.concurrent.atomic.AtomicInteger

import cats.effect.IO._
import cats.effect.{IO, Timer}
import cats.implicits._
import fs2.Stream

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import Util._
object StreamRetryV1 {

  import KafkaIOMock._

  def sleepWithBackOff: Stream[fs2.Pure, FiniteDuration] =
    Stream.unfold(1.second) { s =>
      val delay = (s * 2).min(5.minutes)
      Some(delay -> delay)
    }

  def streamWithAttempts(implicit
      timer: Timer[IO]
  ): Stream[IO, Either[Throwable, String]] =
    Stream
      .repeatEval {
        kafkaIO
      }
      .evalTap(_ => commitOffset())
      .attempts(sleepWithBackOff)
      .repeat // don't really want to do this but just to illustrate one approach

  def streamRecursive(implicit timer: Timer[IO]): Stream[IO, String] =
    Stream
      .repeatEval {
        kafkaIO
      }
      .evalTap(_ => commitOffset())
      .handleErrorWith { e =>
        Stream.eval(
          IO(println(s"Restarting stream with backoff sleep due to error: $e"))
        ) >>
          sleepWithBackOff >>
          streamRecursive

      }

  def streamWithRetry(implicit timer: Timer[IO]): Stream[IO, String] =
    retryProcessing(kafkaIO)
      .flatTap(_ => retryProcessing(commitOffset()))
      .repeat

//  val backoff: FiniteDuration => FiniteDuration = x => (x * 2).min(5.minutes)

  val backoff: FiniteDuration => FiniteDuration = x =>
    (x + 100.milliseconds).min(5.minutes)

  def retryProcessing[A](
      i: IO[A]
  )(implicit timer: Timer[IO]): Stream[IO, A] = {
    Stream.retry[IO, A](
      i,
      1.seconds,
      backoff,
      Int.MaxValue
    )
  }

}
