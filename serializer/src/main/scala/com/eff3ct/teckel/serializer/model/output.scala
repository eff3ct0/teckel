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

package com.eff3ct.teckel.serializer.model

import com.eff3ct.teckel.serializer.types.PrimitiveType
import com.eff3ct.teckel.serializer.types.implicits._
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder, HCursor}

object output {

  case class Output(
      name: String,
      format: String,
      mode: String,
      path: String,
      options: Map[String, PrimitiveType]
  )

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

  implicit val encodeOutput: Encoder[Output] = deriveEncoder[Output]

  case class StreamingOutput(
      name: String,
      format: String,
      options: Map[String, PrimitiveType] = Map.empty,
      path: Option[String] = None,
      outputMode: Option[String] = None,
      checkpointLocation: Option[String] = None,
      trigger: Option[String] = None
  )

  implicit val decodeStreamingOutput: Decoder[StreamingOutput] = (c: HCursor) => {
    for {
      name   <- c.downField("name").as[String]
      format <- c.downField("format").as[String]
      options <- c
        .downField("options")
        .as[Map[String, PrimitiveType]]
        .orElse(Right(Map.empty[String, PrimitiveType]))
      path               <- c.downField("path").as[Option[String]]
      outputMode         <- c.downField("outputMode").as[Option[String]]
      checkpointLocation <- c.downField("checkpointLocation").as[Option[String]]
      trigger            <- c.downField("trigger").as[Option[String]]
    } yield StreamingOutput(name, format, options, path, outputMode, checkpointLocation, trigger)
  }

  implicit val encodeStreamingOutput: Encoder[StreamingOutput] = deriveEncoder[StreamingOutput]
}
