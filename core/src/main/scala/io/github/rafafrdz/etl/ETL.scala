package io.github.rafafrdz.etl

import io.circe._
import io.github.rafafrdz.core.AST.Asset
import io.github.rafafrdz.core.{Eval, Exec}
import io.github.rafafrdz.yaml.{serializer, transform}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.io.Source

object ETL {

  type Ref[T] = Map[String, T]

  def op[T](path: String, f: Map[String, Asset] => T): Either[Error, T] =
    for {
      yaml <- Right(Source.fromFile(path).mkString) // todo. improve
      etl  <- serializer.parse(yaml)
      ref = transform.map(etl)
    } yield f(ref)

  def eval(path: String)(implicit spark: SparkSession): Either[Error, Map[String, DataFrame]] =
    op[Ref[DataFrame]](path, Eval.eval)

  def exec(path: String)(implicit spark: SparkSession): Either[Error, Unit] =
    op[Unit](path, Exec.exec)

}
