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

import com.eff3ct.teckel.model.Asset
import com.eff3ct.teckel.semantic.core.EvalAsset
import com.eff3ct.teckel.semantic.evaluation._
import com.holdenkarau.spark.testing._
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{max, min, sum}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class EvalAssetDebugSpec
    extends AnyFlatSpecLike
    with Matchers
    with DataFrameSuiteBase
    with SparkTestUtils
    with TestResources {

  "EvalAsset" should "debug an input asset" in {
    val inputAsset: Asset = Assets.inputA
    val result: DataFrame = EvalAsset[DataFrame].eval(Assets.context, inputAsset)
    result :===: Resources.input
  }

  it should "debug an output asset" in {
    val outputAsset: Asset = Assets.outputA
    val result: DataFrame  = EvalAsset[DataFrame].eval(Assets.context, outputAsset)
    result :===: Resources.input
  }

  it should "debug a select asset" in {
    val selectAsset: Asset = Assets.selectA
    val result: DataFrame  = EvalAsset[DataFrame].eval(Assets.context, selectAsset)
    result :===: Resources.input.select("Symbol", "Date")
  }

  it should "debug a where asset" in {
    val whereAsset: Asset = Assets.whereA
    val result: DataFrame = EvalAsset[DataFrame].eval(Assets.context, whereAsset)
    result :===: Resources.input.where("Date > '2024-12-12'")
  }

  it should "debug a groupBy asset" in {
    val groupByAsset: Asset = Assets.groupByA
    val result: DataFrame   = EvalAsset[DataFrame].eval(Assets.context, groupByAsset)
    result :===: Resources.input
      .groupBy("Symbol")
      .agg(
        sum("Adj Close") as "TotalClose",
        max("High") as "Highest",
        min("Low") as "Lowest"
      )
  }

  it should "debug a orderBy asset" in {
    val orderByAsset: Asset = Assets.orderByA
    val result: DataFrame   = EvalAsset[DataFrame].eval(Assets.context, orderByAsset)
    result :===: Resources.input.orderBy("High")
  }

  it should "debug a join asset" in {
    val joinAsset: Asset  = Assets.joinA
    val result: DataFrame = EvalAsset[DataFrame].eval(Assets.context, joinAsset)
    result :===: Resources.input.join(
      Resources.input2,
      Resources.input("id") === Resources.input2("id"),
      "inner"
    )
  }
}
