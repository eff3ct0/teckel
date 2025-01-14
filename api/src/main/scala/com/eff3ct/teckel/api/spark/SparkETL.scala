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

package com.eff3ct.teckel.api.spark

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
   * @return Spark session
   */
  private final def sparkBuilder(): SparkSession = {
    val sparkConf: SparkConf = new SparkConf()
    val master: String       = sparkConf.get("spark.master", "local[*]")
    val appName: String      = sparkConf.get("spark.app.name", "spark-etl")
    SparkSession.builder().config(sparkConf).master(master).appName(appName).getOrCreate()
  }

  /**
   * Run the ETL. This method should be implemented by the ETL.
   * @param spark Spark session
   * @param logger logger
   */
  def unsafeRun(args: List[String])(implicit spark: SparkSession, logger: Logger): Unit = {
    import cats.effect.unsafe.implicits.global
    runIO(args)(spark, logger).unsafeRunSync()
  }

  /**
   * Run the ETL using IO
   * @param spark Spark session
   * @param logger logger
   * @return IO
   */
  def runIO(args: List[String])(implicit spark: SparkSession, logger: Logger): IO[ExitCode] =
    IO(unsafeRun(args)(spark, logger)).map(_ => ExitCode.Success)

  /**
   * Main method to run the ETL
   * @param args arguments
   */
  final override def run(args: List[String]): IO[ExitCode] = {
    @transient lazy val spark: SparkSession = sparkBuilder()
    @transient lazy val logger: Logger      = LoggerFactory.getLogger(s"[ETL][$etlName]")
    runIO(args)(spark, logger)
  }

}
