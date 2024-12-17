package io.github.rafafrdz.yaml

import scala.io.Source

object Example {

  val yaml: String =
    Source.fromFile("core/src/main/resources/etl.yaml").mkString

  def main(args: Array[String]): Unit = {
    println(serializer.parse(yaml))
  }

}
