import Library._
import sbt._

object Dependency {

  lazy val provided: String = "provided"

  /** Modules */

  lazy val model: Seq[ModuleID] =
    Seq(
      estatico.newtype
    )

  lazy val semantic: Seq[ModuleID] =
    Seq(
      spark.core,
      spark.sql,
      catsEffect.core,
      catsEffect.std
    ) ++ testing

  lazy val yaml: Seq[ModuleID] =
    Seq(
      circe.generic,
      circe.yaml,
      tofu.core,
      tofu.circe,
      catsEffect.core,
      catsEffect.std,
      fs2.io,
      fs2.core
    ) ++ testing

  lazy val testing: Seq[ModuleID] =
    Seq(
      test.scalaTest
    ).map(d => d % "test")

}
