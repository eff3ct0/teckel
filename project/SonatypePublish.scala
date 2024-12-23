import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype.autoImport.{sonatypeCredentialHost, sonatypeRepository}
import xerial.sbt.Sonatype.sonatypeCentralHost

import scala.collection.Seq

object SonatypePublish {

  def projectSettings: Seq[Setting[_]] = Seq(
    ThisBuild / publish / skip         := true,
    ThisBuild / versionScheme          := Some("early-semver"),
    ThisBuild / sonatypeCredentialHost := sonatypeCentralHost
  )

}
