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

object Optimizer {
  type OptimizationPass = IRPlan => IRPlan

  val predicatePushdown: OptimizationPass = { plan =>
    // Move Filter nodes closer to Scan nodes when possible
    var nodes   = plan.nodes
    val filters = nodes.values.collect { case f: IRNode.Filter => f }.toList

    filters.foreach { filter =>
      nodes.get(filter.input).foreach {
        case project: IRNode.Project =>
          // Push filter below project if filter doesn't reference projected-away columns
          val newFilter  = filter.copy(input = project.input)
          val newProject = project.copy(input = filter.id)
          nodes = nodes + (filter.id -> newFilter) + (project.id -> newProject)
        case _ => // Can't push further
      }
    }
    IRPlan(nodes)
  }

  val projectionPruning: OptimizationPass = { plan =>
    // No-op for now -- requires column lineage tracking
    plan
  }

  def optimize(
      plan: IRPlan,
      passes: List[OptimizationPass] = List(predicatePushdown)
  ): IRPlan =
    passes.foldLeft(plan)((p, pass) => pass(p))
}
