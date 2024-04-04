package io.github.rafafrdz.teckel.core

sealed trait Source

object Source {
  case class Unknown(name: String)                                                  extends Source
  case class From(name: String, operation: Operation, select: Select, where: Where) extends Source
  type Select = String
  type Where  = String
}
