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

package io.github.rafafrdz.teckle.serializer

import io.github.rafafrdz.teckle.serializer.model.{ETL, Input, OptionItem, Output}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.io.Source

class ExampleSpec extends AnyFlatSpecLike with Matchers {

  "ExampleSpec" should "work correctly using default serializer" in {
    Serializer[ETL].decode(
      Source.fromFile("serializer/src/test/resources/simple.yaml").mkString
    ) shouldBe
      Right(
        ETL(
          List(
            Input("table1", "csv", "data/csv/example.csv", Some(OptionItem(Some(true), Some("|"))))
          ),
          List(Output("table1", "parquet", "overwrite", "data/parquet/example", None))
        )
      )
  }

  it should "work correctly using yaml serializer" in {
    Serializer[ETL].decode(
      Source.fromFile("serializer/src/test/resources/simple.yaml").mkString
    ) shouldBe
      Right(
        ETL(
          List(
            Input("table1", "csv", "data/csv/example.csv", Some(OptionItem(Some(true), Some("|"))))
          ),
          List(Output("table1", "parquet", "overwrite", "data/parquet/example", None))
        )
      )
  }

  it should "work correctly using json serializer" in {
    Serializer[ETL].decode(
      Source.fromFile("serializer/src/test/resources/simple.json").mkString
    ) shouldBe
      Right(
        ETL(
          List(
            Input("table1", "csv", "data/csv/example.csv", Some(OptionItem(Some(true), Some("|"))))
          ),
          List(Output("table1", "parquet", "overwrite", "data/parquet/example", None))
        )
      )
  }

}
