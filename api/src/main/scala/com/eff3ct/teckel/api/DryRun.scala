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

  // scalastyle:off method.length cyclomatic.complexity
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
      case s: Distinct =>
        val cols =
          s.columns.map(c => s"columns=[${c.toList.mkString(", ")}]").getOrElse("all columns")
        PlanStep(ref, "DISTINCT", cols, List(s.assetRef))
      case s: Limit =>
        PlanStep(ref, "LIMIT", s"count=${s.count}", List(s.assetRef))
      case s: AddColumns =>
        val colDefs = s.columns.toList.map(c => s"${c.name}=${c.expression}").mkString(", ")
        PlanStep(ref, "ADD_COLUMNS", s"columns=[$colDefs]", List(s.assetRef))
      case s: DropColumns =>
        PlanStep(
          ref,
          "DROP_COLUMNS",
          s"columns=[${s.columns.toList.mkString(", ")}]",
          List(s.assetRef)
        )
      case s: RenameColumns =>
        val maps = s.mappings.map { case (k, v) => s"$k->$v" }.mkString(", ")
        PlanStep(ref, "RENAME_COLUMNS", s"mappings=[$maps]", List(s.assetRef))
      case s: CastColumns =>
        val cols = s.columns.toList.map(c => s"${c.name}:${c.targetType}").mkString(", ")
        PlanStep(ref, "CAST_COLUMNS", s"columns=[$cols]", List(s.assetRef))
      case s: Sql =>
        PlanStep(ref, "SQL", s"query=${s.query}", List(s.assetRef))
      case s: Union =>
        PlanStep(
          ref,
          "UNION",
          s"all=${s.all}, others=[${s.others.toList.mkString(", ")}]",
          s.assetRef :: s.others.toList
        )
      case s: Intersect =>
        PlanStep(
          ref,
          "INTERSECT",
          s"all=${s.all}, others=[${s.others.toList.mkString(", ")}]",
          s.assetRef :: s.others.toList
        )
      case s: Except =>
        PlanStep(
          ref,
          "EXCEPT",
          s"all=${s.all}, other=${s.other}",
          List(s.assetRef, s.other)
        )
      case s: Window =>
        val orderStr =
          s.orderBy.map(o => s", orderBy=[${o.toList.mkString(", ")}]").getOrElse("")
        val funcs = s.functions.toList.map(f => s"${f.expression} as ${f.alias}").mkString(", ")
        PlanStep(
          ref,
          "WINDOW",
          s"partitionBy=[${s.partitionBy.toList.mkString(", ")}]$orderStr, functions=[$funcs]",
          List(s.assetRef)
        )
      case s: Flatten =>
        val sep = s.separator.map(v => s", separator=$v").getOrElse("")
        val exp = s.explodeArrays.map(v => s", explodeArrays=$v").getOrElse("")
        PlanStep(ref, "FLATTEN", s"flatten$sep$exp", List(s.assetRef))
      case s: Sample =>
        val repl = s.withReplacement.map(v => s", withReplacement=$v").getOrElse("")
        val sd   = s.seed.map(v => s", seed=$v").getOrElse("")
        PlanStep(ref, "SAMPLE", s"fraction=${s.fraction}$repl$sd", List(s.assetRef))
      case s: Repartition =>
        val cols =
          s.columns.map(c => s", columns=[${c.toList.mkString(", ")}]").getOrElse("")
        PlanStep(ref, "REPARTITION", s"numPartitions=${s.numPartitions}$cols", List(s.assetRef))
      case s: Coalesce =>
        PlanStep(ref, "COALESCE", s"numPartitions=${s.numPartitions}", List(s.assetRef))
      case s: Rollup =>
        PlanStep(
          ref,
          "ROLLUP",
          s"by=[${s.by.toList.mkString(", ")}], agg=[${s.aggregate.toList.mkString(", ")}]",
          List(s.assetRef)
        )
      case s: Cube =>
        PlanStep(
          ref,
          "CUBE",
          s"by=[${s.by.toList.mkString(", ")}], agg=[${s.aggregate.toList.mkString(", ")}]",
          List(s.assetRef)
        )
      case s: Pivot =>
        val vals = s.values.map(v => s", values=[${v.mkString(", ")}]").getOrElse("")
        PlanStep(
          ref,
          "PIVOT",
          s"groupBy=[${s.groupBy.toList.mkString(", ")}], pivotColumn=${s.pivotColumn}, agg=[${s.aggregate.toList
              .mkString(", ")}]$vals",
          List(s.assetRef)
        )
      case s: Unpivot =>
        PlanStep(
          ref,
          "UNPIVOT",
          s"ids=[${s.ids.toList.mkString(", ")}], values=[${s.values.toList
              .mkString(", ")}], variableColumn=${s.variableColumn}, valueColumn=${s.valueColumn}",
          List(s.assetRef)
        )
      case s: Conditional =>
        val branches =
          s.branches.toList.map(b => s"WHEN ${b.condition} THEN ${b.value}").mkString(", ")
        val other = s.otherwise.map(v => s", OTHERWISE $v").getOrElse("")
        PlanStep(
          ref,
          "CONDITIONAL",
          s"outputColumn=${s.outputColumn}, branches=[$branches$other]",
          List(s.assetRef)
        )
    }
  // scalastyle:on method.length cyclomatic.complexity
}
