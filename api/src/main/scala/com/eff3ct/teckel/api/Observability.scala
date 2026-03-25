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

package com.eff3ct.teckel.api

import cats.effect.{Clock, Sync}
import cats.implicits._

object Observability {

  case class PipelineMetrics(
      pipelineName: String,
      startTime: Long,
      endTime: Long,
      durationMs: Long,
      assetsProcessed: Int,
      status: String,
      errors: List[String]
  )

  def timed[F[_]: Sync: Clock, A](name: String)(fa: F[A]): F[(A, PipelineMetrics)] =
    for {
      start  <- Clock[F].realTime.map(_.toMillis)
      result <- fa.attempt
      end    <- Clock[F].realTime.map(_.toMillis)
      metrics = result match {
        case Right(_) =>
          PipelineMetrics(name, start, end, end - start, 0, "SUCCESS", Nil)
        case Left(err) =>
          PipelineMetrics(name, start, end, end - start, 0, "FAILED", List(err.getMessage))
      }
      _     <- Sync[F].delay(logMetrics(metrics))
      value <- Sync[F].fromEither(result)
    } yield (value, metrics)

  private def logMetrics(m: PipelineMetrics): Unit = {
    println(s"[metrics] pipeline=${m.pipelineName} status=${m.status} duration=${m.durationMs}ms")
    if (m.errors.nonEmpty)
      m.errors.foreach(e => println(s"[metrics] error: $e"))
  }

  def formatMetricsJson(m: PipelineMetrics): String =
    s"""{"pipeline":"${m.pipelineName}","status":"${m.status}","durationMs":${m.durationMs},"startTime":${m.startTime},"endTime":${m.endTime},"assetsProcessed":${m.assetsProcessed},"errors":[${m.errors
        .map(e => s""""$e"""")
        .mkString(",")}]}"""
}
