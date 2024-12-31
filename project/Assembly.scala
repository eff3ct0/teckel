import sbt.Keys.{artifact, name, scalaBinaryVersion}
import sbt.librarymanagement.Artifact
import sbt.{Compile, Setting, addArtifact}
import sbtassembly.AssemblyKeys.{assembly, assemblyJarName, assemblyMergeStrategy}
import sbtassembly.AssemblyPlugin.autoImport.MergeStrategy
import sbtassembly.PathList

object Assembly {

  def projectSettings(assemblyName: Option[String] = None): Seq[Setting[_]] =
    Seq(
      assembly / assemblyMergeStrategy := {
        case "META-INF/services/org.apache.spark.sql.sources.DataSourceRegister" =>
          MergeStrategy.concat
        case PathList("META-INF", xs @ _*) => MergeStrategy.discard
        case "application.conf"            => MergeStrategy.concat
        case x                             => MergeStrategy.first
      },
      // JAR file settings
      assembly / assemblyJarName := {
        val aName: String = assemblyName.getOrElse(name.value)
        s"${aName}_${scalaBinaryVersion.value}.jar"
      }
    )

  def publishAssemblyJar: Seq[Setting[_]] =
    Seq(
      Compile / assembly / artifact := {
        val art: Artifact = (Compile / assembly / artifact).value
        art
      }
    ) ++
      addArtifact(Compile / assembly / artifact, assembly)
}
