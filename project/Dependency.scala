import Library._
import sbt._
import sbtassembly.AssemblyPlugin

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
    sparkD ++ Seq(
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
      test.scalaTest,
      holdenkarau.sparktest
    ).map(d => d % "test")

  lazy val api: Seq[ModuleID] = sparkD ++ testing

  lazy val sparkD: Seq[ModuleID] = Seq(
    spark.core,
    spark.sql
  )

  implicit class ProjectOps(val prj: Project) extends AnyVal {
    def withKindProjector: Project = prj.settings(
      addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)
    )

    def withNoAssembly: Project = prj.disablePlugins(AssemblyPlugin)

    def withAssembly: Project =
      prj
        .enablePlugins(AssemblyPlugin)
        .settings(Assembly.projectSettings(None))

    def withAssembly(name: String): Project =
      prj
        .enablePlugins(AssemblyPlugin)
        .settings(Assembly.projectSettings(Some(name)))

  }

}
