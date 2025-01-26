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

package com.eff3ct.teckel.semantic

import com.eff3ct.teckel.model.Source._
import com.eff3ct.teckel.model._
import com.eff3ct.teckel.semantic.core._
import com.eff3ct.teckel.semantic.sources._
import org.apache.spark.sql._

object evaluation {

  implicit def debug(implicit S: SparkSession): EvalAsset[DataFrame] =
    new EvalAsset[DataFrame] {
      override def eval(context: Context[Asset], asset: Asset): DataFrame = {
        lazy val df: DataFrame = eval(context, context(asset.assetRef))
        asset.source match {
          case s: Input  => Debug.input(s)
          case _: Output => Debug.output(df)
          case s: Transformation =>
            val diffContext: Context[Asset] =
              context.filterNot { case (_, other) => other == asset }
            val others: Context[DataFrame] =
              diffContext.map { case (ref, other) => ref -> eval(diffContext, other) }
            Debug.transformation(s, df, others)
        }
      }
    }

  implicit def debugContext[T: EvalAsset]: EvalContext[Context[T]] =
    (context: Context[Asset]) =>
      context.map { case (ref, asset) =>
        ref -> EvalAsset[T].eval(context, asset)
      }
}
