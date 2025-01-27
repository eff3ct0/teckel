package com.eff3ct.teckel.semantic

import cats.data.NonEmptyList
import com.eff3ct.teckel.model.Source._
import com.eff3ct.teckel.model.{Asset, Context}
import com.holdenkarau.spark.testing.DataFrameSuiteBase
import org.apache.spark.sql.DataFrame

trait TestResources {
  self: DataFrameSuiteBase with SparkTestUtils =>

  object Resources {
    val input: DataFrame = spark.read
      .format("csv")
      .option("header", "true")
      .option("sep", "|")
      .load("src/test/resources/data/csv/example.csv")
      .as("table1")

    val input2: DataFrame = spark.read
      .format("csv")
      .option("header", "true")
      .option("sep", "|")
      .load("src/test/resources/data/csv/example-2.csv")
      .as("table2")
  }

  object Sources {

    val input: Input =
      Input("csv", Map("header" -> "true", "sep" -> "|"), "src/test/resources/data/csv/example.csv")

    val input2: Input =
      Input("csv", Map("header" -> "true", "sep" -> "|"), "src/test/resources/data/csv/example-2.csv")

    val output: Output =
      Output("table1", "parquet", "overwrite", Map(), "src/test/resources/data/parquet/example")

    val select: Select = Select("table1", NonEmptyList.of("Symbol", "Date"))

    val where: Where = Where("table1", "Date > '2024-12-12'")

    val groupBy: GroupBy = GroupBy(
      "table1",
      NonEmptyList.of("Symbol"),
      NonEmptyList.of(
        "sum(`Adj Close`) as TotalClose",
        "max(High) as Highest",
        "min(Low) as Lowest"
      )
    )

    val orderBy: OrderBy = OrderBy("table1", NonEmptyList.of("High"), Some("Asc"))

    val join: Join =
      Join(
        "table1",
        NonEmptyList.of(
          Relation("table2", "inner", List("table1.id == table2.id"))
        )
      )

  }

  object Assets {
    val inputA: Asset   = Asset("table1", Sources.input)
    val outputA: Asset  = Asset("table1", Sources.output)
    val selectA: Asset  = Asset("tableSelect", Sources.select)
    val whereA: Asset   = Asset("tableWhere", Sources.where)
    val groupByA: Asset = Asset("tableGroupBy", Sources.groupBy)
    val orderByA: Asset = Asset("tableOrderBy", Sources.orderBy)
    val context: Context[Asset] = Map(
      "table1"       -> inputA,
      "tableSelect"  -> selectA,
      "tableWhere"   -> whereA,
      "tableGroupBy" -> groupByA,
      "tableOrderBy" -> orderByA
    )
  }
}
