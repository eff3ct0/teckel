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

import cats.effect.Sync
import cats.implicits._

object Notifier {

  sealed trait NotificationEvent
  case class PipelineSuccess(name: String, durationMs: Long) extends NotificationEvent
  case class PipelineFailure(name: String, error: String)    extends NotificationEvent

  def notify[F[_]: Sync](
      event: NotificationEvent,
      channel: String,
      url: Option[String]
  ): F[Unit] =
    channel match {
      case "log" =>
        Sync[F].delay {
          event match {
            case PipelineSuccess(name, dur) =>
              println(s"[notification] Pipeline '$name' completed successfully in ${dur}ms")
            case PipelineFailure(name, err) =>
              System.err.println(s"[notification] Pipeline '$name' failed: $err")
          }
        }
      case "webhook" =>
        url match {
          case Some(webhookUrl) =>
            Sync[F].delay {
              println(s"[notification] Would POST to $webhookUrl: $event")
            }
          case None =>
            Sync[F].delay(System.err.println("[notification] Webhook URL not configured"))
        }
      case "file" =>
        Sync[F].delay {
          println(s"[notification] Would write to file: $event")
        }
      case other =>
        Sync[F].delay(System.err.println(s"[notification] Unknown channel: $other"))
    }
}
