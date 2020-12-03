package com.joolsf.simpleapp1

import cats.effect.{IO, Timer}

import java.time.Instant
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.DurationInt
import scala.util.Random.nextInt

object KafkaConsumers {

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
    IO.sleep(nextInt(5000).milliseconds) *>
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
