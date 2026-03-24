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
import org.apache.spark.sql.functions.expr
import org.apache.spark.sql.{DataFrame, RelationalGroupedDataset, SparkSession}

object Debug {

  def input[S <: Input](source: S)(implicit S: SparkSession): DataFrame =
    S.read.format(source.format).options(source.options).load(source.sourceRef)

  def output[S <: Output]: DataFrame => DataFrame = identity[DataFrame]

  /** Transformation */
  def transformation(source: Transformation, df: DataFrame, others: => Context[DataFrame]): DataFrame =
    source match {
      case s: Select  => select(df, s)
      case s: Where   => where(df, s)
      case s: GroupBy => groupBy(df, s)
      case s: OrderBy => orderBy(df, s)
      case s: Join    => join(s, df, others)
      case s: Distinct => distinct(df, s)
      case s: Limit    => limit(df, s)
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
