package io.github.rafafrdz.teckel.serializer

import io.github.rafafrdz.teckel.serializer.model._
import io.github.rafafrdz.teckel.serializer.model.TransformationYaml._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers._

class YamlEncoderSpec extends AnyFlatSpecLike with Matchers {

  object Resources {
    val input: InputYaml =
      InputYaml("table1", "csv", Map("header" -> "true", "sep" -> "|"), "/path/path1")

    val output: OutputYaml =
      OutputYaml("result", "csv", Map("header" -> "true", "sep" -> "|"), "/path/output")

    val join: TransformationYaml =
      JoinTransformationYaml(
        "result",
        JoinOperationYaml(
          "left",
          JoinRelationYaml(
            "table1",
            List(JoinSourceYaml("table2", Map("t1pk1" -> "t2pk1", "t1pk2" -> "t2pk2")))
          )
        )
      )

    val etl: ETLYaml =
      ETLYaml(
        input = List(input),
        transformation = List(join),
        output = List(output)
      )
  }

  "YamlEncoder" should "serialize an input" in {
    serialize[InputYaml](Resources.input) shouldBe """name: table1
                                                     |format: csv
                                                     |options:
                                                     |  header: 'true'
                                                     |  sep: '|'
                                                     |path: /path/path1
                                                     |""".stripMargin

  }

  it should "serialize an output" in {
    serialize[OutputYaml](Resources.output) shouldBe """name: result
                                                     |format: csv
                                                     |options:
                                                     |  header: 'true'
                                                     |  sep: '|'
                                                     |path: /path/output
                                                     |""".stripMargin
  }

  it should "serialize a join transformation" in {
    serialize[TransformationYaml](Resources.join) shouldBe """name: result
                                                       |join:
                                                       |  joinType: left
                                                       |  relation:
                                                       |    left: table1
                                                       |    right:
                                                       |    - name: table2
                                                       |      fields:
                                                       |        t1pk1: t2pk1
                                                       |        t1pk2: t2pk2
                                                       |""".stripMargin
  }

  it should "serialize an ETL" in {
    serialize[ETLYaml](Resources.etl) shouldBe """input:
                                                 |- name: table1
                                                 |  format: csv
                                                 |  options:
                                                 |    header: 'true'
                                                 |    sep: '|'
                                                 |  path: /path/path1
                                                 |transformation:
                                                 |- name: result
                                                 |  join:
                                                 |    joinType: left
                                                 |    relation:
                                                 |      left: table1
                                                 |      right:
                                                 |      - name: table2
                                                 |        fields:
                                                 |          t1pk1: t2pk1
                                                 |          t1pk2: t2pk2
                                                 |output:
                                                 |- name: result
                                                 |  format: csv
                                                 |  options:
                                                 |    header: 'true'
                                                 |    sep: '|'
                                                 |  path: /path/output
                                                 |""".stripMargin
  }

  it should "transform an ETL object to a yaml" in {
    yaml(Resources.etl) shouldBe """input:
                                   |- name: table1
                                   |  format: csv
                                   |  options:
                                   |    header: 'true'
                                   |    sep: '|'
                                   |  path: /path/path1
                                   |transformation:
                                   |- name: result
                                   |  join:
                                   |    joinType: left
                                   |    relation:
                                   |      left: table1
                                   |      right:
                                   |      - name: table2
                                   |        fields:
                                   |          t1pk1: t2pk1
                                   |          t1pk2: t2pk2
                                   |output:
                                   |- name: result
                                   |  format: csv
                                   |  options:
                                   |    header: 'true'
                                   |    sep: '|'
                                   |  path: /path/output
                                   |""".stripMargin
  }
}
