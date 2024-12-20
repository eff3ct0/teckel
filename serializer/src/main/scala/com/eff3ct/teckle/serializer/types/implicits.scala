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

package com.eff3ct.teckle.serializer.types

import cats.Show
import io.circe.{Decoder, Encoder, Json}
import com.eff3ct.teckle.serializer.types.PrimitiveType._

object implicits {

  implicit val showPrimitiveType: Show[PrimitiveType] = Show.show[PrimitiveType] {
    case StringType(value)  => value
    case CharType(value)    => value.toString
    case BooleanType(value) => value.toString
    case IntegerType(value) => value.toString
    case DoubleType(value)  => value.toString
  }
  implicit val encodePrimitiveType: Encoder[PrimitiveType] = {
    case StringType(value)  => Json.fromString(value)
    case CharType(value)    => Json.fromString(value.toString)
    case BooleanType(value) => Json.fromBoolean(value)
    case IntegerType(value) => Json.fromInt(value)
    case DoubleType(value)  => Json.fromDoubleOrNull(value)
  }

  implicit val decodeStringType: Decoder[StringType] =
    Decoder.decodeString.map(v => StringType(v))

  implicit val decodeCharType: Decoder[CharType] =
    Decoder.decodeChar.map(v => CharType(v))

  implicit val decodeBooleanType: Decoder[BooleanType] =
    Decoder.decodeBoolean.map(v => BooleanType(v))

  implicit val decodeIntegerType: Decoder[IntegerType] =
    Decoder.decodeInt.map(v => IntegerType(v))

  implicit val decodeDoubleType: Decoder[DoubleType] =
    Decoder.decodeDouble.map(v => DoubleType(v))

  implicit val decodePrimitiveType: Decoder[PrimitiveType] =
    Decoder
      .instance { c =>
        c.as[BooleanType]
          .orElse(c.as[IntegerType])
          .orElse(c.as[DoubleType])
          .orElse(c.as[CharType])
          .orElse(c.as[StringType])
      }
}
