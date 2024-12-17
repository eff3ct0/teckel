import sbt.Keys._
import sbt._

object Publish {

  def projectSettings: Seq[Setting[_]] = Seq(
    publishMavenStyle    := true,
    publish / skip       := true,
    versionScheme        := Some("early-semver"),
    pomIncludeRepository := { _ => false },
    nexus(publishTo),
    credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
  ) ++ Assembly.publishAssemblyJar

  /**
   * Publish to Nexus repository
   * @param publishTo publishTo task
   * @return publishTo task
   */
  def nexus(publishTo: TaskKey[Option[Resolver]]): Setting[Task[Option[Resolver]]] =
    publishTo := {
      if (isSnapshot.value)
        Repository.maven(Repository.from(Path.userHome / ".sbt" / ".nexus-snapshots"))
      else Repository.maven(Repository.from(Path.userHome / ".sbt" / ".nexus-releases"))
    }

}
