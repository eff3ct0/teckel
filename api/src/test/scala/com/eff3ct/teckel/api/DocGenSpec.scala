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

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class DocGenSpec extends AnyFlatSpecLike with Matchers {

  private val simpleYaml: String =
    """input:
      |  - name: table1
      |    format: csv
      |    path: 'data/csv/example.csv'
      |    options:
      |      header: true
      |      sep: '|'
      |
      |output:
      |  - name: table1
      |    format: parquet
      |    mode: overwrite
      |    path: 'data/parquet/example'""".stripMargin

  private val complexYaml: String =
    """input:
      |  - name: table1
      |    format: csv
      |    path: 'data/csv/example.csv'
      |    options:
      |      header: true
      |      sep: '|'
      |
      |transformation:
      |  - name: selectTable1
      |    select:
      |      from: table1
      |      columns:
      |        - col1
      |        - col2
      |  - name: filteredTable
      |    where:
      |      from: selectTable1
      |      filter: 'col1 > 10'
      |
      |output:
      |  - name: filteredTable
      |    format: parquet
      |    mode: overwrite
      |    path: 'data/parquet/output'""".stripMargin

  "DocGen.generate" should "return a Right with markdown for a simple pipeline" in {
    val result = DocGen.generate(simpleYaml)
    result shouldBe a[Right[_, _]]

    val doc = result.toOption.get
    doc should include("# Pipeline Documentation")
  }

  it should "contain Data Sources section with input table" in {
    val result = DocGen.generate(simpleYaml)
    val doc    = result.toOption.get
    doc should include("## Data Sources")
    doc should include("table1")
    doc should include("csv")
  }

  it should "generate an inputs table with header row" in {
    val result = DocGen.generate(simpleYaml)
    val doc    = result.toOption.get
    doc should include("| Name | Format | Path |")
    doc should include("|------|--------|------|")
  }

  it should "contain Outputs section with output table" in {
    val result = DocGen.generate(simpleYaml)
    val doc    = result.toOption.get
    doc should include("## Outputs")
    doc should include("| Name | Format | Mode | Path |")
    doc should include("parquet")
    doc should include("overwrite")
  }

  it should "contain Data Flow section" in {
    val result = DocGen.generate(simpleYaml)
    val doc    = result.toOption.get
    doc should include("## Data Flow")
    doc should include("[INPUT]")
    doc should include("[OUTPUT]")
  }

  it should "include Transformations section for complex pipelines" in {
    val result = DocGen.generate(complexYaml)
    result shouldBe a[Right[_, _]]

    val doc = result.toOption.get
    doc should include("## Transformations")
    doc should include("### selectTable1")
    doc should include("### filteredTable")
  }

  it should "describe transformation types correctly" in {
    val result = DocGen.generate(complexYaml)
    val doc    = result.toOption.get
    doc should include("**Type**: Select")
    doc should include("**Type**: Filter")
    doc should include("**Columns**: col1, col2")
  }

  it should "return a Left for invalid YAML" in {
    val invalidYaml = "{{not valid yaml::"
    val result      = DocGen.generate(invalidYaml)
    result shouldBe a[Left[_, _]]
  }

  it should "return a Left for YAML that does not match ETL schema" in {
    val badYaml = "key: value\nother: thing"
    val result  = DocGen.generate(badYaml)
    result shouldBe a[Left[_, _]]
  }
}
