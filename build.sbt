import Build.headerIOLicense

lazy val root =
  (project in file("."))
    .disablePlugins(Build)
    .disablePlugins(AssemblyPlugin, HeaderPlugin)
    .enablePlugins(NoPublish)
    .settings(name := "teckle")
    .aggregate(
      model,
      semantic,
      serializer,
      api,
      example
    )

/**
 * Serializers --> Model --> Semantic \ /
 * -----> API <---------
 */

lazy val model =
  (project in file("./model"))
    .settings(
      name          := "model",
      headerLicense := Some(headerIOLicense),
      libraryDependencies ++= Dependency.model
    )

lazy val semantic =
  (project in file("./semantic"))
    .dependsOn(model)
    .settings(
      name          := "semantic",
      headerLicense := Some(headerIOLicense),
      libraryDependencies ++= Dependency.semantic
    )

/** Serializer */
lazy val serializer =
  (project in file("./serializer"))
    .dependsOn(model)
    .settings(
      name           := "serializer",
      publish / skip := false,
      headerLicense  := Some(headerIOLicense),
      libraryDependencies ++= Dependency.serializer
    )

lazy val api =
  (project in file("./api"))
    .dependsOn(serializer, semantic)
    .settings(
      name           := "api",
      publish / skip := false,
      headerLicense  := Some(headerIOLicense),
      libraryDependencies ++= Dependency.api
    )

lazy val example =
  (project in file("./example"))
    .dependsOn(api)
    .settings(
      name          := "example",
      headerLicense := Some(headerIOLicense)
    )
