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

case class IRPlan(nodes: Map[AssetRef, IRNode]) {

  def roots: List[IRNode] = nodes.values.filter(_.dependencies.isEmpty).toList

  def sinks: List[IRNode] = nodes.values.collect { case s: IRNode.Sink => s }.toList

  def topologicalOrder: List[IRNode] = {
    var visited = Set.empty[AssetRef]
    var result  = List.empty[IRNode]

    def visit(id: AssetRef): Unit =
      if (!visited.contains(id)) {
        visited += id
        nodes.get(id).foreach { node =>
          node.dependencies.foreach(visit)
          result = result :+ node
        }
      }

    nodes.keys.foreach(visit)
    result
  }

  def dependentsOf(id: AssetRef): List[IRNode] =
    nodes.values.filter(_.dependencies.contains(id)).toList

  def fanOut(id: AssetRef): Int = dependentsOf(id).size
}
