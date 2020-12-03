package com.joolsf.simpleapp1

import cats.effect.{ContextShift, IO, Timer}
import fs2.Stream

import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.util.Try
import scala.util.control.NonFatal

class SimpleFs2Demo1(errorState: mutable.Queue[String], logger: LogBuffer)(
    implicit
    t: Timer[IO],
    c: ContextShift[IO]
) {

  // Meter read stream
  private val meterReadStream: Stream[IO, Unit] =
    Stream
      .repeatEval(readFromElecMeterTopic)
      .evalMap(ElecMeterReadingDao.save)
      .evalTap(res =>
        logger
          .updateBuffer1(InfoLog(res.toString))
      )
      .map(_ => ())
      .metered(2.second)
      .handleErrorWith { error =>
        Stream.eval(
          logger.updateBuffer1(
            ErrorLog(s"Cannot handle error in meterReadStream: $error")
          )
        )
      }

  def readFromElecMeterTopic: IO[ElecMeterReading] = {
    generateError *> KafkaConsumers.elecMeterReadingTopic()
  }.handleErrorWith {
    case NonFatal(error) if error.getMessage == "boom" =>
      logger.updateBuffer1(
        ErrorLog(s"Handling error in readFromElecMeterTopic: $error")
      ) *>
        readFromElecMeterTopic
    case e =>
      logger.updateBuffer1(
        ErrorLog(s"Cannot handle error in readFromElecMeterTopic: $e")
      ) *>
        IO.raiseError(e)

  }

  // Customer stream
  private val newCustomerStream: Stream[IO, Unit] =
    Stream
      .repeatEval(readFromCustomerTopic)
      .evalMap(CustomerDao.save)
      .evalTap(res =>
        logger
          .updateBuffer2(InfoLog(res.toString))
      )
      .map(_ => ())
      .metered(2.second)

  def readFromCustomerTopic: IO[Customer] = {
    KafkaConsumers.newCustomerTopic()
  }

  // Log writer stream
  private val logWriterStream: Stream[IO, Unit] = {
    new LogWriterStream(logger)
  }.stream

  // Console input stream
  private val consoleStream =
    Stream
      .repeatEval(ConsoleIO.nextInput)
      .evalTap {
        case "customers" =>
          CustomerDao
            .findAll()
            .map(_ mkString "\n")
            .map(print)
        case "readings" =>
          ElecMeterReadingDao
            .findAll()
            .map(_ mkString "\n")
            .map(print)
        case "boom" =>
          errorState.enqueue("boom")
          IO.unit
        case "bang" =>
          errorState.enqueue("bang")
          IO.unit
        case _ => IO.unit
      }
      .map(_ => ())

  private def generateError: IO[Unit] =
    IO(Try(errorState.dequeue()).toOption).flatMap {
      case Some(error) =>
        IO.raiseError(new RuntimeException(error))
      case None =>
        IO.unit
    }

  // Stream
  val stream: Stream[IO, Unit] =
    Stream(
      meterReadStream,
      newCustomerStream,
      logWriterStream,
      consoleStream
    ).parJoinUnbounded

}

object SimpleFs2Demo1 {

  def apply(implicit t: Timer[IO], c: ContextShift[IO]): SimpleFs2Demo1 = {
    val errorState: mutable.Queue[String] = new mutable.Queue[String]()
    val logger: LogBuffer =
      new LogBuffer(new mutable.Queue[Log], new mutable.Queue[Log])

    new SimpleFs2Demo1(errorState, logger)

  }
}
