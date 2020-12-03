package com.joolsf.simpleapp1
import cats.effect.IO

import scala.collection.mutable
import scala.util.Try

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
