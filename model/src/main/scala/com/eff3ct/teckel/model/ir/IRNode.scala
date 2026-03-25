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

package com.eff3ct.teckel.model.ir

import com.eff3ct.teckel.model.AssetRef

sealed trait IRNode {
  def id: AssetRef
  def dependencies: List[AssetRef]
}

object IRNode {

  case class Scan(id: AssetRef, format: String, path: String, options: Map[String, String])
      extends IRNode {
    def dependencies: List[AssetRef] = Nil
  }

  case class Project(id: AssetRef, input: AssetRef, columns: List[String]) extends IRNode {
    def dependencies: List[AssetRef] = List(input)
  }

  case class Filter(id: AssetRef, input: AssetRef, condition: String) extends IRNode {
    def dependencies: List[AssetRef] = List(input)
  }

  case class Aggregate(
      id: AssetRef,
      input: AssetRef,
      groupBy: List[String],
      aggregations: List[String]
  ) extends IRNode {
    def dependencies: List[AssetRef] = List(input)
  }

  case class Sort(id: AssetRef, input: AssetRef, sortBy: List[String], ascending: Boolean)
      extends IRNode {
    def dependencies: List[AssetRef] = List(input)
  }

  case class JoinNode(
      id: AssetRef,
      left: AssetRef,
      right: AssetRef,
      condition: String,
      joinType: String
  ) extends IRNode {
    def dependencies: List[AssetRef] = List(left, right)
  }

  case class Sink(
      id: AssetRef,
      input: AssetRef,
      format: String,
      path: String,
      mode: String,
      options: Map[String, String]
  ) extends IRNode {
    def dependencies: List[AssetRef] = List(input)
  }

  case class Transform(
      id: AssetRef,
      input: AssetRef,
      transformType: String,
      params: Map[String, String]
  ) extends IRNode {
    def dependencies: List[AssetRef] = List(input)
  }
}
