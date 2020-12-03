package com.joolsf.simpleapp1

import cats.effect.{Blocker, IO, Timer}
import fs2.{Stream, io, text}

import java.nio.file.Path
import scala.concurrent.duration.DurationInt

class LogWriterStream(logBuffer: LogBuffer)(implicit t: Timer[IO]) {
  // Blocker provides an ExecutionContext that is intended for executing blocking tasks and integrates directly with ContextShift.
  import scala.concurrent.ExecutionContext.Implicits.global
  private implicit val cs = IO.contextShift(global)

  private val outputFileName: Path =
    java.nio.file.Paths.get("/home/julianfenner/Desktop/log/fs2-app-log.txt")

  val stream =
    Stream
      .resource(Blocker[IO])
      .flatMap { blocker =>
        Stream
          .repeatEval(logBuffer.getNextLog())
          .metered(1000.milliseconds)
          .map(l => l.getOrElse("waiting...").toString)
          .intersperse("\n\n")
          .through(text.utf8Encode)
          .through(io.file.writeAll(outputFileName, blocker))

      }

}
