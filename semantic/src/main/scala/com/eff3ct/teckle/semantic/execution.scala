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

package com.eff3ct.teckle.semantic
import com.eff3ct.teckle.model.Source.Output
import com.eff3ct.teckle.model._
import com.eff3ct.teckle.semantic.core._
import com.eff3ct.teckle.semantic.evaluation._
import com.eff3ct.teckle.semantic.sources.Exec
import com.eff3ct.teckle.semantic.sources.Exec._
import org.apache.spark.sql._

object execution {

  implicit def exec(implicit S: SparkSession): EvalAsset[Unit] =
    (context: Context[Asset], asset: Asset) => {
      asset.source match {
        case o: Output =>
          val EA: EvalAsset[DataFrame] = debug
          Exec[Output].eval(
            EA.eval(context, asset),
            o
          ) // TODO: Check if the asset is already evaluated
        case _ => ()
      }
    }

  implicit def execContext(implicit E: EvalAsset[Unit]): EvalContext[Unit] =
    (context: Context[Asset]) =>
      context.foreach {
        case (ref, asset @ Asset(_, _: Output)) =>
          ref -> EvalAsset[Unit].eval(context, asset)
        case _ => ()
      }

}
