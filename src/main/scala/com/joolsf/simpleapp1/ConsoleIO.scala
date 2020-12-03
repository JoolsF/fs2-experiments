package com.joolsf.simpleapp1

import cats.effect.IO

object ConsoleIO {
  def nextInput: IO[String] =
    IO {
      println("\n\n *** Enter next command *** \n\n")
      scala.io.StdIn.readLine
    }

}
