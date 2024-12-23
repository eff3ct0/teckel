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

import cats.implicits._
import com.eff3ct.teckel.serializer.model.operations._
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.syntax._
import io.circe.{Decoder, Encoder}

object transformation {
  sealed trait Transformation

  implicit val encodeEvent: Encoder[Transformation] =
    Encoder.instance {
      case s: Select  => s.asJson
      case w: Where   => w.asJson
      case g: GroupBy => g.asJson
      case o: OrderBy => o.asJson
    }

  implicit val decodeEvent: Decoder[Transformation] =
    List[Decoder[Transformation]](
      Decoder[Select].widen,
      Decoder[Where].widen,
      Decoder[GroupBy].widen,
      Decoder[OrderBy].widen
    ).reduceLeft(_ or _)

  @derive(encoder, decoder)
  case class Select(name: String, select: SelectOp) extends Transformation
  @derive(encoder, decoder)
  case class Where(name: String, where: WhereOp) extends Transformation
  @derive(encoder, decoder)
  case class GroupBy(name: String, group: GroupByOp) extends Transformation
  @derive(encoder, decoder)
  case class OrderBy(name: String, order: OrderByOp) extends Transformation
}
