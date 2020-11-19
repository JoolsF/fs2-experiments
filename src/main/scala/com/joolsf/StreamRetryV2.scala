package com.joolsf

import cats.effect.{IO, Timer}
import fs2.Stream

import scala.concurrent.duration.DurationInt

object StreamRetryV2 {

  def stream(implicit timer: Timer[IO]): Stream[IO, Either[Throwable, Unit]] = {
    Stream
      .awakeEvery[IO](1.second)
      .evalMap(_ => IO.raiseError(new RuntimeException))
      .handleErrorWith(t =>
        Stream(Left(t)) ++ stream
      ) //put Left to the stream and restart it
  }

}
