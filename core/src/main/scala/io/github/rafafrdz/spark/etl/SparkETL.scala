package io.github.rafafrdz.spark.etl

import cats.effect.{ExitCode, IO, IOApp}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}

trait SparkETL extends IOApp {

  /**
   * Name of the ETL
   */
  val etlName: String

  /**
   * Builds a Spark session
   * @param config ETL configuration
   * @return Spark session
   */
  private final def sparkBuilder(): SparkSession = {
    val sparkConf: SparkConf = new SparkConf()
    val master: String       = sparkConf.get("spark.master", "local[*]")
    val appName: String      = sparkConf.get("spark.app.name", "spark-etl")
    SparkSession.builder().config(sparkConf).master(master).appName(appName).getOrCreate()
  }

  /**
   * Logger instance for the ETL
   * @return logger instance
   */
  private final def logger: Logger = LoggerFactory.getLogger(s"[ETL][$etlName]")

  /**
   * Run the ETL. This method should be implemented by the ETL.
   * @param config ETL configuration
   * @param spark Spark session
   * @param logger logger
   */
  def unsafeRun(implicit spark: SparkSession, logger: Logger): Unit = {
    import cats.effect.unsafe.implicits.global
    runIO(spark, logger).unsafeRunSync()
  }

  /**
   * Run the ETL using IO
   * @param config ETL configuration
   * @param spark Spark session
   * @param logger logger
   * @return IO
   */
  def runIO(implicit spark: SparkSession, logger: Logger): IO[Unit] =
    IO(unsafeRun(spark, logger))

  /**
   * Main method to run the ETL
   * @param args arguments
   */
  final override def run(args: List[String]): IO[ExitCode] = {
    @transient lazy val spark: SparkSession = sparkBuilder()
    runIO(spark, logger).as(ExitCode.Success)
  }
}
