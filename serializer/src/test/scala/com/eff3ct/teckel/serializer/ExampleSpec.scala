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

package com.eff3ct.teckel.serializer

import cats.data.NonEmptyList
import com.eff3ct.teckel.serializer.model.etl._
import com.eff3ct.teckel.serializer.model.input._
import com.eff3ct.teckel.serializer.model.operations._
import com.eff3ct.teckel.serializer.model.output._
import com.eff3ct.teckel.serializer.model.transformation._
import com.eff3ct.teckel.serializer.types.PrimitiveType._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.io.Source

class ExampleSpec extends AnyFlatSpecLike with Matchers {

  object Model {
    val simple: ETL = ETL(
      NonEmptyList.of(
        Input(
          "table1",
          "csv",
          "data/csv/example.csv",
          Map("header" -> BooleanType(true), "sep" -> CharType('|'))
        )
      ),
      NonEmptyList.of(Output("table1", "parquet", "overwrite", "data/parquet/example", Map()))
    )

    val complexETL: ETL =
      ETL(
        NonEmptyList.of(
          Input(
            "table1",
            "csv",
            "data/csv/example.csv",
            Map("header" -> BooleanType(true), "sep" -> CharType('|'))
          )
        ),
        Some(
          NonEmptyList.of(
            Select("selectTable1", SelectOp("table1", NonEmptyList.of("col1", "col2"))),
            Where("whereTable1", WhereOp("selectTable1", "col1 > 10")),
            GroupBy(
              "groupByTable1",
              GroupByOp(
                "whereTable1",
                NonEmptyList.of("col1", "col2"),
                NonEmptyList.of("sum(col1)", "max(col2)")
              )
            ),
            OrderBy(
              "orderByTable1",
              OrderByOp("groupByTable1", NonEmptyList.of("col1", "col2"), Some("Desc"))
            )
          )
        ),
        NonEmptyList.of(
          Output("orderByTable1", "parquet", "overwrite", "data/parquet/example", Map())
        )
      )
  }

  /** Default */
  "ExampleSpec" should "work correctly using a simple yaml with default serializer" in {
    Serializer[ETL].decode(
      Source.fromFile("src/test/resources/simple.yaml").mkString
    ) shouldBe
      Right(Model.simple)
  }

  it should "work correctly using a complex yaml with default serializer" in {
    Serializer[ETL].decode(
      Source.fromFile("src/test/resources/complex.yaml").mkString
    ) shouldBe
      Right(Model.complexETL)
  }

  /** Yaml */
  it should "work correctly using a simple yaml with yaml serializer" in {
    Serializer[ETL].decode(
      Source.fromFile("src/test/resources/simple.yaml").mkString
    ) shouldBe
      Right(Model.simple)
  }

  it should "work correctly using a complex yaml with yaml serializer" in {
    Serializer[ETL].decode(
      Source.fromFile("src/test/resources/complex.yaml").mkString
    ) shouldBe
      Right(Model.complexETL)
  }

  /** Json */
  it should "work correctly using a simple json with json serializer" in {
    Serializer[ETL].decode(
      Source.fromFile("src/test/resources/simple.json").mkString
    ) shouldBe
      Right(Model.simple)
  }

  it should "work correctly using a complex json with json serializer" in {
    Serializer[ETL].decode(
      Source.fromFile("src/test/resources/complex.json").mkString
    ) shouldBe
      Right(Model.complexETL)
  }

}
