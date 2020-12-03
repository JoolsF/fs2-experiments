package com.joolsf.simpleapp1

import cats.effect.IO

import scala.collection.mutable.ListBuffer

object CustomerDao {

  private var customers = new ListBuffer[Customer]

  def save(customer: Customer): IO[Customer] =
    IO {
      customers += customer
      customer
    }

  def find(id: Int): IO[Option[Customer]] = IO(customers.find(_.id == id))

  def findAll(): IO[List[Customer]] = IO(customers.toList)

}
