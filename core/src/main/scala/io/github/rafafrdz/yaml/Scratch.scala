package io.github.rafafrdz.yaml

import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, yaml}

object Scratch extends App {

  case class Baz(baz: List[String])
  implicit val fooDecoder: Decoder[Baz] = deriveDecoder
  val something =
    yaml.parser
      .parse(
        """
      baz:
          - hello: true
          - world: 'a'
      """.stripMargin
      )
      .map(_.as[Baz])

  println(something)

}
