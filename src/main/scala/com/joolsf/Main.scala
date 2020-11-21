package com.joolsf

import cats.conversions.all.autoWidenFunctor
import cats.effect.{ExitCode, IO, IOApp}
import com.joolsf.retryexamples.StreamRetryV2
import fs2._
import cats.implicits._
import com.joolsf.simpleapp1.SimpleExample1

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

//    StreamRetryV2.stream
//      .take(10)
//      .compile
//      .toList
//      .map(println)
//      .map(_ => ExitCode.Success)

    val se1 = new SimpleExample1
    se1
      .stream
      .compile
      .drain
      .as(ExitCode.Success)



  }

}
