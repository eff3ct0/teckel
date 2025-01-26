import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.{HeaderLicense, headerLicense}
import de.heikoseeberger.sbtheader.{HeaderPlugin, License}
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
    headerLicense            := Some(headerIOLicense),
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

  /**
   * SBT Header Plugin
   */

  lazy val headerText: String =
    """|MIT License
       |
       |Copyright (c) 2024 Rafael Fernandez
       |
       |Permission is hereby granted, free of charge, to any person obtaining a copy
       |of this software and associated documentation files (the "Software"), to deal
       |in the Software without restriction, including without limitation the rights
       |to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
       |copies of the Software, and to permit persons to whom the Software is
       |furnished to do so, subject to the following conditions:
       |
       |The above copyright notice and this permission notice shall be included in all
       |copies or substantial portions of the Software.
       |
       |THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
       |IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
       |FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
       |AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
       |LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
       |OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
       |SOFTWARE.
       |""".stripMargin

  lazy val headerIOLicense: License.Custom =
    HeaderLicense.Custom(headerText)
}
