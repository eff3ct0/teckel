import Library._
import sbt._

object Dependency {

  /** Modules */

  lazy val model: Seq[ModuleID] =
    Seq(
      cats.core,
      cats.laws
    )

  lazy val semantic: Seq[ModuleID] =
    Seq(
      spark.core,
      spark.sql,
      catsEffect.core,
      catsEffect.std
    ) ++ testing

  lazy val serializer: Seq[ModuleID] =
    sparkD ++ Seq(
      circe.parser,
      circe.generic,
      circe.yaml,
      catsEffect.core,
      catsEffect.std,
      fs2.io,
      fs2.core
    ) ++ testing

  lazy val testing: Seq[ModuleID] =
    Seq(
      test.scalaTest,
      holdenkarau.sparktest
    ).map(d => d % "test")

  lazy val api: Seq[ModuleID] = sparkD ++ testing

  lazy val sparkD: Seq[ModuleID] = Seq(
    spark.core,
    spark.sql
  )

}
