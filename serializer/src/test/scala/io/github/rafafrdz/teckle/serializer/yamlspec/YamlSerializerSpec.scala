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

package io.github.rafafrdz.teckle.serializer.yamlspec

import io.github.rafafrdz.teckle.serializer.Serializer
import io.github.rafafrdz.teckle.serializer.model._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class YamlSerializerSpec extends AnyFlatSpecLike with Matchers {

  import io.github.rafafrdz.teckle.serializer.alternative.yaml

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
      Input("table1", "csv", "/path/path1/file.csv", Some(OptionItem(Some(true), Some("|"))))

    val output: Output =
      Output("table1", "parquet", "overwrite", "/path/path1", None)

    val etl: ETL =
      ETL(
        List(
          Input("table1", "csv", "data/csv/example.csv", Some(OptionItem(Some(true), Some("|"))))
        ),
        List(Output("table1", "parquet", "overwrite", "data/parquet/example", None))
      )
  }

  "YamlSerializer" should "decode into an Input" in {
    Serializer[Input].decode(Yaml.input) shouldBe Right(Model.input)
  }

  it should "decode into an Output" in {
    Serializer[Output].decode(Yaml.output) shouldBe Right(Model.output)
  }

  it should "decode into a simple ETL" in {
    Serializer[ETL].decode(Yaml.etl) shouldBe Right(Model.etl)
  }
}
