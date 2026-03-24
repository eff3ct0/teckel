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

package com.eff3ct.teckel.serializer

import io.circe.Json
import io.circe.yaml.parser
import io.circe.yaml.Printer

object ConfigMerger {

  private val yamlPrinter: Printer = Printer.spaces2

  def merge(base: String, overlay: String): Either[io.circe.ParsingFailure, String] =
    for {
      baseJson    <- parser.parse(base)
      overlayJson <- parser.parse(overlay)
    } yield yamlPrinter.pretty(deepMerge(baseJson, overlayJson))

  def mergeAll(configs: List[String]): Either[io.circe.ParsingFailure, String] =
    configs match {
      case Nil         => Right("")
      case head :: Nil => Right(head)
      case head :: tail =>
        tail.foldLeft(Right(head): Either[io.circe.ParsingFailure, String]) { (acc, next) =>
          acc.flatMap(base => merge(base, next))
        }
    }

  private def deepMerge(base: Json, overlay: Json): Json =
    (base.asObject, overlay.asObject) match {
      case (Some(baseObj), Some(overlayObj)) =>
        val merged = overlayObj.toList.foldLeft(baseObj) { case (acc, (key, value)) =>
          acc(key) match {
            case Some(existing) => acc.add(key, deepMerge(existing, value))
            case None           => acc.add(key, value)
          }
        }
        Json.fromJsonObject(merged)
      case _ => overlay
    }
}
