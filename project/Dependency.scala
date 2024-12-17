import Library._
import sbt._

object Dependency {

  lazy val provided: String = "provided"

  lazy val commons: Seq[ModuleID] = Seq(
    spark.core,
    spark.sql,
    spark.hadoopCloud,
    hashicorp.vault,
    cats.core,
    cats.laws,
    catsEffect.core,
    catsEffect.std,
    database.postgresql,
    pureconfig.pureconfig,
    circe.generic,
    circe.yaml,
    tofu.core,
    tofu.circe,
    test.scalaTest % "test"
  )

}
