package io.github.rafafrdz.teckel.serializer

import io.github.rafafrdz.teckel.serializer.model._
import io.github.rafafrdz.teckel.serializer.model.TransformationYaml._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers._

class YamlDecoderSpec extends AnyFlatSpecLike with Matchers {

  object Resource {
    val input: String = """name: table1
                          |format: csv
                          |options:
                          |  header: 'true'
                          |  sep: '|'
                          |path: /path/path1
                          |""".stripMargin

    val output: String = """name: result
                           |format: csv
                           |options:
                           |  header: 'true'
                           |  sep: '|'
                           |path: /path/output
                           |""".stripMargin

    val join: String = """name: result
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

    val etl: String = """input:
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
  object Expected {
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

  "YamlEncoder" should "deserialize an input" in {
    deserialize[InputYaml](Resource.input) shouldBe Right(Expected.input)
  }

  it should "deserialize an output" in {
    deserialize[OutputYaml](Resource.output) shouldBe Right(Expected.output)
  }

  it should "deserialize a join transformation" in {
    deserialize[TransformationYaml](Resource.join) shouldBe Right(Expected.join)
  }

  it should "deserialize an ETL" in {
    deserialize[ETLYaml](Resource.etl) shouldBe Right(Expected.etl)
  }
  it should "convert an yaml to an ETL object" in {
    parse(Resource.etl) shouldBe Right(Expected.etl)
  }
}
