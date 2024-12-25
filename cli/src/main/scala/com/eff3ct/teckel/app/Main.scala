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

package com.eff3ct.teckel.app

import cats.effect.{Async, ExitCode, IO}
import com.eff3ct.teckel.api.core.Run
import com.eff3ct.teckel.api.spark.SparkETL
import com.eff3ct.teckel.io.Console
import com.eff3ct.teckel.semantic.core.EvalContext
import com.eff3ct.teckel.semantic.execution._
import fs2.io.file.Files
import org.apache.spark.sql.SparkSession
import org.slf4j

object Main extends SparkETL {

  override def runIO(
      args: List[String]
  )(implicit spark: SparkSession, logger: slf4j.Logger): IO[ExitCode] =
    execute[IO, Unit](args).compile.drain.as(ExitCode.Success)

  def execute[F[_]: Files: Async: Run, O: EvalContext](args: List[String]): fs2.Stream[F, O] =
    for {
      commands <- Console.command[F](args)
      result   <- Console.eval[F, O](commands)
    } yield result

  /**
   * Name of the ETL
   */
  override val etlName: String = "spark-etl-cli"
}
