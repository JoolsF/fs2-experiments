import StreamRetryV2.stream
import cats.effect.{ExitCode, IO, IOApp}

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

    StreamRetryV1.streamWithRetry
      .take(15)
      .compile
      .toList
      .map(println)
      .map(_ => ExitCode.Success)
      

//    StreamRetryV2.stream
//      .take(3)
//      .compile
//      .toList
//      .map(println)
//      .map(_ => ExitCode.Success)

  }

}
