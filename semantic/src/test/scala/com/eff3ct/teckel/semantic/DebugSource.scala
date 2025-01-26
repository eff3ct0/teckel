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

import cats.data.NonEmptyList
import com.eff3ct.teckel.model.Source._
import com.eff3ct.teckel.semantic.core.Semantic
import com.eff3ct.teckel.semantic.sources.Debug
import com.eff3ct.teckel.semantic.sources.Debug._
import com.holdenkarau.spark.testing._
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class DebugSource
    extends AnyFlatSpecLike
    with Matchers
    with DataFrameSuiteBase
    with SparkTestUtils {

  object Resources {
    val input: DataFrame = spark.read
      .format("csv")
      .option("header", "true")
      .option("sep", "|")
      .load("src/test/resources/data/csv/example.csv")
  }

  object Sources {

    val input: Input =
      Input("csv", Map("header" -> "true", "sep" -> "|"), "src/test/resources/data/csv/example.csv")

    val output: Output =
      Output("table1", "parquet", "overwrite", Map(), "src/test/resources/data/parquet/example")

    val select: Select = Select("table1", NonEmptyList.of("Symbol", "Date"))

    val where: Where = Where("table1", "Date > '2024-12-12'")

    val groupBy: GroupBy = GroupBy(
      "table1",
      NonEmptyList.of("Symbol"),
      NonEmptyList.of(
        "sum(`Adj Close`) as TotalClose",
        "max(High) as Highest",
        "min(Low) as Lowest"
      )
    )

    val orderBy: OrderBy = OrderBy("table1", NonEmptyList.of("High"), Some("Asc"))

  }
  "DebugSource" should "debug an input source" in {
    Semantic.any[Input, DataFrame](Sources.input) :===: Resources.input
  }

  it should "debug an output source" in {
    Debug[Output].eval(Resources.input, Sources.output) :===: Resources.input
  }

  it should "debug a select transformation" in {
    Debug[Select].eval(Resources.input, Sources.select) :===:
      Resources.input.select("Symbol", "Date")
  }

  it should "debug a where transformation" in {
    Debug[Where].eval(Resources.input, Sources.where) :===:
      Resources.input.where("Date > '2024-12-12'")
  }

  it should "debug a groupBy transformation" in {
    Debug[GroupBy].eval(Resources.input, Sources.groupBy) :===:
      Resources.input
        .groupBy("Symbol")
        .agg(
          sum("Adj Close") as "TotalClose",
          max("High") as "Highest",
          min("Low") as "Lowest"
        )
  }

  it should "debug an orderBy transformation" in {
    Debug[OrderBy].eval(Resources.input, Sources.orderBy) :===:
      Resources.input.orderBy("High")
  }

}
