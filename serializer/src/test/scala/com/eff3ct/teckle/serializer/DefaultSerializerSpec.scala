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

package com.eff3ct.teckle.serializer

import com.eff3ct.teckle.serializer.model._
import com.eff3ct.teckle.serializer.types.PrimitiveType._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class DefaultSerializerSpec extends AnyFlatSpecLike with Matchers {

  object Yaml {

    val input: String = """name: table1
                          |format: csv
                          |path: '/path/path1/file.csv'
                          |options:
                          |  header: true
                          |  sep: '|'""".stripMargin

    val output: String = """name: table1
                           |format: parquet
                           |mode: overwrite
                           |path: '/path/path1'""".stripMargin

    val etl: String = """input:
                        |  - name: table1
                        |    format: csv
                        |    path: 'data/csv/example.csv'
                        |    options:
                        |      header: true
                        |      sep: '|'
                        |
                        |
                        |output:
                        |  - name: table1
                        |    format: parquet
                        |    mode: overwrite
                        |    path: 'data/parquet/example'""".stripMargin

  }

  object Model {

    val input: Input =
      Input(
        "table1",
        "csv",
        "/path/path1/file.csv",
        Map("header" -> BooleanType(true), "sep" -> CharType('|'))
      )

    val output: Output =
      Output("table1", "parquet", "overwrite", "/path/path1", Map())

    val etl: ETL =
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
  }

  "DefaultSerializer" should "decode into an Input" in {

    Serializer[Input].decode(Yaml.input) shouldBe Right(Model.input)
  }

  it should "decode into an Output" in {

    Serializer[Output].decode(Yaml.output) shouldBe Right(Model.output)
  }

  it should "decode into a simple ETL" in {

    Serializer[ETL].decode(Yaml.etl) shouldBe Right(Model.etl)
  }
}
