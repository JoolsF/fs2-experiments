import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {

    StreamRetryV1.stream
      .take(15)
      .compile
      .toList
      .map(println)
      .map(_ => ExitCode.Success)

  }

}
