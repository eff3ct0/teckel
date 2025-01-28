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

package com.eff3ct.teckel.api

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.eff3ct.teckel.api.file._
import com.eff3ct.teckel.semantic.execution._
import com.eff3ct.teckel.semantic.{SparkTestUtils, TestResources}
import com.holdenkarau.spark.testing.DataFrameSuiteBase
import org.apache.spark.sql.functions._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class ExampleSpec
    extends AnyFlatSpecLike
    with Matchers
    with DataFrameSuiteBase
    with SparkTestUtils
    with TestResources {

  "ExampleSpec" should "work correctly in a ETL F using IO" in {
    noException should be thrownBy etl[IO, Unit]("src/test/resources/etl/simple.yaml")
      .unsafeRunSync()
    spark.read.parquet("src/test/resources/data/parquet/example/simple") :===: Resources.input
  }

  it should "work correctly in a ETL IO" in {
    noException should be thrownBy etlIO[Unit]("src/test/resources/etl/simple.yaml").unsafeRunSync()
    spark.read.parquet("src/test/resources/data/parquet/example/simple") :===: Resources.input
  }

  it should "work correctly in an unsafe ETL" in {
    noException should be thrownBy unsafeETL[Unit]("src/test/resources/etl/simple.yaml")
    spark.read.parquet("src/test/resources/data/parquet/example/simple") :===: Resources.input
  }

  it should "work correctly a select pipeline" in {
    noException should be thrownBy unsafeETL[Unit]("src/test/resources/etl/select.yaml")
    spark.read
      .parquet("src/test/resources/data/parquet/example/select") :===:
      Resources.input.select("id", "date")
  }

  it should "work correctly a where pipeline" in {
    noException should be thrownBy unsafeETL[Unit]("src/test/resources/etl/where.yaml")
    spark.read
      .parquet("src/test/resources/data/parquet/example/where") :===:
      Resources.input.where("Date > '2024-12-12'")
  }

  it should "work correctly a groupBy pipeline" in {
    noException should be thrownBy unsafeETL[Unit]("src/test/resources/etl/group-by.yaml")
    spark.read
      .parquet("src/test/resources/data/parquet/example/group-by") :===:
      Resources.input
        .groupBy("Symbol")
        .agg(
          sum("Adj Close") as "TotalClose",
          max("High") as "Highest",
          min("Low") as "Lowest"
        )
  }

  it should "work correctly a orderBy pipeline" in {
    noException should be thrownBy unsafeETL[Unit]("src/test/resources/etl/order-by.yaml")
    spark.read
      .parquet("src/test/resources/data/parquet/example/order-by") :===:
      Resources.input.orderBy("Id", "Date")

  }
//  it should "work correctly a join pipeline" in {
  //    noException should be thrownBy unsafeETL[Unit]("src/test/resources/etl/join.yaml")
  //  }
}
