import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype.autoImport._
import xerial.sbt.Sonatype._

import scala.collection.Seq

object SonatypePublish {

  def projectSettings: Seq[Setting[_]] = Seq(
    ThisBuild / publish / skip         := true,
    ThisBuild / versionScheme          := Some("early-semver"),
    ThisBuild / sonatypeCredentialHost := sonatypeCentralHost,
    ThisBuild / organization           := "com.eff3ct",
    ThisBuild / organizationName       := "eff3ct",
    ThisBuild / homepage               := Some(url("https://github.com/rafafrdz/teckel")),
    ThisBuild / licenses               := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    ThisBuild / scmInfo := Some(
      ScmInfo(
        browseUrl = url("https://github.com/rafafrdz/teckel"),
        connection = "scm:git:git@github.com:rafafrdz/teckel.git"
      )
    ),
    ThisBuild / developers := List(
      Developer(
        id = "rafafrdz",
        name = "Rafael Fernandez",
        email = "hi@rafaelfernandez.dev",
        url = url("https://rafaelfernandez.dev")
      )
    ),
    ThisBuild / sonatypeProjectHosting := Some(
      GitHubHosting("rafafrdz", "teckel", "hi@rafaelfernandez.dev")
    )
  )

}
