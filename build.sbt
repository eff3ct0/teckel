lazy val teckel: Project =
  project
    .in(file("."))
    .disablePlugins(AssemblyPlugin)
    .aggregate(core, yaml)
    .settings(
      name := "teckel"
    )

lazy val core: Project =
  (project in file("core"))
    .settings(
      name           := "teckel-core",
      publish / skip := false
    )

lazy val yaml: Project =
  (project in file("yaml"))
    .settings(
      name           := "teckel-yaml",
      publish / skip := false
    )
    .dependsOn(core)
