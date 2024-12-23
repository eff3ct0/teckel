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

import com.eff3ct.teckel.serializer.model.{ETL, Input, Output}
import com.eff3ct.teckel.serializer.types.PrimitiveType._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.io.Source

class ExampleSpec extends AnyFlatSpecLike with Matchers {

  "ExampleSpec" should "work correctly using default serializer" in {
    Serializer[ETL].decode(
      Source.fromFile("src/test/resources/simple.yaml").mkString
    ) shouldBe
      Right(
        ETL(
          List(
            Input(
              "table1",
              "csv",
              "data/csv/example.csv",
              Map("header" -> BooleanType(true), "sep" -> CharType('|'))
            )
          ),
          List(Output("table1", "parquet", "overwrite", "data/parquet/example", Map()))
        )
      )
  }

  it should "work correctly using yaml serializer" in {
    Serializer[ETL].decode(
      Source.fromFile("src/test/resources/simple.yaml").mkString
    ) shouldBe
      Right(
        ETL(
          List(
            Input(
              "table1",
              "csv",
              "data/csv/example.csv",
              Map("header" -> BooleanType(true), "sep" -> CharType('|'))
            )
          ),
          List(Output("table1", "parquet", "overwrite", "data/parquet/example", Map()))
        )
      )
  }

  it should "work correctly using json serializer" in {
    Serializer[ETL].decode(
      Source.fromFile("src/test/resources/simple.json").mkString
    ) shouldBe
      Right(
        ETL(
          List(
            Input(
              "table1",
              "csv",
              "data/csv/example.csv",
              Map("header" -> BooleanType(true), "sep" -> CharType('|'))
            )
          ),
          List(Output("table1", "parquet", "overwrite", "data/parquet/example", Map()))
        )
      )
  }

}
