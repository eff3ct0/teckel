/*
 * Invasion Order Software License Agreement
 *
 * This file is part of the proprietary software provided by Invasion Order.
 * Use of this file is governed by the terms and conditions outlined in the
 * Invasion Order Software License Agreement.
 *
 * Unauthorized copying, modification, or distribution of this file, via any
 * medium, is strictly prohibited. The software is provided "as is," without
 * warranty of any kind, express or implied.
 *
 * For the full license text, please refer to the LICENSE file included
 * with this distribution, or contact Invasion Order at contact@iorder.dev.
 *
 * (c) 2024 Invasion Order. All rights reserved.
 */

package io.github.rafafrdz.teckle.api.example

import cats.effect.IO
import io.github.rafafrdz.teckle.api.etl.etlF
import io.github.rafafrdz.teckle.api.spark.SparkETL
import io.github.rafafrdz.teckle.semantic.evaluation._
import io.github.rafafrdz.teckle.semantic.execution._
import org.apache.spark.sql.SparkSession
import org.slf4j.Logger

object EffectExample extends SparkETL {

  /**
   * Name of the ETL
   */
  override val etlName: String = "Effect Example"

  override def runIO(implicit spark: SparkSession, logger: Logger): IO[Unit] =
    etlF[IO, Unit]("example/src/main/resources/etl/simple.yaml")
}
