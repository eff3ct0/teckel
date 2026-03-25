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

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class VariableResolverSpec extends AnyFlatSpecLike with Matchers {

  "VariableResolver" should "resolve a single variable with provided value" in {
    val content   = "path: ${DATA_DIR}/files"
    val variables = Map("DATA_DIR" -> "/mnt/data")
    VariableResolver.resolve(content, variables) shouldBe "path: /mnt/data/files"
  }

  it should "resolve multiple variables in one string" in {
    val content   = "${ENV}/${REGION}/data/${FORMAT}"
    val variables = Map("ENV" -> "prod", "REGION" -> "eu-west-1", "FORMAT" -> "parquet")
    VariableResolver.resolve(content, variables) shouldBe "prod/eu-west-1/data/parquet"
  }

  it should "use default value when variable is not provided" in {
    val content = "path: ${DATA_DIR:/tmp/default}/files"
    VariableResolver.resolve(content, Map.empty) shouldBe "path: /tmp/default/files"
  }

  it should "prefer provided value over default" in {
    val content   = "path: ${DATA_DIR:/tmp/default}/files"
    val variables = Map("DATA_DIR" -> "/mnt/actual")
    VariableResolver.resolve(content, variables) shouldBe "path: /mnt/actual/files"
  }

  it should "support empty default value" in {
    val content = "prefix${SUFFIX:}end"
    VariableResolver.resolve(content, Map.empty) shouldBe "prefixend"
  }

  it should "throw IllegalArgumentException for missing variable without default" in {
    val content = "path: ${MISSING_VAR}/files"
    an[IllegalArgumentException] should be thrownBy {
      VariableResolver.resolve(content, Map.empty)
    }
  }

  it should "include variable name in error message for missing variable" in {
    val content = "path: ${MY_UNDEFINED_VAR}/files"
    val ex = the[IllegalArgumentException] thrownBy {
      VariableResolver.resolve(content, Map.empty)
    }
    ex.getMessage should include("MY_UNDEFINED_VAR")
  }

  it should "return content unchanged when no variables are present" in {
    val content = "path: /simple/path/no/vars"
    VariableResolver.resolve(content, Map("FOO" -> "bar")) shouldBe content
  }

  it should "resolve with an empty variables map using defaults" in {
    val content = "format: ${FMT:csv}"
    VariableResolver.resolve(content, Map.empty) shouldBe "format: csv"
  }

  it should "resolve using the no-args overload" in {
    val content = "format: ${FMT:json}"
    VariableResolver.resolve(content) shouldBe "format: json"
  }

  it should "handle special regex characters in variable values" in {
    val content   = "filter: ${EXPR}"
    val variables = Map("EXPR" -> "col1 > $100 && col2 != 'N/A'")
    VariableResolver.resolve(content, variables) shouldBe "filter: col1 > $100 && col2 != 'N/A'"
  }

  it should "resolve adjacent variables without separator" in {
    val content   = "${A}${B}"
    val variables = Map("A" -> "hello", "B" -> "world")
    VariableResolver.resolve(content, variables) shouldBe "helloworld"
  }
}
