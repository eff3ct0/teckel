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

package io.github.rafafrdz.teckle.transform

import io.github.rafafrdz.teckle.model.Source.{Input => I, Output => O}
import io.github.rafafrdz.teckle.model.{Asset, Context}
import io.github.rafafrdz.teckle.serializer.model._

object Rewrite {

  def rewrite(options: OptionItem): Map[String, String] =
    List(
      options.header.map(v => "header" -> v.toString),
      options.sep.map(v => "sep" -> v)
    ).flatten.toMap

  def rewrite(item: Input): Asset =
    Asset(item.name, I(item.format, item.options.map(rewrite).getOrElse(Map.empty), item.path))

  def rewrite(item: Output): Asset =
    Asset(
      s"output_${item.name}",
      O(item.name, item.format, item.mode, item.options.map(rewrite).getOrElse(Map.empty), item.path)
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
