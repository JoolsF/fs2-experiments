package com.joolsf

import cats.effect.{ExitCode, IO, IOApp}
import com.joolsf.retryexamples.{StreamRetryV1, StreamRetryV2}

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {

//    StreamRetryV1.streamWithAttempts
//      .take(15)
//      .compile
//      .toList
//      .map(println)
//      .map(_ => ExitCode.Success)

//    StreamRetryV1.streamRecursive
//      .take(15)
//      .compile
//      .toList
//      .map(println)
//      .map(_ => ExitCode.Success)

//    StreamRetryV1.streamWithRetry
//      .take(15)
//      .compile
//      .toList
//      .map(println)
//      .map(_ => ExitCode.Success)

    StreamRetryV2.stream
      .take(10)
      .compile
      .toList
      .map(println)
      .map(_ => ExitCode.Success)

  }

}
