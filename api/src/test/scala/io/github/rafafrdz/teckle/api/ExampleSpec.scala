/*
 * Invasion Order Software License Agreement
 *
 * This file is part of the proprietary software provided by Invasion Order.
 * Use of this file is governed by the terms and conditions outlined in the
 * Invasion Order Software License Agreement.
 *
 * Unauthorized copying, modification, or distribution of this file, via any
 * medium, is strictly prohibited. The software is provided "as is," without
 * warranty of any kind, express or implied.
 *
 * For the full license text, please refer to the LICENSE file included
 * with this distribution, or contact Invasion Order at contact@iorder.dev.
 *
 * (c) 2024 Invasion Order. All rights reserved.
 */

package io.github.rafafrdz.teckle.api

import cats.effect.unsafe.implicits.global
import io.github.rafafrdz.teckle.api.etl.{etl, unsafeETL}
import io.github.rafafrdz.teckle.semantic.evaluation._
import io.github.rafafrdz.teckle.semantic.execution._
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

  "ExampleSpec" should "work correctly in a ETL" in {
    noException should be thrownBy etl[Unit]("api/src/test/resources/etl/simple.yaml").unsafeRunSync()
  }

  it should "work correctly in an unsafe ETL" in {
    noException should be thrownBy unsafeETL[Unit]("api/src/test/resources/etl/simple.yaml")
  }

}
