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

import com.eff3ct.teckel.model.Asset._
import com.eff3ct.teckel.model.Source._
import com.eff3ct.teckel.model._
import com.eff3ct.teckel.semantic.core._
import com.eff3ct.teckel.semantic.sources._
import org.apache.spark.sql._

object evaluation {

  private def getTable(
      context: Context[Asset],
      assetRef: AssetRef,
      orElse: => DataFrame
  ): DataFrame =
    context(assetRef) match {
      case a: Asset.ResolvedAsset[DataFrame] => a.source
      case _                                 => orElse
    }

  private def register(context: Context[Asset], a: UnResolvedAsset, df: DataFrame): DataFrame = {
    context.put(a.assetRef, Asset.ResolvedAsset(a.assetRef, df))
    df
  }

  implicit def debug(implicit S: SparkSession): EvalAsset[DataFrame] =
    new EvalAsset[DataFrame] {
      override def eval(context: Context[Asset], asset: Asset): DataFrame = {
        asset match {
          case a: Asset.ResolvedAsset[DataFrame] => a.source
          case a: Asset.UnResolvedAsset          => resolveAndRegister(context, a, eval)

        }
      }
    }

  def resolve(
      context: Context[Asset],
      asset: UnResolvedAsset,
      evalCallBack: (Context[Asset], Asset) => DataFrame
  )(implicit S: SparkSession): DataFrame =
    asset.source match {
      case s: Input => Debug.input(s).as(asset.assetRef)

      case s: Output =>
        lazy val callBackDf = evalCallBack(context, context(s.assetRef))
        val inner           = getTable(context, s.assetRef, callBackDf)
        val result          = Debug.output(inner).as(asset.assetRef)
        result

      case s: Transformation =>
        lazy val diffContext: Context[Asset] =
          context.filterNot { case (ref, _) => ref == asset.assetRef }

        lazy val others: Context[DataFrame] =
          diffContext.map { case (ref, other) =>
            lazy val callBackDf = evalCallBack(diffContext, other)
            ref -> getTable(context, ref, callBackDf)
          }

        lazy val callBackDf = evalCallBack(context, context(s.assetRef))
        lazy val inner      = getTable(context, s.assetRef, callBackDf)
        val result          = Debug.transformation(s, inner, others).as(s.assetRef)
        result

    }

  def resolveAndRegister(
      context: Context[Asset],
      asset: UnResolvedAsset,
      evalCallBack: (Context[Asset], Asset) => DataFrame
  )(implicit S: SparkSession): DataFrame = {
    val df = resolve(context, asset, evalCallBack)
    register(context, asset, df)
  }
  implicit def debugContext[T: EvalAsset]: EvalContext[Context[T]] =
    (context: Context[Asset]) =>
      context.map { case (ref, asset) =>
        ref -> EvalAsset[T].eval(context, asset)
      }
}
