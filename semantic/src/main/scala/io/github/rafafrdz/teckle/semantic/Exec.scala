package io.github.rafafrdz.teckle.semantic

import io.github.rafafrdz.teckle.model.AST
import io.github.rafafrdz.teckle.model.AST.{Asset, Context, Output}
import io.github.rafafrdz.teckle.semantic.Eval.eval
import org.apache.spark.sql.SparkSession

object Exec {

  def exec(context: Context)(implicit spark: SparkSession): Unit =
    context.foreach {
      case (ref, asset @ Asset(_, _: Output)) =>
        ref -> exec(context, asset)
      case _ => ()
    }

  def exec(context: Context, asset: Asset)(implicit
      spark: SparkSession
  ): Unit =
    asset.source match {
      case AST.Output(_, format, options, ref) =>
        eval(context, asset).write.format(format).options(options).save(ref)
      case _ => ()
    }
}
