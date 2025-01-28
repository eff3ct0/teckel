package com.eff3ct.teckel.api

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
}
