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
import com.eff3ct.teckel.serializer.Serializer
import com.eff3ct.teckel.serializer.model.etl._
import com.eff3ct.teckel.transform.Rewrite

object DocGen {

  def generate(yamlContent: String): Either[Throwable, String] =
    Serializer[ETL].decode(yamlContent).map { etl =>
      val context = Rewrite.rewrite(etl)
      formatDoc(context)
    }

  private def formatDoc(context: Context[Asset]): String = {
    val sb = new StringBuilder
    sb.append("# Pipeline Documentation\n\n")

    val inputs          = context.filter(_._2.source.isInstanceOf[Input])
    val outputs         = context.filter(_._2.source.isInstanceOf[Output])
    val transformations = context.filter(e => e._2.source.isInstanceOf[Transformation])

    sb.append("## Data Sources\n\n")
    sb.append("| Name | Format | Path |\n")
    sb.append("|------|--------|------|\n")
    inputs.foreach { case (ref, asset) =>
      val s = asset.source.asInstanceOf[Input]
      sb.append(s"| $ref | ${s.format} | ${s.sourceRef} |\n")
    }

    if (transformations.nonEmpty) {
      sb.append("\n## Transformations\n\n")
      transformations.foreach { case (ref, asset) =>
        sb.append(s"### $ref\n\n")
        sb.append(describeTransformation(asset.source.asInstanceOf[Transformation]))
        sb.append("\n")
      }
    }

    sb.append("## Outputs\n\n")
    sb.append("| Name | Format | Mode | Path |\n")
    sb.append("|------|--------|------|------|\n")
    outputs.foreach { case (ref, asset) =>
      val s = asset.source.asInstanceOf[Output]
      sb.append(s"| ${s.assetRef} | ${s.format} | ${s.mode} | ${s.sourceRef} |\n")
    }

    sb.append("\n## Data Flow\n\n")
    sb.append("```\n")
    context.foreach { case (ref, asset) =>
      asset.source match {
        case _: Input =>
          sb.append(s"[INPUT] $ref\n")
        case s: Output =>
          sb.append(s"  ${s.assetRef} --> [OUTPUT] $ref (${s.format}:${s.sourceRef})\n")
        case t: Transformation =>
          sb.append(s"  ${t.assetRef} --> [$ref]\n")
      }
    }
    sb.append("```\n")

    sb.toString
  }

  private def describeTransformation(t: Transformation): String = t match {
    case s: Select =>
      s"- **Type**: Select\n- **From**: ${s.assetRef}\n- **Columns**: ${s.columns.toList.mkString(", ")}\n"
    case s: Where =>
      s"- **Type**: Filter\n- **From**: ${s.assetRef}\n- **Condition**: `${s.condition}`\n"
    case s: GroupBy =>
      s"- **Type**: Group By\n- **From**: ${s.assetRef}\n- **Group By**: ${s.by.toList.mkString(
          ", "
        )}\n- **Aggregations**: ${s.aggregate.toList.mkString(", ")}\n"
    case s: OrderBy =>
      s"- **Type**: Order By\n- **From**: ${s.assetRef}\n- **Order By**: ${s.by.toList.mkString(", ")} ${s.order
          .getOrElse("asc")}\n"
    case s: Join =>
      s"- **Type**: Join\n- **From**: ${s.assetRef}\n- **Joins**: ${s.others.toList
          .map(r => s"${r.name} (${r.joinType})")
          .mkString(", ")}\n"
    case s: Distinct =>
      s"- **Type**: Distinct\n- **From**: ${s.assetRef}\n${s.columns
          .map(c => s"- **Columns**: ${c.toList.mkString(", ")}\n")
          .getOrElse("")}"
    case s: Limit =>
      s"- **Type**: Limit\n- **From**: ${s.assetRef}\n- **Count**: ${s.count}\n"
    case s: Union =>
      s"- **Type**: Union${if (s.all) " All"
        else ""}\n- **From**: ${s.assetRef}\n- **With**: ${s.others.toList.mkString(", ")}\n"
    case s: Intersect =>
      s"- **Type**: Intersect${if (s.all) " All"
        else ""}\n- **From**: ${s.assetRef}\n- **With**: ${s.others.toList.mkString(", ")}\n"
    case s: Except =>
      s"- **Type**: Except${if (s.all) " All" else ""}\n- **From**: ${s.assetRef}\n- **Other**: ${s.other}\n"
    case s: AddColumns =>
      s"- **Type**: Add Columns\n- **From**: ${s.assetRef}\n- **Columns**: ${s.columns.toList
          .map(c => s"${c.name} = ${c.expression}")
          .mkString(", ")}\n"
    case s: DropColumns =>
      s"- **Type**: Drop Columns\n- **From**: ${s.assetRef}\n- **Columns**: ${s.columns.toList.mkString(", ")}\n"
    case s: RenameColumns =>
      s"- **Type**: Rename Columns\n- **From**: ${s.assetRef}\n- **Mappings**: ${s.mappings
          .map { case (k, v) => s"$k -> $v" }
          .mkString(", ")}\n"
    case s: CastColumns =>
      s"- **Type**: Cast Columns\n- **From**: ${s.assetRef}\n- **Columns**: ${s.columns.toList
          .map(c => s"${c.name} as ${c.targetType}")
          .mkString(", ")}\n"
    case s: Sql =>
      s"- **Type**: SQL\n- **From**: ${s.assetRef}\n- **Query**: `${s.query}`\n"
    case s: Window =>
      s"- **Type**: Window\n- **From**: ${s.assetRef}\n- **Partition By**: ${s.partitionBy.toList
          .mkString(", ")}\n${s.orderBy.map(o => s"- **Order By**: ${o.toList.mkString(", ")}\n").getOrElse("")}- **Functions**: ${s.functions.toList
          .map(f => s"${f.expression} as ${f.alias}")
          .mkString(", ")}\n"
    case s: Flatten =>
      s"- **Type**: Flatten\n- **From**: ${s.assetRef}\n${s.separator
          .map(sep => s"- **Separator**: $sep\n")
          .getOrElse("")}${s.explodeArrays.map(e => s"- **Explode Arrays**: $e\n").getOrElse("")}"
    case s: Sample =>
      s"- **Type**: Sample\n- **From**: ${s.assetRef}\n- **Fraction**: ${s.fraction}\n${s.withReplacement
          .map(w => s"- **With Replacement**: $w\n")
          .getOrElse("")}${s.seed.map(seed => s"- **Seed**: $seed\n").getOrElse("")}"
    case s: Repartition =>
      s"- **Type**: Repartition\n- **From**: ${s.assetRef}\n- **Partitions**: ${s.numPartitions}\n${s.columns
          .map(c => s"- **Columns**: ${c.toList.mkString(", ")}\n")
          .getOrElse("")}"
    case s: Coalesce =>
      s"- **Type**: Coalesce\n- **From**: ${s.assetRef}\n- **Partitions**: ${s.numPartitions}\n"
    case s: Rollup =>
      s"- **Type**: Rollup\n- **From**: ${s.assetRef}\n- **By**: ${s.by.toList.mkString(", ")}\n- **Aggregations**: ${s.aggregate.toList
          .mkString(", ")}\n"
    case s: Cube =>
      s"- **Type**: Cube\n- **From**: ${s.assetRef}\n- **By**: ${s.by.toList.mkString(", ")}\n- **Aggregations**: ${s.aggregate.toList
          .mkString(", ")}\n"
    case s: Pivot =>
      s"- **Type**: Pivot\n- **From**: ${s.assetRef}\n- **Group By**: ${s.groupBy.toList.mkString(
          ", "
        )}\n- **Pivot Column**: ${s.pivotColumn}\n${s.values
          .map(v => s"- **Values**: ${v.mkString(", ")}\n")
          .getOrElse("")}- **Aggregations**: ${s.aggregate.toList.mkString(", ")}\n"
    case s: Unpivot =>
      s"- **Type**: Unpivot\n- **From**: ${s.assetRef}\n- **IDs**: ${s.ids.toList.mkString(", ")}\n- **Values**: ${s.values.toList
          .mkString(", ")}\n- **Variable Column**: ${s.variableColumn}\n- **Value Column**: ${s.valueColumn}\n"
    case s: Conditional =>
      s"- **Type**: Conditional\n- **From**: ${s.assetRef}\n- **Output Column**: ${s.outputColumn}\n- **Branches**: ${s.branches.toList
          .map(b => s"when ${b.condition} then ${b.value}")
          .mkString("; ")}\n${s.otherwise.map(o => s"- **Otherwise**: $o\n").getOrElse("")}"
    case s: SCD2 =>
      s"- **Type**: SCD Type 2\n- **From**: ${s.assetRef}\n- **Key Columns**: ${s.keyColumns.toList
          .mkString(", ")}\n- **Track Columns**: ${s.trackColumns.toList
          .mkString(", ")}\n- **Start Date**: ${s.startDateColumn}\n- **End Date**: ${s.endDateColumn}\n- **Current Flag**: ${s.currentFlagColumn}\n"
    case s: Enrich =>
      s"- **Type**: API Enrichment\n- **From**: ${s.assetRef}\n- **URL**: ${s.url}\n- **Method**: ${s.method
          .getOrElse("GET")}\n- **Key Column**: ${s.keyColumn}\n- **Response Column**: ${s.responseColumn}\n"
    case s: SchemaEnforce =>
      s"- **Type**: Schema Enforce\n- **From**: ${s.assetRef}\n- **Mode**: ${s.mode}\n- **Columns**: ${s.columns.toList
          .map(c => s"${c.name}:${c.dataType}${c.nullable.map(n => s" nullable=$n").getOrElse("")}${c.default.map(d => s" default=$d").getOrElse("")}")
          .mkString(", ")}\n"
    case s: Assertion =>
      val checks = s.checks.toList
        .map { c =>
          val col  = c.column.map(v => s"on column `$v`").getOrElse("dataset-level")
          val desc = c.description.getOrElse(c.rule)
          s"$desc ($col, rule=`${c.rule}`)"
        }
        .mkString(", ")
      s"- **Type**: Data Quality Assertion\n- **From**: ${s.assetRef}\n- **On Failure**: ${s.onFailure}\n- **Checks**: $checks\n"
  }
}
