import de.heikoseeberger.sbtheader.HeaderPlugin
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.headerLicense
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object BuildPlugin extends AutoPlugin {

  override def requires: Plugins = JvmPlugin && HeaderPlugin

  override def trigger: PluginTrigger = allRequirements

  lazy val localJvmSettings: Seq[String] =
    Seq(
      "-Xms8G",
      "-Xmx8G",
      "-XX:MaxPermSize=4048M",
      "-XX:+CMSClassUnloadingEnabled",
      "-Duser.timezone=GMT",
      "-XX:+PrintCommandLineFlags",
      "-XX:+CMSClassUnloadingEnabled"
    )

  override def projectSettings: Seq[Setting[_]] = Seq(
    organizationName   := "eff3ct",
    organization       := "com.eff3ct",
    scalaVersion       := Version.Scala,
    crossScalaVersions := Vector(scalaVersion.value),
    javacOptions := Seq(
      "-g:none",
      "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED"
    ),
    run / javaOptions ++= localJvmSettings,
    run / fork               := true,
    Test / fork              := true,
    Test / parallelExecution := false,
    headerLicense            := Some(Header.headerIOLicense),
    Compile / console / scalacOptions ~= (_.filterNot(
      Set("-Xfatal-warnings", "-Ywarn-unused:imports")
    )),
    updateOptions           := updateOptions.value.withCachedResolution(cachedResolution = false),
    Compile / doc / sources := Seq.empty,
    Compile / run := Defaults
      .runTask(Compile / fullClasspath, Compile / run / mainClass, Compile / run / runner)
      .evaluated,
    Test / testOptions += Tests.Argument("-oDF"),
    Test / testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")
  ) ++ SonatypePublish.projectSettings

}
