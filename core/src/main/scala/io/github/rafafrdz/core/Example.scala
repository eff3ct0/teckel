package io.github.rafafrdz.core

import io.github.rafafrdz.core.AST._
import io.github.rafafrdz.spark.etl.SparkETL
import org.apache.spark.sql.SparkSession
import org.slf4j.Logger

object Example extends SparkETL {

  val input: Asset =
    Asset("example", Input("csv", Map("sep" -> "|", "header" -> "true"), "data/csv/example.csv"))

  val output: Asset =
    Asset("output", Output("example", "parquet", Map("partition" -> "1"), "data/parquet/example"))

  val context: Context =
    Map(
      "example" -> input,
      "output"  -> output
    )

  /**
   * Name of the ETL
   */
  override val etlName: String = "Test"

  override def unsafeRun(implicit spark: SparkSession, logger: Logger): Unit =
    Exec.exec(context)
}
