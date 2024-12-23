package com.eff3ct.teckle.semantic.sources

import com.eff3ct.teckle.model.Source.Output
import com.eff3ct.teckle.semantic.core.Semantic
import org.apache.spark.sql.DataFrame

trait Exec[-S] extends Semantic[S, DataFrame, Unit]

object Exec {
  def apply[S: Exec]: Exec[S] = implicitly[Exec[S]]

  implicit val execOutput: Exec[Output] =
    (df, source) =>
      source match {
        case Output(_, format, mode, options, ref) =>
          df.write
            .format(format)
            .mode(mode)
            .options(options)
            .save(ref)
      }

}
