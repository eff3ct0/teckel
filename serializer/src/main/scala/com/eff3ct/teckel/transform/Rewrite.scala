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

package com.eff3ct.teckel.transform

import cats.Show
import cats.data.NonEmptyList
import com.eff3ct.teckel.model.{Asset, Context, Source}
import com.eff3ct.teckel.serializer.model.etl._
import com.eff3ct.teckel.serializer.model.input._
import com.eff3ct.teckel.serializer.model.operations.Relation
import com.eff3ct.teckel.serializer.model.output._
import com.eff3ct.teckel.serializer.model.transformation._
import com.eff3ct.teckel.serializer.types.PrimitiveType
import com.eff3ct.teckel.serializer.types.implicits._

object Rewrite {

  def rewrite(options: Map[String, PrimitiveType]): Map[String, String] =
    options.map { case (k, v) => k -> Show[PrimitiveType].show(v) }

  def rewrite(item: Input): Asset =
    Asset(item.name, Source.Input(item.format, rewrite(item.options), item.path))

  def rewrite(item: Output): Asset =
    Asset(
      s"output_${item.name}",
      Source.Output(item.name, item.format, item.mode, rewrite(item.options), item.path)
    )

  def rewriteOp(item: Select): Asset =
    Asset(item.name, Source.Select(item.select.from, item.select.columns))

  def rewriteOp(item: Where): Asset =
    Asset(item.name, Source.Where(item.where.from, item.where.filter))

  def rewriteOp(item: GroupBy): Asset =
    Asset(item.name, Source.GroupBy(item.group.from, item.group.by, item.group.agg))

  def rewriteOp(item: OrderBy): Asset =
    Asset(item.name, Source.OrderBy(item.order.from, item.order.by, item.order.order))

  def rewriteOp(item: Join): Asset =
    Asset(item.name, Source.Join(item.join.left, item.join.right.map(rewriteOp)))

  def rewriteOp(item: Relation): Source.Relation =
    Source.Relation(item.name, item.relationType, item.on)

  def rewrite(item: Transformation): Asset =
    item match {
      case s: Select  => rewriteOp(s)
      case s: Where   => rewriteOp(s)
      case s: GroupBy => rewriteOp(s)
      case s: OrderBy => rewriteOp(s)
      case s: Join    => rewriteOp(s)
    }

  def icontext(item: NonEmptyList[Input]): Context[Asset] =
    item
      .map { i =>
        val asset: Asset = rewrite(i)
        asset.assetRef -> asset
      }
      .toList
      .toMap

  def ocontext(item: NonEmptyList[Output]): Context[Asset] =
    item
      .map { o =>
        val asset: Asset = rewrite(o)
        asset.assetRef -> asset
      }
      .toList
      .toMap

  def tcontext(item: Option[NonEmptyList[Transformation]]): Context[Asset] =
    (for {
      transformation <- item
      context = transformation.map { t =>
        val asset: Asset = rewrite(t)
        asset.assetRef -> asset
      }
    } yield context.toList.toMap).getOrElse(Map())

  def rewrite(item: ETL): Context[Asset] =
    icontext(item.input) ++ ocontext(item.output) ++ tcontext(item.transformation)

}
