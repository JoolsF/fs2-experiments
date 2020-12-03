package com.joolsf.simpleapp1

import cats.effect.IO

import scala.collection.mutable.ListBuffer

object ElecMeterReadingDao {

  private var elecMeterReadings = new ListBuffer[ElecMeterReading]

  def save(reading: ElecMeterReading): IO[ElecMeterReading] =
    IO {
      elecMeterReadings += reading
      reading
    }

  def find(customerId: Int): IO[List[ElecMeterReading]] =
    IO(elecMeterReadings.filter(_.c.id == customerId).toList)

  def findAll(): IO[List[ElecMeterReading]] = IO(elecMeterReadings.toList)

}
