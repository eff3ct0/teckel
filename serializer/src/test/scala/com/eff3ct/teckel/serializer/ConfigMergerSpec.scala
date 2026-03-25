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

import io.circe.yaml.parser
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class ConfigMergerSpec extends AnyFlatSpecLike with Matchers {

  "ConfigMerger" should "deep merge two YAML configs" in {
    val base =
      """database:
        |  host: localhost
        |  port: 5432""".stripMargin

    val overlay =
      """database:
        |  port: 3306
        |  name: mydb""".stripMargin

    val result = ConfigMerger.merge(base, overlay)
    result shouldBe a[Right[_, _]]

    val json = parser.parse(result.toOption.get).toOption.get
    val db   = json.hcursor.downField("database")
    db.downField("host").as[String] shouldBe Right("localhost")
    db.downField("port").as[Int] shouldBe Right(3306)
    db.downField("name").as[String] shouldBe Right("mydb")
  }

  it should "let overlay override base scalar values" in {
    val base    = "format: csv"
    val overlay = "format: parquet"

    val result = ConfigMerger.merge(base, overlay)
    result shouldBe a[Right[_, _]]

    val json = parser.parse(result.toOption.get).toOption.get
    json.hcursor.downField("format").as[String] shouldBe Right("parquet")
  }

  it should "preserve base keys not present in overlay" in {
    val base =
      """input:
        |  name: table1
        |  format: csv
        |  path: /data/input""".stripMargin

    val overlay =
      """input:
        |  format: parquet""".stripMargin

    val result = ConfigMerger.merge(base, overlay)
    result shouldBe a[Right[_, _]]

    val json  = parser.parse(result.toOption.get).toOption.get
    val input = json.hcursor.downField("input")
    input.downField("name").as[String] shouldBe Right("table1")
    input.downField("format").as[String] shouldBe Right("parquet")
    input.downField("path").as[String] shouldBe Right("/data/input")
  }

  it should "return a Left for invalid base YAML" in {
    val base    = "{{invalid yaml::"
    val overlay = "key: value"
    ConfigMerger.merge(base, overlay) shouldBe a[Left[_, _]]
  }

  it should "return a Left for invalid overlay YAML" in {
    val base    = "key: value"
    val overlay = "{{invalid yaml::"
    ConfigMerger.merge(base, overlay) shouldBe a[Left[_, _]]
  }

  it should "merge nested objects recursively" in {
    val base =
      """a:
        |  b:
        |    c: 1
        |    d: 2""".stripMargin

    val overlay =
      """a:
        |  b:
        |    d: 99
        |    e: 3""".stripMargin

    val result = ConfigMerger.merge(base, overlay)
    result shouldBe a[Right[_, _]]

    val json = parser.parse(result.toOption.get).toOption.get
    val ab   = json.hcursor.downField("a").downField("b")
    ab.downField("c").as[Int] shouldBe Right(1)
    ab.downField("d").as[Int] shouldBe Right(99)
    ab.downField("e").as[Int] shouldBe Right(3)
  }

  "ConfigMerger.mergeAll" should "return empty string for empty list" in {
    ConfigMerger.mergeAll(Nil) shouldBe Right("")
  }

  it should "return the single config unchanged for a singleton list" in {
    val config = "key: value"
    ConfigMerger.mergeAll(List(config)) shouldBe Right(config)
  }

  it should "merge multiple configs in order" in {
    val cfg1 = "x: 1"
    val cfg2 = "x: 2\ny: 10"
    val cfg3 = "x: 3\nz: 20"

    val result = ConfigMerger.mergeAll(List(cfg1, cfg2, cfg3))
    result shouldBe a[Right[_, _]]

    val json = parser.parse(result.toOption.get).toOption.get
    json.hcursor.downField("x").as[Int] shouldBe Right(3)
    json.hcursor.downField("y").as[Int] shouldBe Right(10)
    json.hcursor.downField("z").as[Int] shouldBe Right(20)
  }
}
