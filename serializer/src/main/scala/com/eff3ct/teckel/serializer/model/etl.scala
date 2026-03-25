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

package com.eff3ct.teckel.serializer.model

import cats.data.NonEmptyList
import com.eff3ct.teckel.serializer.model.input._
import com.eff3ct.teckel.serializer.model.output._
import com.eff3ct.teckel.serializer.model.transformation._
import io.circe.generic.auto._

private[teckel] object etl {

  case class Hook(name: String, command: String)

  case class Hooks(
      preExecution: Option[List[Hook]],
      postExecution: Option[List[Hook]]
  )

  case class CachePolicy(
      autoCacheThreshold: Option[Int] = None,
      defaultStorageLevel: Option[String] = None
  )

  case class NotificationTarget(
      channel: String, // "webhook", "log", "file"
      url: Option[String] = None,
      path: Option[String] = None
  )

  case class NotificationConfig(
      onSuccess: Option[List[NotificationTarget]] = None,
      onFailure: Option[List[NotificationTarget]] = None
  )

  case class PipelineConfig(
      cache: Option[CachePolicy] = None,
      notifications: Option[NotificationConfig] = None
  )

  case class Template(
      name: String,
      parameters: Option[Map[String, String]] = None
  )

  case class ETL(
      input: NonEmptyList[Input],
      transformation: Option[NonEmptyList[Transformation]],
      output: NonEmptyList[Output],
      hooks: Option[Hooks] = None,
      config: Option[PipelineConfig] = None,
      templates: Option[List[Template]] = None,
      streamingInput: Option[NonEmptyList[StreamingInput]] = None,
      streamingOutput: Option[NonEmptyList[StreamingOutput]] = None,
      version: Option[String] = None
  )

  object ETL {
    def apply(input: NonEmptyList[Input], output: NonEmptyList[Output]): ETL =
      ETL(input, None, output)
  }

}
