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
import com.eff3ct.teckel.model.Source.{Input => I, Output => O}
import com.eff3ct.teckel.model.{Asset, Context}
import com.eff3ct.teckel.serializer.model._
import com.eff3ct.teckel.serializer.types.PrimitiveType
import com.eff3ct.teckel.serializer.types.implicits._

object Rewrite {

  def rewrite(options: Map[String, PrimitiveType]): Map[String, String] =
    options.map { case (k, v) => k -> Show[PrimitiveType].show(v) }

  def rewrite(item: Input): Asset =
    Asset(item.name, I(item.format, rewrite(item.options), item.path))

  def rewrite(item: Output): Asset =
    Asset(
      s"output_${item.name}",
      O(item.name, item.format, item.mode, rewrite(item.options), item.path)
    )

  def rewrite(item: ETL): Context[Asset] =
    (item.input.map { i =>
      val asset: Asset = rewrite(i)
      asset.assetRef -> asset
    } :::
      item.output.map { i =>
        val asset: Asset = rewrite(i)
        asset.assetRef -> asset
      }).toMap

}
