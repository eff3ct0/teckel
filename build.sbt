
lazy val root =
  (project in file("."))
    .disablePlugins(BuildPlugin, AssemblyPlugin, HeaderPlugin)
    .settings(
      name           := "teckle",
      publish / skip := true
    )
    .aggregate(
      model,
      semantic,
      serializer,
      api,
      example
    )

/**
 * Serializers --> Model --> Semantic
 *      \-----> API <--------- /
 */

lazy val model =
  (project in file("./model"))
    .settings(
      name := "teckle-model",
      libraryDependencies ++= Dependency.model
    )

lazy val semantic =
  (project in file("./semantic"))
    .dependsOn(model)
    .settings(
      name := "teckle-semantic",
      libraryDependencies ++= Dependency.semantic
    )

/** Serializer */
lazy val serializer =
  (project in file("./serializer"))
    .dependsOn(model)
    .settings(
      name           := "teckle-serializer",
      publish / skip := false,
      libraryDependencies ++= Dependency.serializer
    )

lazy val api =
  (project in file("./api"))
    .dependsOn(serializer, semantic)
    .settings(
      name           := "teckle-api",
      publish / skip := false,
      libraryDependencies ++= Dependency.api
    )

lazy val example =
  (project in file("./example"))
    .dependsOn(api)
    .settings(
      name := "teckle-example"
    )
