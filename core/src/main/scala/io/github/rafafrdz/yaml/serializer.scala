package io.github.rafafrdz.yaml

import io.circe.syntax.EncoderOps
import io.circe.yaml.syntax.AsYaml
import io.circe.{Decoder, Encoder, Error}
import io.github.rafafrdz.yaml.AST.ETL

object serializer {
  def deserialize[T: Decoder](value: String): Either[Error, T] =
    io.circe.yaml.parser.parse(value).flatMap(json => json.as[T])
  def serialize[T: Encoder](value: T): String      = value.asJson.asYaml.spaces2
  def parse(value: String): Either[Error, ETL] = deserialize[ETL](value)
  def yaml(value: ETL): String                 = serialize(value)

}
