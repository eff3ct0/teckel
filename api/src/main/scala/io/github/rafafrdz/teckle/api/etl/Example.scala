package io.github.rafafrdz.teckle.api.etl

import io.github.rafafrdz.teckle.api.spark.SparkETL
import org.apache.spark.sql.SparkSession
import org.slf4j.Logger

object Example extends SparkETL {

  /**
   * Name of the ETL
   */
  override val etlName: String = "Test"

  override def unsafeRun(implicit spark: SparkSession, logger: Logger): Unit =
    ETL.exec("etl/simple.yaml")
}
