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

import com.eff3ct.teckel.model.{Asset, AssetRef, Context}
import com.eff3ct.teckel.model.Source._
import com.eff3ct.teckel.serializer.{Serializer, Validator}
import com.eff3ct.teckel.serializer.model.etl._
import com.eff3ct.teckel.transform.Rewrite

object DryRun {

  case class PlanStep(name: String, stepType: String, details: String, dependsOn: List[String])

  def explain(yamlContent: String): Either[Throwable, String] =
    Serializer[ETL].decode(yamlContent).map { etl =>
      val context          = Rewrite.rewrite(etl)
      val validationResult = Validator.validate(context)
      val validationReport = Validator.formatErrors(validationResult) match {
        case Some(errors) => s"\n--- Validation Errors ---\n$errors\n"
        case None         => "\n--- Validation: OK ---\n"
      }
      formatPlan(context) + validationReport
    }

  private def formatPlan(context: Context[Asset]): String = {
    val steps           = context.map { case (ref, asset) => planStep(ref, asset) }.toList
    val inputs          = steps.filter(_.stepType == "INPUT")
    val transformations = steps.filter(s => s.stepType != "INPUT" && s.stepType != "OUTPUT")
    val outputs         = steps.filter(_.stepType == "OUTPUT")

    val sb = new StringBuilder
    sb.append("=== Pipeline Execution Plan ===\n\n")

    sb.append("--- Inputs ---\n")
    inputs.foreach { s =>
      sb.append(s"  [${s.name}] ${s.details}\n")
    }

    if (transformations.nonEmpty) {
      sb.append("\n--- Transformations ---\n")
      transformations.foreach { s =>
        val deps = if (s.dependsOn.nonEmpty) s" (from: ${s.dependsOn.mkString(", ")})" else ""
        sb.append(s"  [${s.name}] ${s.stepType}$deps -> ${s.details}\n")
      }
    }

    sb.append("\n--- Outputs ---\n")
    outputs.foreach { s =>
      val deps = if (s.dependsOn.nonEmpty) s" (from: ${s.dependsOn.mkString(", ")})" else ""
      sb.append(s"  [${s.name}]$deps -> ${s.details}\n")
    }

    sb.append(
      s"\nTotal: ${inputs.size} inputs, ${transformations.size} transformations, ${outputs.size} outputs\n"
    )
    sb.toString
  }

  private def planStep(ref: AssetRef, asset: Asset): PlanStep =
    asset.source match {
      case s: Input =>
        PlanStep(ref, "INPUT", s"format=${s.format}, path=${s.sourceRef}", Nil)
      case s: Output =>
        PlanStep(
          ref,
          "OUTPUT",
          s"format=${s.format}, mode=${s.mode}, path=${s.sourceRef}",
          List(s.assetRef)
        )
      case s: Select =>
        PlanStep(
          ref,
          "SELECT",
          s"columns=[${s.columns.toList.mkString(", ")}]",
          List(s.assetRef)
        )
      case s: Where =>
        PlanStep(ref, "WHERE", s"condition=${s.condition}", List(s.assetRef))
      case s: GroupBy =>
        PlanStep(
          ref,
          "GROUP_BY",
          s"by=[${s.by.toList.mkString(", ")}], agg=[${s.aggregate.toList.mkString(", ")}]",
          List(s.assetRef)
        )
      case s: OrderBy =>
        PlanStep(
          ref,
          "ORDER_BY",
          s"by=[${s.by.toList.mkString(", ")}], order=${s.order.getOrElse("asc")}",
          List(s.assetRef)
        )
      case s: Join =>
        PlanStep(
          ref,
          "JOIN",
          s"tables=[${s.others.toList.map(_.name).mkString(", ")}]",
          s.assetRef :: s.others.toList.map(_.name)
        )
      case _: Transformation =>
        PlanStep(
          ref,
          "TRANSFORMATION",
          "custom",
          List(asset.source.asInstanceOf[Transformation].assetRef)
        )
    }
}
