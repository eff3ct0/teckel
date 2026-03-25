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
import com.eff3ct.teckel.model.{Asset, Context}
import com.eff3ct.teckel.model.Source._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class ValidatorSpec extends AnyFlatSpecLike with Matchers {

  private def mkContext(entries: (String, Asset)*): Context[Asset] =
    entries.toMap

  "Validator" should "pass validation for a valid simple pipeline" in {
    val context: Context[Asset] = mkContext(
      "table1" -> Asset("table1", Input("csv", Map.empty, "/data/input.csv")),
      "output1" -> Asset(
        "output1",
        Output("table1", "parquet", "overwrite", Map.empty, "/data/out")
      )
    )

    val result = Validator.validate(context)
    result.isValid shouldBe true
    Validator.formatErrors(result) shouldBe None
  }

  it should "pass validation for a pipeline with transformations" in {
    val context: Context[Asset] = mkContext(
      "table1" -> Asset("table1", Input("csv", Map.empty, "/data/input.csv")),
      "filtered" -> Asset(
        "filtered",
        Where("table1", "col1 > 10")
      ),
      "output1" -> Asset(
        "output1",
        Output("filtered", "parquet", "overwrite", Map.empty, "/data/out")
      )
    )

    Validator.validate(context).isValid shouldBe true
  }

  it should "detect a missing reference in a transformation" in {
    val context: Context[Asset] = mkContext(
      "table1" -> Asset("table1", Input("csv", Map.empty, "/data/input.csv")),
      "filtered" -> Asset(
        "filtered",
        Where("nonexistent", "col1 > 10")
      ),
      "output1" -> Asset(
        "output1",
        Output("filtered", "parquet", "overwrite", Map.empty, "/data/out")
      )
    )

    val result = Validator.validate(context)
    result.isInvalid shouldBe true

    val errors = Validator.formatErrors(result)
    errors shouldBe defined
    errors.get should include("nonexistent")
  }

  it should "detect a missing reference in an output" in {
    val context: Context[Asset] = mkContext(
      "table1" -> Asset("table1", Input("csv", Map.empty, "/data/input.csv")),
      "output1" -> Asset(
        "output1",
        Output("missing", "parquet", "overwrite", Map.empty, "/data/out")
      )
    )

    val result = Validator.validate(context)
    result.isInvalid shouldBe true

    val errors = Validator.formatErrors(result)
    errors shouldBe defined
    errors.get should include("missing")
  }

  it should "detect a cycle in the pipeline" in {
    val context: Context[Asset] = mkContext(
      "table1" -> Asset("table1", Input("csv", Map.empty, "/data/input.csv")),
      "stepA" -> Asset(
        "stepA",
        Select("stepB", NonEmptyList.of("col1"))
      ),
      "stepB" -> Asset(
        "stepB",
        Where("stepA", "col1 > 0")
      ),
      "output1" -> Asset(
        "output1",
        Output("stepA", "parquet", "overwrite", Map.empty, "/data/out")
      )
    )

    val result = Validator.validate(context)
    result.isInvalid shouldBe true

    val errors = Validator.formatErrors(result)
    errors shouldBe defined
    errors.get should include("Circular dependency")
  }

  it should "suggest similar names for typos (Levenshtein)" in {
    val context: Context[Asset] = mkContext(
      "table1" -> Asset("table1", Input("csv", Map.empty, "/data/input.csv")),
      "filtered" -> Asset(
        "filtered",
        Where("tabel1", "col1 > 10") // typo: tabel1 vs table1
      ),
      "output1" -> Asset(
        "output1",
        Output("filtered", "parquet", "overwrite", Map.empty, "/data/out")
      )
    )

    val result = Validator.validate(context)
    result.isInvalid shouldBe true

    val errors = Validator.formatErrors(result)
    errors shouldBe defined
    errors.get should include("Did you mean")
    errors.get should include("table1")
  }

  it should "detect output referencing another output" in {
    val context: Context[Asset] = mkContext(
      "table1" -> Asset("table1", Input("csv", Map.empty, "/data/input.csv")),
      "out1"   -> Asset("out1", Output("table1", "parquet", "overwrite", Map.empty, "/data/out1")),
      "out2"   -> Asset("out2", Output("out1", "parquet", "overwrite", Map.empty, "/data/out2"))
    )

    val result = Validator.validate(context)
    result.isInvalid shouldBe true

    val errors = Validator.formatErrors(result)
    errors shouldBe defined
    errors.get should include("outputs must reference inputs or transformations")
  }

  it should "collect multiple errors" in {
    val context: Context[Asset] = mkContext(
      "table1" -> Asset("table1", Input("csv", Map.empty, "/data/input.csv")),
      "filtered" -> Asset(
        "filtered",
        Where("nonexistent1", "col1 > 10")
      ),
      "output1" -> Asset(
        "output1",
        Output("nonexistent2", "parquet", "overwrite", Map.empty, "/data/out")
      )
    )

    val result = Validator.validate(context)
    result.isInvalid shouldBe true

    val errors = Validator.formatErrors(result)
    errors shouldBe defined
    errors.get should include("nonexistent1")
    errors.get should include("nonexistent2")
  }

  "Validator.formatErrors" should "return None for valid result" in {
    val context: Context[Asset] = mkContext(
      "table1" -> Asset("table1", Input("csv", Map.empty, "/data/input.csv")),
      "output1" -> Asset(
        "output1",
        Output("table1", "parquet", "overwrite", Map.empty, "/data/out")
      )
    )

    val result = Validator.validate(context)
    Validator.formatErrors(result) shouldBe None
  }

  it should "format error count correctly" in {
    val context: Context[Asset] = mkContext(
      "output1" -> Asset(
        "output1",
        Output("missing", "parquet", "overwrite", Map.empty, "/data/out")
      )
    )

    val result = Validator.validate(context)
    val errors = Validator.formatErrors(result)
    errors shouldBe defined
    errors.get should include("error(s)")
  }
}
