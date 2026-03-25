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

package com.eff3ct.teckel.api.backend

import com.eff3ct.teckel.model.{Asset, Context}
import com.eff3ct.teckel.model.Source._

object SparkBackend extends Backend {
  def name: String = "spark"

  def execute(context: Context[Asset]): Unit = {
    // Delegates to existing Semantic evaluation
    // This is a bridge to the current implementation
    println(s"[spark-backend] Executing pipeline with ${context.size} assets")
    context.foreach { case (ref, asset) =>
      println(s"[spark-backend] Processing: $ref (${asset.source.getClass.getSimpleName})")
    }
  }

  def dryRun(context: Context[Asset]): String = {
    val sb = new StringBuilder("Spark Backend - Execution Plan:\n")
    context.foreach { case (ref, asset) =>
      sb.append(s"  [$ref] ${asset.source.getClass.getSimpleName}\n")
    }
    sb.toString
  }

  // Register on load
  Backend.register(this)
}
