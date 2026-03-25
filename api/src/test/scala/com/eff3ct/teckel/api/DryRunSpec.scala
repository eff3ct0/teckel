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

class DryRunSpec extends AnyFlatSpecLike with Matchers {

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

  "DryRun.explain" should "return a Right with plan for a simple pipeline" in {
    val result = DryRun.explain(simpleYaml)
    result shouldBe a[Right[_, _]]

    val plan = result.toOption.get
    plan should include("Pipeline Execution Plan")
    plan should include("Inputs")
    plan should include("Outputs")
    plan should include("table1")
    plan should include("Validation: OK")
  }

  it should "include transformation details for a complex pipeline" in {
    val result = DryRun.explain(complexYaml)
    result shouldBe a[Right[_, _]]

    val plan = result.toOption.get
    plan should include("Transformations")
    plan should include("selectTable1")
    plan should include("filteredTable")
    plan should include("SELECT")
    plan should include("WHERE")
  }

  it should "include input and output counts in the plan summary" in {
    val result = DryRun.explain(simpleYaml)
    result shouldBe a[Right[_, _]]

    val plan = result.toOption.get
    plan should include("1 inputs")
    plan should include("1 outputs")
  }

  it should "return a Left for invalid YAML" in {
    val invalidYaml = "{{not valid yaml at all::"
    val result      = DryRun.explain(invalidYaml)
    result shouldBe a[Left[_, _]]
  }

  it should "return a Left for YAML that does not match ETL schema" in {
    val badYaml = "key: value\nother: thing"
    val result  = DryRun.explain(badYaml)
    result shouldBe a[Left[_, _]]
  }

  it should "show validation errors for pipelines with bad references" in {
    val badRefYaml =
      """input:
        |  - name: table1
        |    format: csv
        |    path: 'data/input.csv'
        |
        |transformation:
        |  - name: filtered
        |    where:
        |      from: nonexistent
        |      filter: 'col > 0'
        |
        |output:
        |  - name: filtered
        |    format: parquet
        |    mode: overwrite
        |    path: 'data/output'""".stripMargin

    val result = DryRun.explain(badRefYaml)
    result shouldBe a[Right[_, _]]

    val plan = result.toOption.get
    plan should include("Validation Errors")
    plan should include("nonexistent")
  }
}
