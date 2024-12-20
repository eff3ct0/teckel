import sbt.Keys._
import sbt._

object NexusPublish extends AutoPlugin {

  override def projectSettings: Seq[Setting[_]] = Seq(
    publishMavenStyle    := true,
    publish / skip       := true,
    versionScheme        := Some("early-semver"),
    pomIncludeRepository := { _ => false },
    nexus(publishTo),
    setCredentials(credentials)
  )

  val LocalNexusCredentials: String = "LOCAL_NEXUS_CREDENTIALS"

  def localCondition: Boolean = true

  def localCredential: Option[Credentials] =
    if (localCondition) Option(Credentials(Path.userHome / ".sbt" / ".credentials")) else None

  def setCredentials(credentials: TaskKey[Seq[Credentials]]): Setting[Task[Seq[Credentials]]] =
    credentials ++= localCredential.toSeq

  /**
   * Publish to Nexus repository
   * @param publishTo
   *   publishTo task
   * @return
   *   publishTo task
   */
  def nexus(publishTo: TaskKey[Option[Resolver]]): Setting[Task[Option[Resolver]]] =
    if (localCondition) {
      publishTo := {
        if (isSnapshot.value)
          Repository.maven(Repository.from(Path.userHome / ".sbt" / ".nexus-snapshots"))
        else Repository.maven(Repository.from(Path.userHome / ".sbt" / ".nexus-releases"))
      }
    } else publishTo := Repository.maven(Repository.dummy)

}
