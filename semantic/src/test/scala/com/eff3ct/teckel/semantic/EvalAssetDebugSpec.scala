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
}
