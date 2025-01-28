import Extension.ProjectOps

lazy val root =
  (project in file("."))
    .disablePlugins(BuildPlugin, AssemblyPlugin, HeaderPlugin)
    .settings(
      name           := "teckel",
      publish / skip := true
    )
    .aggregate(
      model,
      semantic,
      serializer,
      api,
      cli
    )

/**
 * Serializers --> Model --> Semantic
 *      \-----> API <--------- /
 */

lazy val model =
  (project in file("./model"))
    .settings(
      name := "teckel-model",
      libraryDependencies ++= Dependency.model,
      publish / skip := false
    )
    .withNoAssembly

lazy val semantic =
  (project in file("./semantic"))
    .dependsOn(model)
    .settings(
      name := "teckel-semantic",
      libraryDependencies ++= Dependency.semantic,
      publish / skip := false
    )
    .withNoAssembly
    .withKindProjector
    .withCoverage

/** Serializer */
lazy val serializer =
  (project in file("./serializer"))
    .dependsOn(model)
    .settings(
      name           := "teckel-serializer",
      publish / skip := false,
      libraryDependencies ++= Dependency.serializer
    )
    .withNoAssembly
    .withCoverage

lazy val api =
  (project in file("./api"))
    .dependsOn(model, semantic, serializer)
    .settings(
      name           := "teckel-api",
      publish / skip := false,
      libraryDependencies ++= Dependency.api
    )
    .withNoAssembly
    .withCoverage

lazy val cli =
  (project in file("./cli"))
    .dependsOn(api)
    .settings(
      name           := "teckel-cli",
      publish / skip := false,
      libraryDependencies ++= Dependency.sparkD
    )
    .withAssembly("teckel-etl")

lazy val example =
  (project in file("./example"))
    .dependsOn(api)
    .settings(
      name := "teckel-example",
      libraryDependencies ++= Dependency.sparkD
    )
