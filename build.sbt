
lazy val root =
  (project in file("."))
    .disablePlugins(Build)
    .disablePlugins(AssemblyPlugin)
    .enablePlugins(NoPublish)
    .settings(name := "sdk")
    .aggregate(
      core
    )

lazy val core =
  (project in file("./core"))
    .settings(
      name           := "core",
      publish / skip := false
    )


//addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
