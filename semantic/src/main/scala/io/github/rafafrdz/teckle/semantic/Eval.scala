package io.github.rafafrdz.teckle.semantic

import io.github.rafafrdz.teckle.model.AST
import io.github.rafafrdz.teckle.model.AST.{Asset, Context, Output}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.annotation.tailrec

object Eval {

  @tailrec
  def eval(context: Context, asset: Asset)(implicit
      spark: SparkSession
  ): DataFrame =
    asset.source match {
      case AST.Input(format, options, ref) =>
        spark.read.format(format).options(options).load(ref)
      case AST.Output(assetRef, _, _, _) =>
        eval(context, context(assetRef))
    }

  def eval(context: Context)(implicit spark: SparkSession): Map[String, DataFrame] =
    context.collect { case (ref, asset @ Asset(_, _: Output)) =>
      ref -> eval(context, asset)
    }

}
