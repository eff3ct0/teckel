package io.github.rafafrdz.teckel.core

trait Format
object Format {
  case object CSV     extends Format
  case object Parquet extends Format
  case object Json    extends Format
  case object Text    extends Format
}
