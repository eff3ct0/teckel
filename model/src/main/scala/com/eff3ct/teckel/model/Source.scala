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

package com.eff3ct.teckel.model

import cats.data.NonEmptyList

sealed trait Source

object Source {

  case class Input(format: Format, options: Options, sourceRef: SourceRef) extends Source

  case class Output(
      assetRef: AssetRef,
      format: Format,
      mode: Mode,
      options: Options,
      sourceRef: SourceRef
  ) extends Source
      with WithAssetRef

  trait WithAssetRef {
    def assetRef: AssetRef
  }

  sealed trait Transformation extends Source with WithAssetRef

  case class Select(assetRef: AssetRef, columns: NonEmptyList[Column]) extends Transformation

  case class Where(assetRef: AssetRef, condition: Condition) extends Transformation

  case class GroupBy(assetRef: AssetRef, by: NonEmptyList[Column], aggregate: NonEmptyList[Column])
      extends Transformation

  case class OrderBy(assetRef: AssetRef, by: NonEmptyList[Column], order: Option[Order])
      extends Transformation
}
