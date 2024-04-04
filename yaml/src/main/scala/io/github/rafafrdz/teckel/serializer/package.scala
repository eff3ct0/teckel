package io.github.rafafrdz.teckel

import io.circe.{Decoder, Encoder, Error}
import io.circe.syntax.EncoderOps
import io.circe.yaml.syntax.AsYaml
import io.github.rafafrdz.teckel.serializer.model.ETLYaml

package object serializer {
  def deserialize[T: Decoder](value: String): Either[Error, T] =
    io.circe.yaml.parser.parse(value).flatMap(json => json.as[T])
  def serialize[T: Encoder](value: T): String      = value.asJson.asYaml.spaces2
  def parse(value: String): Either[Error, ETLYaml] = deserialize[ETLYaml](value)
  def yaml(value: ETLYaml): String                 = serialize(value)

}
