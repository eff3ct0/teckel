package io.github.rafafrdz.teckel.serializer.model

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

@derive(encoder, decoder)
case class OutputYaml(
    name: String,
    format: String,
    options: Map[String, String],
    path: String
)
