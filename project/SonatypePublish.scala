import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype.autoImport._
import xerial.sbt.Sonatype.sonatypeCentralHost

import scala.collection.Seq

object SonatypePublish {

  val PGPPassphrase: String      = "PGP_PASSPHRASE"
  val PGPSecret: String          = "PGP_SECRET"
  val SonatypeUser: String       = "SONATYPE_USERNAME"
  val SonatypePass: String       = "SONATYPE_PASSWORD"
  lazy val pgpPassphrase: String = sys.env.getOrElse(PGPPassphrase, "")
  lazy val pgpSecret: String     = sys.env.getOrElse(PGPSecret, "")
  lazy val sonatypeUser: String  = sys.env.getOrElse(SonatypeUser, "")
  lazy val sonatypePass: String  = sys.env.getOrElse(SonatypePass, "")

  def projectSettings: Seq[Setting[_]] = Seq(
    ThisBuild / organizationName       := "eff3ct",
    ThisBuild / organization           := "com.eff3ct",
    ThisBuild / publishTo              := sonatypePublishToBundle.value,
    ThisBuild / sonatypeCredentialHost := sonatypeCentralHost,
    ThisBuild / publishMavenStyle      := true,
    ThisBuild / publish / skip         := true,
    ThisBuild / versionScheme          := Some("early-semver"),
    ThisBuild / pomIncludeRepository   := { _ => false }
  )

}
