lazy val root =
  (project in file("."))
    .disablePlugins(Build)
    .disablePlugins(AssemblyPlugin)
    .enablePlugins(NoPublish)
    .settings(name := "teckle")
    .aggregate(
      model,
      semantic,
      yaml,
      api
    )

/**
 * Serializers --> Model --> Semantic
 *     \                     /
 *      -----> API <---------
 */

lazy val model =
  (project in file("./model"))
    .settings(
      name := "model",
      libraryDependencies ++= Dependency.model
    )

lazy val semantic =
  (project in file("./semantic"))
    .settings(
      name := "semantic",
      libraryDependencies ++= Dependency.semantic
    )
    .dependsOn(model)

/** YAML Serializer */
lazy val yaml =
  (project in file("./yaml"))
    .settings(
      name           := "yaml",
      publish / skip := false,
      libraryDependencies ++= Dependency.yaml
    )
    .dependsOn(model)

lazy val api =
  (project in file("./api"))
    .settings(
      name           := "api",
      publish / skip := false
    )
    .dependsOn(yaml, semantic)

//addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
