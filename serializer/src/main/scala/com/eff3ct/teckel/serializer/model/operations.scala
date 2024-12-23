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

import cats.data.NonEmptyList
import cats.implicits._
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.syntax._
import io.circe.{Decoder, Encoder}

object operations {

  sealed trait Operation

  implicit val encodeEvent: Encoder[Operation] =
    Encoder.instance {
      case s: SelectOp  => s.asJson
      case w: WhereOp   => w.asJson
      case g: GroupByOp => g.asJson
      case o: OrderByOp => o.asJson
    }

  implicit val decodeEvent: Decoder[Operation] =
    List[Decoder[Operation]](
      Decoder[SelectOp].widen,
      Decoder[WhereOp].widen,
      Decoder[GroupByOp].widen,
      Decoder[OrderByOp].widen
    ).reduceLeft(_ or _)

  @derive(encoder, decoder)
  case class SelectOp(from: String, columns: NonEmptyList[String]) extends Operation
  @derive(encoder, decoder)
  case class WhereOp(from: String, filter: String) extends Operation
  @derive(encoder, decoder)
  case class GroupByOp(from: String, by: NonEmptyList[String], agg: NonEmptyList[String])
      extends Operation
  @derive(encoder, decoder)
  case class OrderByOp(from: String, by: NonEmptyList[String], order: Option[String])
      extends Operation

}
