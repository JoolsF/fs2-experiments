package com.joolsf.simpleapp1

import java.time.Instant

case class Customer(id: Int) {
  override def toString: String = {
    s"Customer: $id"
  }
}

case class ElecMeterReading(c: Customer, reading: Int, readingTime: Instant) {
  override def toString: String =
    s"ElecMeterReading: $c reading: $reading readingTime: $readingTime"
}

trait Log {
  val level: String
  val msg: String
  val time: Instant = Instant.now()
  override def toString: String = {
    s"$level - timestamp: $time  message: $msg"
  }

}
case class InfoLog(msg: String) extends Log {
  override val level: String = "Info"
}

case class ErrorLog(msg: String) extends Log {
  override val level: String = "Error"
}
