package com.joolsf.simpleapp1

import java.nio.file.Path
import java.time.Instant

import cats.effect.{Blocker, ContextShift, IO, Timer}
import fs2.{Stream, io, text}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.DurationInt
import scala.util.Random.nextInt
import scala.util.Try

case class Customer(id: Int)
case class ElecMeterReading(c: Customer, reading: Int, readingTime: Instant)
case class Log(msg: String, time: Instant = Instant.now())

//VERY toy example - added two buffers to avoid having to deal with concurrent access for each buffer
class LogBuffer(
    private val buffer1: mutable.Queue[Log],
    private val buffer2: mutable.Queue[Log]
) {

  private val buffer = new mutable.Queue[Log]

  def updateBuffer1(s: Log): IO[Unit] =
    IO {
      buffer1.enqueue(s)
      ()
    }

  def updateBuffer2(s: Log): IO[Unit] =
    IO {
      buffer2.enqueue(s)
      ()
    }

  private def flushToMainBuffer(): mutable.Seq[Log] = {

    implicit val logOrder: Ordering[Log] =
      Ordering.fromLessThan(_.time isAfter _.time)

    val logs = List(
      Try(buffer1.dequeue()).toOption,
      Try(buffer2.dequeue()).toOption
    ).flatten.sorted

    buffer
      .enqueueAll(logs)
      .sorted

  }

  def getNextLog(): IO[Option[Log]] =
    IO {
      flushToMainBuffer()
      Try(buffer.dequeue()).toOption
    }

}
class SimpleExample1(implicit t: Timer[IO], c: ContextShift[IO]) {

  private val errorState: mutable.Queue[Unit] = new mutable.Queue[Unit]()

  def generateError: IO[Unit] =
    IO(Try(errorState.dequeue()).toOption).flatMap {
      case Some(_) =>
        IO.raiseError(new RuntimeException("error"))
      case None =>
        IO.unit
    }

  private val logBuffer =
    new LogBuffer(new mutable.Queue[Log], new mutable.Queue[Log])

  private val meterReadStream: Stream[IO, Unit] =
    Stream
      .repeatEval(generateError *> KafkaClient.elecMeterReadingTopic())
      .evalMap(ElecMeterReadingDao.save)
      .evalTap(res =>
        logBuffer
          .updateBuffer1(Log(res.toString))
      )
      .map(_ => ())
      .metered(1.second)

  private val newCustomerStream: Stream[IO, Unit] =
    Stream
      .repeatEval(generateError *> KafkaClient.newCustomerTopic())
      .evalMap(CustomerDao.save)
      .evalTap(res =>
        logBuffer
          .updateBuffer2(Log(res.toString))
      )
      .map(_ => ())
      .metered(1.second)

  private val logWriterStream = new LogWriterStream(logBuffer)

  private val consoleStream =
    Stream
      .repeatEval(ConsoleIO.nextInput)
      .evalTap {
        case "error" =>
          println("Generating error")
          errorState.enqueue(())
          IO.unit
        case "" => IO(())
      }
      .map(_ => ())

  val stream: Stream[IO, Unit] =
    Stream(
      meterReadStream,
      newCustomerStream,
      logWriterStream.stream,
      consoleStream
    ).parJoinUnbounded

}
object ConsoleIO {
  def nextInput: IO[String] =
    IO {
      println("Enter next command")
      scala.io.StdIn.readLine
    }

}

object KafkaClient {

  def elecMeterReadingTopic()(implicit t: Timer[IO]): IO[ElecMeterReading] =
    IO.sleep(nextInt(5000).milliseconds) *>
      IO {
        ElecMeterReading(
          c = Customer(nextInt(150)),
          reading = nextInt(1000),
          Instant.now
        )
      }
  var customers = new ListBuffer[Customer]
  for (id <- 1 to 100) yield customers += Customer(id)

  def newCustomerTopic()(implicit t: Timer[IO]): IO[Customer] = {
    IO.sleep(nextInt(12000).milliseconds) *>
      IO {
        customers.headOption match {
          case Some(customer) =>
            customers -= customer
            customer
          case None => Customer(-1)
        }
      }
  }

}

object CustomerDao {

  private var customers = new ListBuffer[Customer]
  def save(customer: Customer): IO[Customer] =
    IO {
      customers += customer
      customer
    }

  def find(id: Int): IO[Option[Customer]] = IO(customers.find(_.id == id))

}

object ElecMeterReadingDao {

  private var elecMeterReadings = new ListBuffer[ElecMeterReading]
  def save(reading: ElecMeterReading): IO[ElecMeterReading] =
    IO {
      elecMeterReadings += reading
      reading
    }

  def find(customerId: Int): IO[List[ElecMeterReading]] =
    IO(elecMeterReadings.filter(_.c.id == customerId).toList)

}

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
          .metered(2.seconds)
          .map(l => l.getOrElse(Log("tick")).toString)
          .intersperse("\n")
          .through(text.utf8Encode)
          .through(io.file.writeAll(outputFileName, blocker))

      }

}

object Error {

  def error(chance: Int): Boolean = nextInt(chance) == chance - 1

}
