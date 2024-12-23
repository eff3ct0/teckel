package com.eff3ct.teckle.semantic.sources

import cats.data.NonEmptyList
import com.eff3ct.teckle.model.Source._
import com.eff3ct.teckle.semantic.SemanticA
import com.eff3ct.teckle.semantic.core.Semantic
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
