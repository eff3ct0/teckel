import Library._
import sbt._

object Dependency {

  lazy val provided: String = "provided"

  /** Modules */

  lazy val model: Seq[ModuleID] =
    Seq(
      estatico.newtype,
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
    Seq(
      circe.parser,
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

  lazy val api: Seq[ModuleID] = testing

  implicit class ProjectOps(val prj: Project) extends AnyVal {
    def withKindProjector: Project = prj.settings(
      addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)
    )
  }

}
