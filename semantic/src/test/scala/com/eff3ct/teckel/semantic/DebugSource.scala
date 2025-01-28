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

import com.eff3ct.teckel.semantic.sources.Debug
import com.holdenkarau.spark.testing._
import org.apache.spark.sql.functions._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import scala.collection.mutable.{Map => MMap}

class DebugSource
    extends AnyFlatSpecLike
    with Matchers
    with DataFrameSuiteBase
    with SparkTestUtils
    with TestResources {

  "DebugSource" should "debug an input source" in {
    Debug.input(Sources.input) :===: Resources.input
  }

  it should "debug an output source" in {
    Debug.output(Resources.input) :===: Resources.input
  }

  it should "debug a select transformation" in {
    Debug.select(Resources.input, Sources.select) :===:
      Resources.input.select("Symbol", "Date")
  }

  it should "debug a where transformation" in {
    Debug.where(Resources.input, Sources.where) :===:
      Resources.input.where("Date > '2024-12-12'")
  }

  it should "debug a groupBy transformation" in {
    Debug.groupBy(Resources.input, Sources.groupBy) :===:
      Resources.input
        .groupBy("Symbol")
        .agg(
          sum("Adj Close") as "TotalClose",
          max("High") as "Highest",
          min("Low") as "Lowest"
        )
  }

  it should "debug an orderBy transformation" in {
    Debug.orderBy(Resources.input, Sources.orderBy) :===:
      Resources.input.orderBy("High")
  }

  it should "debug a join transformation using column expressions" in {
    Debug.join(Sources.join, Resources.input, MMap("table2" -> Resources.input2)) :===:
      Resources.input
        .join(Resources.input2, Resources.input("id") === Resources.input2("id"), "inner")
  }

  it should "debug a join transformation using a condition column expression" in {
    Debug.join(Sources.join, Resources.input, MMap("table2" -> Resources.input2)) :===:
      Resources.input
        .join(Resources.input2, expr("table1.id == table2.id"), "inner")
  }

}
