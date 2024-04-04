package io.github.rafafrdz.teckel.serializer.model

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

@derive(encoder, decoder)
case class ETLYaml(
    input: List[InputYaml],
    transformation: List[TransformationYaml],
    output: List[OutputYaml]
)

object ETLYaml {
  def empty: ETLYaml = ETLYaml(Nil, Nil, Nil)

  implicit class ImplicitETLYaml(etl: ETLYaml) {
    def addInput(input: List[InputYaml]): ETLYaml =
      etl.copy(input = etl.input ::: input)

    def addOutput(output: List[OutputYaml]): ETLYaml =
      etl.copy(output = etl.output ::: output)

    def addTransformation(transformation: List[TransformationYaml]): ETLYaml =
      etl.copy(transformation = etl.transformation ::: transformation)
  }

}
