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

object DataFusionBackend extends Backend {
  def name: String = "datafusion"

  def execute(context: Context[Asset]): Unit = {
    println("[datafusion-backend] DataFusion backend is not yet implemented")
    println(
      "[datafusion-backend] This is a placeholder for future DataFusion/DataFusion-Comet integration"
    )
    throw new UnsupportedOperationException(
      "DataFusion backend is planned for a future release. " +
        "See docs/architecture/multi-backend.md for the roadmap."
    )
  }

  def dryRun(context: Context[Asset]): String =
    "DataFusion Backend - Not yet implemented\nSee docs/architecture/multi-backend.md"

  Backend.register(this)
}
