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

package io.github.rafafrdz.teckle.semantic

import io.github.rafafrdz.teckle.model.Source.{Input, Output}
import io.github.rafafrdz.teckle.model.{Asset, Context}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.annotation.tailrec

object evaluation {

  implicit def evalAssetDebug(implicit S: SparkSession): EvalAsset[DataFrame] =
    new EvalAsset[DataFrame] {
      @tailrec
      override def eval(context: Context[Asset], asset: Asset): DataFrame =
        asset.source match {
          case Input(format, options, ref) =>
            S.read.format(format).options(options).load(ref)
          case Output(assetRef, _,_, _, _) =>
            eval(context, context(assetRef))
        }
    }

  implicit def evalContextDebug[T: EvalAsset]: EvalContext[Context[T]] =
    (context: Context[Asset]) =>
      context.map { case (ref, asset) =>
        ref -> EvalAsset[T].eval(context, asset)
      }
}
