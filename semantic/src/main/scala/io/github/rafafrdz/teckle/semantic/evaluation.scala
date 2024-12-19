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
