import sbt._

object Library {

  object spark {
    lazy val core: ModuleID        = "org.apache.spark" %% "spark-core"         % Version.Spark % Provided
    lazy val sql: ModuleID         = "org.apache.spark" %% "spark-sql"          % Version.Spark % Provided
    lazy val hadoopCloud: ModuleID = "org.apache.spark" %% "spark-hadoop-cloud" % Version.Spark % Provided
  }

  object cats {
    lazy val core: ModuleID = "org.typelevel" %% "cats-core" % Version.Cats
    lazy val laws: ModuleID = "org.typelevel" %% "cats-laws" % Version.Cats
  }

  object catsEffect {
    lazy val core: ModuleID = "org.typelevel" %% "cats-effect"     % Version.CatsEffect
    lazy val std: ModuleID  = "org.typelevel" %% "cats-effect-std" % Version.CatsEffect
  }

  object circe {
    lazy val parser  = "io.circe" %% "circe-parser"  % Version.Circe
    lazy val generic = "io.circe" %% "circe-generic" % Version.Circe
    lazy val yaml    = "io.circe" %% "circe-yaml"    % Version.CirceYaml
  }

  object fs2 {
    lazy val core: ModuleID = "co.fs2" %% "fs2-core" % Version.Fs2
    lazy val io: ModuleID   = "co.fs2" %% "fs2-io"   % Version.Fs2
  }

  object test {
    lazy val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Version.ScalaTest
  }
  object holdenkarau {
    lazy val sparktest: ModuleID = "com.holdenkarau" %% "spark-testing-base" % Version.HoldenVersion
  }

}
