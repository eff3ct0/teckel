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

import scala.collection.mutable.{Map => MMap}

object evaluation {

  private def register(
      context: Mutex[DataFrame],
      a: Asset,
      df: DataFrame
  ): DataFrame = {
    context.put(a.assetRef, df)
    df
  }

  implicit def debug(implicit S: SparkSession): EvalAsset[DataFrame] =
    new EvalAsset[DataFrame] {
      val global: Mutex[DataFrame] = MMap()
      override def eval(context: Context[Asset], asset: Asset): DataFrame = {
        val registerCallBack: DataFrame => DataFrame     = register(global, asset, _)
        val getTableCallBack: Asset => Option[DataFrame] = a => global.get(a.assetRef)
        resolveAndRegister(context, asset, eval, getTableCallBack, registerCallBack)
      }
    }

  def resolve(
      context: Context[Asset],
      asset: Asset,
      getOrEval: (Context[Asset], Asset) => DataFrame
  )(implicit S: SparkSession): DataFrame =
    asset.source match {
      case s: Input => Debug.input(s).as(asset.assetRef)

      case s: Output =>
        val inner  = getOrEval(context, context(s.assetRef))
        val result = Debug.output(inner).as(asset.assetRef)
        result

      case s: Transformation =>
        lazy val diffContext: Context[Asset] =
          context.filterNot { case (ref, _) => ref == asset.assetRef }

        lazy val others: Context[DataFrame] =
          diffContext.map { case (ref, other) =>
            ref -> getOrEval(diffContext, other)
          }

        val inner  = getOrEval(context, context(s.assetRef))
        val result = Debug.transformation(s, inner, others).as(s.assetRef)
        result

    }

  def resolveAndRegister(
      context: Context[Asset],
      asset: Asset,
      evalCallBack: (Context[Asset], Asset) => DataFrame,
      getTableCallBack: Asset => Option[DataFrame],
      registerCallBack: DataFrame => DataFrame
  )(implicit S: SparkSession): DataFrame = {

    val getOrEval: (Context[Asset], Asset) => DataFrame =
      (context, asset) =>
        registerCallBack(getTableCallBack(asset).getOrElse(evalCallBack(context, asset)))
    val df = resolve(context, asset, getOrEval)
    registerCallBack(df)
  }
  implicit def debugContext[T: EvalAsset]: EvalContext[Context[T]] =
    (context: Context[Asset]) =>
      context.map { case (ref, asset) =>
        ref -> EvalAsset[T].eval(context, asset)
      }
}
