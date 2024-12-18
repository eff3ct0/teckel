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
import io.github.rafafrdz.teckle.model.Source.Output
import io.github.rafafrdz.teckle.model.{Asset, Context}
import org.apache.spark.sql.DataFrame

object execution {

  implicit def evalAssetExecution(implicit E: EvalAsset[DataFrame]): EvalAsset[Unit] =
    (context: Context[Asset], asset: Asset) =>
      asset.source match {
        case Output(_, format, mode, options, ref) =>
          EvalAsset[DataFrame]
            .eval(context, asset)
            .write
            .format(format)
            .mode(mode)
            .options(options)
            .save(ref)
        case _ => ()
      }

  implicit def evalContextExecution(implicit E: EvalAsset[Unit]): EvalContext[Unit] =
    (context: Context[Asset]) =>
      context.foreach {
        case (ref, asset @ Asset(_, _: Output)) =>
          ref -> EvalAsset[Unit].eval(context, asset)
        case _ => ()
      }

}
