/*
 * Invasion Order Software License Agreement
 *
 * This file is part of the proprietary software provided by Invasion Order.
 * Use of this file is governed by the terms and conditions outlined in the
 * Invasion Order Software License Agreement.
 *
 * Unauthorized copying, modification, or distribution of this file, via any
 * medium, is strictly prohibited. The software is provided "as is," without
 * warranty of any kind, express or implied.
 *
 * For the full license text, please refer to the LICENSE file included
 * with this distribution, or contact Invasion Order at contact@iorder.dev.
 *
 * (c) 2024 Invasion Order. All rights reserved.
 */

package io.github.rafafrdz.teckle.serializer

import io.circe._
import io.circe.syntax._
import io.circe.yaml.syntax._

object alternative {
  implicit def json[T: Encoder: Decoder]: Serializer[T] = new Serializer[T] {
    override def encode(value: T): String = value.asJson.spaces2

    override def decode(value: String): Either[Error, T] =
      io.circe.parser.parse(value).flatMap(json => json.as[T])
  }

  implicit def yaml[T: Encoder: Decoder]: Serializer[T] = new Serializer[T] {
    override def encode(value: T): String = value.asJson.asYaml.spaces2

    override def decode(value: String): Either[Error, T] =
      io.circe.yaml.parser.parse(value).flatMap(json => json.as[T])
  }
}
