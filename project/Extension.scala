import sbt._
import sbtassembly.AssemblyPlugin
import scoverage.ScoverageKeys._
import scoverage._

object Extension {

  implicit class ProjectOps(project: Project) {

    def withNoAssembly: Project = project.disablePlugins(AssemblyPlugin)

    def withAssembly: Project =
      project
        .enablePlugins(AssemblyPlugin)
        .settings(Assembly.projectSettings(None))

    def withAssembly(name: String): Project =
      project
        .enablePlugins(AssemblyPlugin)
        .settings(Assembly.projectSettings(Some(name)))

    def withKindProjector: Project =
      project.settings(
        Seq(
          addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full)
        )
      )

    def withBetterMonadicFor: Project =
      project.settings(
        Seq(
          addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
        )
      )

    def withCoverage: Project =
      project
        .enablePlugins(ScoverageSbtPlugin)
        .settings(
          coverageEnabled          := true,
          coverageFailOnMinimum    := false,
          coverageMinimumStmtTotal := 30 // TODO. provisional
        )

  }

}
