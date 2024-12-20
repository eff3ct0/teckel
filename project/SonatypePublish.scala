import sbt.Keys._
import sbt._

import scala.collection.Seq

object SonatypePublish {

  def projectSettings: Seq[Setting[_]] = Seq(
    ThisBuild / publish / skip       := true,
    ThisBuild / versionScheme        := Some("early-semver"),
    ThisBuild / pomIncludeRepository := { _ => false }
  )

}
