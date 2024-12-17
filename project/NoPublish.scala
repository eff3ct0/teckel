import sbt.Keys._
import sbt._
import sbt.plugins._

object NoPublish extends AutoPlugin {

  override def requires: JvmPlugin.type = plugins.JvmPlugin

  override def projectSettings: Seq[Setting[?]] = Seq(
    publishArtifact := false,
    publish         := {},
    publishLocal    := {}
  )
}
