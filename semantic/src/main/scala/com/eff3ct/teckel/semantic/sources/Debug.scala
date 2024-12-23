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
import com.eff3ct.teckel.model.Source._
import com.eff3ct.teckel.semantic.SemanticA
import com.eff3ct.teckel.semantic.core.Semantic
import org.apache.spark.sql.functions.expr
import org.apache.spark.sql.{DataFrame, RelationalGroupedDataset, SparkSession}

trait Debug[-S] extends Semantic[S, DataFrame, DataFrame] {
  def debug(df: DataFrame, source: S): DataFrame = eval(df, source)
}

object Debug {
  def apply[S: Debug]: Debug[S] = implicitly[Debug[S]]

  implicit def input[S <: Input](implicit S: SparkSession): SemanticA[S, DataFrame] =
    Semantic.pure((source: S) =>
      S.read.format(source.format).options(source.options).load(source.sourceRef)
    )

  implicit val output: Debug[Output] =
    (df, _) => df

  /** Transformation */
  implicit val transformation: Debug[Transformation] =
    (df, source) =>
      source match {
        case s: Select  => Debug[Select].debug(df, s)
        case s: Where   => Debug[Where].debug(df, s)
        case s: GroupBy => Debug[GroupBy].debug(df, s)
        case s: OrderBy => Debug[OrderBy].debug(df, s)
      }

  /** Select */
  implicit val select: Debug[Select] =
    (df, source) => df.select(source.columns.toList.map(df(_)): _*)

  /** Where */
  implicit val whereS: Debug[Where] =
    (df, source) => df.where(source.condition)

  /** GroupBy */
  implicit val groupByS: Debug[GroupBy] =
    (df, source) => {
      val relDF: RelationalGroupedDataset = df.groupBy(source.by.toList.map(df(_)): _*)
      source.aggregate match {
        case NonEmptyList(a, Nil)  => relDF.agg(expr(a))
        case NonEmptyList(a, tail) => relDF.agg(expr(a), tail.map(expr): _*)
      }
    }

  /** OrderBy */
  implicit val orderByS: Debug[OrderBy] =
    (df, source) => df.orderBy(source.by.toList.map(df(_)): _*)
}
