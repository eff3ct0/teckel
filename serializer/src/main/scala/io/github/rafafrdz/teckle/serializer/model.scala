/*
 * MIT License
 *
 * Copyright (c) 2024 Rafael Fernandez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.rafafrdz.teckle.serializer

import cats.Show
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.github.rafafrdz.teckle.serializer.types.PrimitiveType
import io.github.rafafrdz.teckle.serializer.types.implicits._

object model {

  case class Input(
      name: String,
      format: String,
      path: String,
      options: Map[String, String]
  )

  case class Output(
      name: String,
      format: String,
      mode: String,
      path: String,
      options: Map[String, String]
  )

  @derive(encoder, decoder)
  case class ETL(input: List[Input], output: List[Output])

  /** Decoders */
  implicit val decodeOutput: Decoder[Output] = (c: HCursor) => {
    for {
      name   <- c.downField("name").as[String]
      format <- c.downField("format").as[String]
      mode   <- c.downField("mode").as[String]
      path   <- c.downField("path").as[String]
      optionsRaw <- c
        .downField("options")
        .as[Map[String, PrimitiveType]]
        .orElse(Right(Map.empty[String, PrimitiveType]))
      options = optionsRaw.map { case (k, v) => k -> Show[PrimitiveType].show(v) }

    } yield Output(name, format, mode, path, options)
  }

  implicit val decodeInput: Decoder[Input] = (c: HCursor) => {
    for {
      name   <- c.downField("name").as[String]
      format <- c.downField("format").as[String]
      path   <- c.downField("path").as[String]
      optionsRaw <- c
        .downField("options")
        .as[Map[String, PrimitiveType]]
        .orElse(Right(Map.empty[String, PrimitiveType]))
      options = optionsRaw.map { case (k, v) => k -> Show[PrimitiveType].show(v) }

    } yield Input(name, format, path, options)
  }

  /** Encoders */
  implicit val encodeOutput: Encoder[Output] = (o: Output) => {
    Json.obj(
      "name"    -> Json.fromString(o.name),
      "format"  -> Json.fromString(o.format),
      "mode"    -> Json.fromString(o.mode),
      "path"    -> Json.fromString(o.path),
      "options" -> Json.fromFields(o.options.map { case (k, v) => k -> Json.fromString(v) })
    )
  }

  implicit val encodeInput: Encoder[Input] = (i: Input) => {
    Json.obj(
      "name"    -> Json.fromString(i.name),
      "format"  -> Json.fromString(i.format),
      "path"    -> Json.fromString(i.path),
      "options" -> Json.fromFields(i.options.map { case (k, v) => k -> Json.fromString(v) })
    )
  }

}
