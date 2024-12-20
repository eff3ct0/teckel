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

package com.eff3ct.teckle.serializer

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.{Decoder, HCursor}
import com.eff3ct.teckle.serializer.types.PrimitiveType
import com.eff3ct.teckle.serializer.types.implicits._

object model {

  @derive(encoder)
  case class Input(
      name: String,
      format: String,
      path: String,
      options: Map[String, PrimitiveType]
  )

  @derive(encoder)
  case class Output(
      name: String,
      format: String,
      mode: String,
      path: String,
      options: Map[String, PrimitiveType]
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
      options <- c
        .downField("options")
        .as[Map[String, PrimitiveType]]
        .orElse(Right(Map.empty[String, PrimitiveType]))

    } yield Output(name, format, mode, path, options)
  }

  implicit val decodeInput: Decoder[Input] = (c: HCursor) => {
    for {
      name   <- c.downField("name").as[String]
      format <- c.downField("format").as[String]
      path   <- c.downField("path").as[String]
      options <- c
        .downField("options")
        .as[Map[String, PrimitiveType]]
        .orElse(Right(Map.empty[String, PrimitiveType]))

    } yield Input(name, format, path, options)
  }

}
