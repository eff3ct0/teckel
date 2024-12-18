import sbt._

object Library {

  object spark {
    lazy val core: ModuleID        = "org.apache.spark" %% "spark-core"         % Version.Spark
    lazy val sql: ModuleID         = "org.apache.spark" %% "spark-sql"          % Version.Spark
    lazy val hadoopCloud: ModuleID = "org.apache.spark" %% "spark-hadoop-cloud" % Version.Spark
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
    lazy val parser    = "io.circe" %% "circe-parser"    % Version.Circe
    lazy val yaml    = "io.circe" %% "circe-yaml"    % Version.Circe
    lazy val generic = "io.circe" %% "circe-generic" % Version.Circe
  }

  object fs2 {
    lazy val core: ModuleID = "co.fs2" %% "fs2-core" % Version.Fs2
    lazy val io: ModuleID   = "co.fs2" %% "fs2-io"   % Version.Fs2
  }

  object tofu {
    lazy val core  = "tf.tofu" %% "derevo-core"           % Version.Tofu
    lazy val circe = "tf.tofu" %% "derevo-circe-magnolia" % Version.Tofu
  }

  object estatico {
    lazy val newtype: ModuleID = "io.estatico" %% "newtype" % Version.Estatico
  }

  object pureconfig {
    lazy val pureconfig: ModuleID = "com.github.pureconfig" %% "pureconfig" % Version.Pureconfig
  }

  object database {
    lazy val postgresql: ModuleID = "org.postgresql" % "postgresql" % Version.Postgres
  }

  object hashicorp {
    lazy val vault: ModuleID = "io.github.jopenlibs" % "vault-java-driver" % Version.Vault
  }

  object test {
    lazy val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Version.ScalaTest
  }

}
