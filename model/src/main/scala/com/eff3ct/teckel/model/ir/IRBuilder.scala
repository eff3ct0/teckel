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

import com.eff3ct.teckel.model.{Asset, AssetRef, Context}
import com.eff3ct.teckel.model.Source._

object IRBuilder {

  def fromContext(context: Context[Asset]): IRPlan = {
    val nodes = context.map { case (ref, asset) =>
      ref -> toIRNode(ref, asset)
    }
    IRPlan(nodes)
  }

  private def toIRNode(ref: AssetRef, asset: Asset): IRNode = asset.source match {
    case s: Input =>
      IRNode.Scan(ref, s.format, s.sourceRef, s.options)
    case s: Output =>
      IRNode.Sink(ref, s.assetRef, s.format, s.sourceRef, s.mode, s.options)
    case s: Select =>
      IRNode.Project(ref, s.assetRef, s.columns.toList)
    case s: Where =>
      IRNode.Filter(ref, s.assetRef, s.condition)
    case s: GroupBy =>
      IRNode.Aggregate(ref, s.assetRef, s.by.toList, s.aggregate.toList)
    case s: OrderBy =>
      IRNode.Sort(ref, s.assetRef, s.by.toList, s.order.forall(_.toLowerCase != "desc"))
    case s: Join =>
      val firstRel = s.others.head
      IRNode.JoinNode(
        ref,
        s.assetRef,
        firstRel.name,
        firstRel.on.mkString(" AND "),
        firstRel.joinType
      )
    case t: Transformation =>
      IRNode.Transform(ref, t.assetRef, t.getClass.getSimpleName, Map.empty)
  }
}
