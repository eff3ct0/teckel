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
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class ExampleSpec extends AnyFlatSpecLike with Matchers {

  def sparkBuilder(): SparkSession = {
    val sparkConf: SparkConf = new SparkConf()
    val master: String       = sparkConf.get("spark.master", "local[*]")
    val appName: String      = sparkConf.get("spark.app.name", "spark-etl")
    SparkSession.builder().config(sparkConf).master(master).appName(appName).getOrCreate()
  }

  implicit val spark: SparkSession = sparkBuilder()

  "ExampleSpec" should "work correctly in a ETL F using IO" in {
    noException should be thrownBy etl[IO, Unit]("src/test/resources/etl/simple.yaml")
      .unsafeRunSync()
  }

  it should "work correctly in a ETL IO" in {
    noException should be thrownBy etlIO[Unit]("src/test/resources/etl/simple.yaml").unsafeRunSync()
  }

  it should "work correctly in an unsafe ETL" in {
    noException should be thrownBy unsafeETL[Unit]("src/test/resources/etl/simple.yaml")
  }

  it should "work correctly a select pipeline" in {
    noException should be thrownBy unsafeETL[Unit]("src/test/resources/etl/select.yaml")
  }

}
