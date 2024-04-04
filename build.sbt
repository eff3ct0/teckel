lazy val root = (project in file("."))
  .settings(
    name := "teckel",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core"            % "3.2.1",
      "org.apache.spark" %% "spark-sql"             % "3.2.1",
      "io.circe"         %% "circe-yaml"            % "0.13.0",
      "io.circe"         %% "circe-generic"         % "0.13.0",
      "tf.tofu"          %% "derevo-core"           % "0.13.0",
      "tf.tofu"          %% "derevo-circe-magnolia" % "0.13.0",
      "org.scalatest"    %% "scalatest"             % "3.2.18" % Test,
      "org.scalatest"    %% "scalatest-flatspec"    % "3.2.18" % Test
    )
  )
