package io.github.rafafrdz.teckel.core

sealed trait Operation
object Operation {
  case class Input(format: Format, options: List[Option], path: String)  extends Operation
  case class Output(format: Format, options: List[Option], path: String) extends Operation
  trait Transformation                                                   extends Operation
  case class Option(key: String, value: String)

}
