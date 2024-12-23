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

package com.eff3ct.teckel.serializer.yamlspec

import cats.data.NonEmptyList
import com.eff3ct.teckel.serializer.Serializer
import com.eff3ct.teckel.serializer.model.input._
import com.eff3ct.teckel.serializer.model.output._
import com.eff3ct.teckel.serializer.model.transformation._
import com.eff3ct.teckel.serializer.model.operations._
import com.eff3ct.teckel.serializer.model.etl._
import com.eff3ct.teckel.serializer.types.PrimitiveType._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class YamlSerializerSpec extends AnyFlatSpecLike with Matchers {

  import com.eff3ct.teckel.serializer.alternative.yaml

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

    val select: String =
      """
        |name: selectTable1
        |select:
        |  from: table1
        |  columns:
        |    - col1
        |    - col2
        |""".stripMargin

    val where: String =
      """
        |name: whereTable1
        |where:
        |  from: table1
        |  filter: 'col1 > 10'
        |""".stripMargin

    val groupBy: String =
      """
        |name: groupByTable1
        |group:
        |  from: table1
        |  by:
        |    - col1
        |    - col2
        |  agg:
        |    - sum(col1)
        |    - max(col2)
        |""".stripMargin

    val orderBy: String =
      """
        |name: orderByTable1
        |order:
        |  from: table1
        |  by:
        |    - col1
        |    - col2
        |  order: Desc
        |""".stripMargin

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

    val complexETL: String = """input:
                        |  - name: table1
                        |    format: csv
                        |    path: 'data/csv/example.csv'
                        |    options:
                        |      header: true
                        |      sep: '|'
                        |
                        |
                        |transformation:
                        |  - name: selectTable1
                        |    select:
                        |      from: table1
                        |      columns:
                        |        - col1
                        |        - col2
                        |  - name: whereTable1
                        |    where:
                        |      from: selectTable1
                        |      filter: 'col1 > 10'
                        |  - name: groupByTable1
                        |    group:
                        |      from: whereTable1
                        |      by:
                        |        - col1
                        |        - col2
                        |      agg:
                        |        - sum(col1)
                        |        - max(col2)
                        |  - name: orderByTable1
                        |    order:
                        |      from: groupByTable1
                        |      by:
                        |        - col1
                        |        - col2
                        |      order: Desc
                        |
                        |
                        |output:
                        |  - name: orderByTable1
                        |    format: parquet
                        |    mode: overwrite
                        |    path: 'data/parquet/example'
                        |""".stripMargin

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

    val select: Select =
      Select(
        "selectTable1",
        SelectOp("table1", NonEmptyList.of("col1", "col2"))
      )

    val where: Where =
      Where(
        "whereTable1",
        WhereOp("table1", "col1 > 10")
      )

    val groupBy: GroupBy =
      GroupBy(
        "groupByTable1",
        GroupByOp(
          "table1",
          NonEmptyList.of("col1", "col2"),
          NonEmptyList.of("sum(col1)", "max(col2)")
        )
      )

    val orderBy: OrderBy =
      OrderBy(
        "orderByTable1",
        OrderByOp("table1", NonEmptyList.of("col1", "col2"), Some("Desc"))
      )

    val etl: ETL =
      ETL(
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

  "YamlSerializer" should "decode into an Input" in {
    Serializer[Input].decode(Yaml.input) shouldBe Right(Model.input)
  }

  it should "decode into an Output" in {
    Serializer[Output].decode(Yaml.output) shouldBe Right(Model.output)
  }

  it should "decode into a Select" in {
    Serializer[Select].decode(Yaml.select) shouldBe Right(Model.select)
  }

  it should "decode into a Where" in {
    Serializer[Where].decode(Yaml.where) shouldBe Right(Model.where)
  }

  it should "decode into a GroupBy" in {
    Serializer[GroupBy].decode(Yaml.groupBy) shouldBe Right(Model.groupBy)
  }

  it should "decode into a OrderBy" in {
    Serializer[OrderBy].decode(Yaml.orderBy) shouldBe Right(Model.orderBy)
  }

  it should "decode into a simple ETL" in {
    Serializer[ETL].decode(Yaml.etl) shouldBe Right(Model.etl)
  }

  it should "decode into a complex ETL" in {
    Serializer[ETL].decode(Yaml.complexETL) shouldBe Right(Model.complexETL)
  }
}
