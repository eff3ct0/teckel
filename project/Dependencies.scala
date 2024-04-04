import sbt.*
object Dependencies {

  object Spark {
    lazy val core = "org.apache.spark" %% "spark-core" % Version.Spark
    lazy val sql  = "org.apache.spark" %% "spark-sql"  % Version.Spark
  }

  object Circe {
    lazy val yaml    = "io.circe" %% "circe-yaml"    % Version.Circe
    lazy val generic = "io.circe" %% "circe-generic" % Version.Circe
  }

  object Tofu {
    lazy val core  = "tf.tofu" %% "derevo-core"           % Version.Tofu
    lazy val circe = "tf.tofu" %% "derevo-circe-magnolia" % Version.Tofu
  }

  object ScalaTest {
    lazy val core     = "org.scalatest" %% "scalatest"          % Version.ScalaTest % Test
    lazy val flatspec = "org.scalatest" %% "scalatest-flatspec" % Version.ScalaTest % Test
  }

}
