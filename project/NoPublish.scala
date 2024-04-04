import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin

object NoPublish extends AutoPlugin {

  override def requires: JvmPlugin.type = plugins.JvmPlugin

  override def projectSettings = Seq(
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )
}