package io.github.rafafrdz.teckle.yaml

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

object AST {

  @derive(encoder, decoder)
  case class Input(name: String, format: String, path: String, options: Option[OptionItem])
  @derive(encoder, decoder)
  case class Output(name: String, format: String, path: String, options: Option[OptionItem])
  @derive(encoder, decoder)
  case class ETL(input: List[Input], output: List[Output])


  @derive(encoder, decoder)
  case class OptionItem(header: Option[Boolean], sep: Option[String])

}
