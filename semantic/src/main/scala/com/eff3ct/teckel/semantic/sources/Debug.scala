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

package com.eff3ct.teckel.semantic.sources

import cats.data.NonEmptyList
import com.eff3ct.teckel.model.Context
import com.eff3ct.teckel.model.Source._
import org.apache.spark.sql.expressions.{Window => SparkWindow}
import org.apache.spark.sql.functions.{col, explode_outer, expr}
import org.apache.spark.sql.types.{ArrayType, StructType}
import org.apache.spark.sql.{Column, DataFrame, RelationalGroupedDataset, SparkSession}

object Debug {

  def input[S <: Input](source: S)(implicit S: SparkSession): DataFrame =
    S.read.format(source.format).options(source.options).load(source.sourceRef)

  def output[S <: Output]: DataFrame => DataFrame = identity[DataFrame]

  /** Transformation */
  def transformation(source: Transformation, df: DataFrame, others: => Context[DataFrame])(implicit
      S: SparkSession
  ): DataFrame =
    source match {
      case s: Select        => select(df, s)
      case s: Where         => where(df, s)
      case s: GroupBy       => groupBy(df, s)
      case s: OrderBy       => orderBy(df, s)
      case s: Join          => join(s, df, others)
      case s: Distinct      => distinct(df, s)
      case s: Limit         => limit(df, s)
      case s: AddColumns    => addColumns(df, s)
      case s: DropColumns   => dropColumns(df, s)
      case s: RenameColumns => renameColumns(df, s)
      case s: CastColumns   => castColumns(df, s)
      case s: Sql           => sql(s, df, others)
      case s: Union         => union(s, df, others)
      case s: Intersect     => intersect(s, df, others)
      case s: Except        => except(s, df, others)
      case s: Window        => window(df, s)
      case s: Flatten       => flatten(df, s)
      case s: Sample        => sample(df, s)
      case s: Repartition   => repartition(df, s)
      case s: Coalesce      => coalesce(df, s)
      case s: Rollup        => rollup(df, s)
      case s: Cube          => cube(df, s)
      case s: Pivot         => pivot(df, s)
      case s: Unpivot       => unpivot(df, s)
      case s: Conditional   => conditional(df, s)
      case s: SCD2          => scd2(df, s)
      case s: Enrich        => enrich(df, s)
    }

  /** Select */
  def select[S <: Select](df: DataFrame, source: S): DataFrame =
    df.select(source.columns.toList.map(df(_)): _*)

  /** Where */
  def where[S <: Where](df: DataFrame, source: S): DataFrame =
    df.where(source.condition)

  /** GroupBy */
  def groupBy[S <: GroupBy](df: DataFrame, source: S): DataFrame = {
    val relDF: RelationalGroupedDataset = df.groupBy(source.by.toList.map(df(_)): _*)
    source.aggregate match {
      case NonEmptyList(a, Nil)  => relDF.agg(expr(a))
      case NonEmptyList(a, tail) => relDF.agg(expr(a), tail.map(expr): _*)
    }
  }

  /** OrderBy */
  def orderBy[S <: OrderBy](df: DataFrame, source: S): DataFrame = {
    val columns = source.order match {
      case Some(o) if o.toLowerCase == "desc" => source.by.toList.map(df(_).desc)
      case Some(o) if o.toLowerCase == "asc"  => source.by.toList.map(df(_).asc)
      case _                                  => source.by.toList.map(df(_).asc)
    }
    df.orderBy(columns: _*)
  }

  /** Distinct */
  def distinct[S <: Distinct](df: DataFrame, source: S): DataFrame =
    source.columns match {
      case Some(cols) => df.select(cols.toList.map(df(_)): _*).distinct
      case None       => df.distinct
    }

  /** Limit */
  def limit[S <: Limit](df: DataFrame, source: S): DataFrame =
    df.limit(source.count)

  /** AddColumns */
  def addColumns[S <: AddColumns](df: DataFrame, source: S): DataFrame =
    source.columns.foldLeft(df) { (acc, colDef) =>
      acc.withColumn(colDef.name, expr(colDef.expression))
    }

  /** DropColumns */
  def dropColumns[S <: DropColumns](df: DataFrame, source: S): DataFrame =
    df.drop(source.columns.toList: _*)

  /** RenameColumns */
  def renameColumns[S <: RenameColumns](df: DataFrame, source: S): DataFrame =
    source.mappings.foldLeft(df) { case (acc, (oldName, newName)) =>
      acc.withColumnRenamed(oldName, newName)
    }

  /** CastColumns */
  def castColumns[S <: CastColumns](df: DataFrame, source: S): DataFrame =
    source.columns.foldLeft(df) { (acc, castCol) =>
      acc.withColumn(castCol.name, col(castCol.name).cast(castCol.targetType))
    }

  /** Sql */
  def sql[S <: Sql](source: S, df: DataFrame, others: => Context[DataFrame])(implicit
      S: SparkSession
  ): DataFrame = {
    others.foreach { case (name, otherDf) => otherDf.createOrReplaceTempView(name) }
    df.createOrReplaceTempView(source.assetRef)
    S.sql(source.query)
  }

  /** Union */
  def union[S <: Union](source: S, df: DataFrame, context: Context[DataFrame]): DataFrame = {
    val otherDfs = source.others.map(ref => context(ref))
    otherDfs.foldLeft(df) { (acc, other) =>
      if (source.all) acc.unionAll(other) else acc.union(other)
    }
  }

  /** Intersect */
  def intersect[S <: Intersect](
      source: S,
      df: DataFrame,
      context: Context[DataFrame]
  ): DataFrame = {
    val otherDfs = source.others.map(ref => context(ref))
    otherDfs.foldLeft(df) { (acc, other) =>
      if (source.all) acc.intersectAll(other) else acc.intersect(other)
    }
  }

  /** Except */
  def except[S <: Except](source: S, df: DataFrame, context: Context[DataFrame]): DataFrame = {
    val other = context(source.other)
    if (source.all) df.exceptAll(other) else df.except(other)
  }

  /** Window */
  def window[S <: Window](df: DataFrame, source: S): DataFrame = {
    val partitionCols = source.partitionBy.toList.map(col)
    val windowSpec = source.orderBy match {
      case Some(orderCols) =>
        SparkWindow.partitionBy(partitionCols: _*).orderBy(orderCols.toList.map(col): _*)
      case None =>
        SparkWindow.partitionBy(partitionCols: _*)
    }
    source.functions.foldLeft(df) { (acc, func) =>
      acc.withColumn(func.alias, expr(func.expression).over(windowSpec))
    }
  }

  /** Flatten */
  def flatten[S <: Flatten](df: DataFrame, source: S): DataFrame = {
    val sep     = source.separator.getOrElse("_")
    val explode = source.explodeArrays.getOrElse(true)

    def flattenSchema(schema: StructType, prefix: String): Array[Column] =
      schema.fields.flatMap { field =>
        val colName = if (prefix.isEmpty) field.name else s"$prefix$sep${field.name}"
        field.dataType match {
          case st: StructType =>
            flattenSchema(st, colName)
          case _: ArrayType if explode =>
            Array(
              explode_outer(
                col(
                  if (prefix.isEmpty) field.name else s"$prefix.${field.name}"
                )
              ).as(colName)
            )
          case _ =>
            Array(
              col(
                if (prefix.isEmpty) field.name else s"$prefix.${field.name}"
              ).as(colName)
            )
        }
      }

    df.select(flattenSchema(df.schema, ""): _*)
  }

  /** Sample */
  def sample[S <: Sample](df: DataFrame, source: S): DataFrame = {
    val replacement = source.withReplacement.getOrElse(false)
    source.seed match {
      case Some(s) => df.sample(replacement, source.fraction, s)
      case None    => df.sample(replacement, source.fraction)
    }
  }

  /** Repartition */
  def repartition[S <: Repartition](df: DataFrame, source: S): DataFrame =
    source.columns match {
      case Some(cols) => df.repartition(source.numPartitions, cols.toList.map(col): _*)
      case None       => df.repartition(source.numPartitions)
    }

  /** Coalesce */
  def coalesce[S <: Coalesce](df: DataFrame, source: S): DataFrame =
    df.coalesce(source.numPartitions)

  /** Rollup */
  def rollup[S <: Rollup](df: DataFrame, source: S): DataFrame = {
    val relDF = df.rollup(source.by.toList.map(df(_)): _*)
    source.aggregate match {
      case NonEmptyList(a, Nil)  => relDF.agg(expr(a))
      case NonEmptyList(a, tail) => relDF.agg(expr(a), tail.map(expr): _*)
    }
  }

  /** Cube */
  def cube[S <: Cube](df: DataFrame, source: S): DataFrame = {
    val relDF = df.cube(source.by.toList.map(df(_)): _*)
    source.aggregate match {
      case NonEmptyList(a, Nil)  => relDF.agg(expr(a))
      case NonEmptyList(a, tail) => relDF.agg(expr(a), tail.map(expr): _*)
    }
  }

  /** Pivot */
  def pivot[S <: Pivot](df: DataFrame, source: S): DataFrame = {
    val grouped = df.groupBy(source.groupBy.toList.map(col): _*)
    val pivoted = source.values match {
      case Some(vals) => grouped.pivot(source.pivotColumn, vals)
      case None       => grouped.pivot(source.pivotColumn)
    }
    source.aggregate match {
      case NonEmptyList(a, Nil)  => pivoted.agg(expr(a))
      case NonEmptyList(a, tail) => pivoted.agg(expr(a), tail.map(expr): _*)
    }
  }

  /** Unpivot */
  def unpivot[S <: Unpivot](df: DataFrame, source: S): DataFrame = {
    val idCols    = source.ids.toList.map(col).toArray
    val valueCols = source.values.toList.map(col).toArray
    df.unpivot(idCols, valueCols, source.variableColumn, source.valueColumn)
  }

  /** Conditional */
  def conditional[S <: Conditional](df: DataFrame, source: S): DataFrame = {
    import org.apache.spark.sql.functions.{when, lit}
    val firstBranch = source.branches.head
    var colExpr = when(expr(firstBranch.condition), expr(firstBranch.value))
    source.branches.tail.foreach { branch =>
      colExpr = colExpr.when(expr(branch.condition), expr(branch.value))
    }
    val finalExpr = source.otherwise match {
      case Some(ow) => colExpr.otherwise(expr(ow))
      case None     => colExpr
    }
    df.withColumn(source.outputColumn, finalExpr)
  }

  /** SCD2 */
  def scd2[S <: SCD2](df: DataFrame, source: S)(implicit S: SparkSession): DataFrame = {
    import org.apache.spark.sql.functions._
    df.withColumn(source.startDateColumn, current_timestamp())
      .withColumn(source.endDateColumn, lit(null).cast("timestamp"))
      .withColumn(source.currentFlagColumn, lit(true))
  }

  /** Enrich */
  def enrich[S <: Enrich](df: DataFrame, source: S): DataFrame = {
    import org.apache.spark.sql.functions.lit
    df.withColumn(source.responseColumn, lit(s"[enrichment:${source.url}]"))
  }

  /** Join */
  def join[S <: Join](source: S, df: DataFrame, context: Context[DataFrame]): DataFrame = {
    val relations: NonEmptyList[(Relation, DataFrame)] =
      source.others.map(other => other -> context(other.name))

    relations.foldLeft(df) { case (left, (relation, right)) =>
      val condition = relation.on.map(cond => expr(cond)).reduce(_ && _)
      left.join(right, condition, relation.joinType)
    }
  }
}
