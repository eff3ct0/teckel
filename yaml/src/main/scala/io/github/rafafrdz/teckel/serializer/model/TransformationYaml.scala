package io.github.rafafrdz.teckel.serializer.model

import cats.syntax.functor._
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.{Decoder, Encoder}
import io.circe.syntax._

sealed trait TransformationYaml
object TransformationYaml {

  @derive(encoder, decoder)
  case class JoinTransformationYaml(name: String, join: JoinOperationYaml)
      extends TransformationYaml

  @derive(encoder, decoder)
  case class JoinOperationYaml(joinType: String, relation: JoinRelationYaml)
  @derive(encoder, decoder)
  case class JoinRelationYaml(left: String, right: List[JoinSourceYaml])
  @derive(encoder, decoder)
  case class JoinSourceYaml(name: String, fields: Map[String, String])

  /**
   * ADTs encoding and decoding:
   * https://circe.github.io/circe/codecs/adt.html#adts-encoding-and-decoding
   */

  implicit val transformationYamlEncoder: Encoder[TransformationYaml] =
    Encoder.instance[TransformationYaml] { case join @ JoinTransformationYaml(_, _) =>
      join.asJson
    // add the rest here
    }

  implicit val transformationYamlDecoder: Decoder[TransformationYaml] =
    List[Decoder[TransformationYaml]](
      Decoder[JoinTransformationYaml].widen
        // add the rest here
    ).reduceLeft(_ or _)

}
