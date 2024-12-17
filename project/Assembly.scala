import sbt.Keys.{artifact, name, scalaBinaryVersion, version}
import sbt.librarymanagement.Artifact
import sbt.{addArtifact, Compile, Setting}
import sbtassembly.AssemblyKeys.{assembly, assemblyJarName, assemblyMergeStrategy}
import sbtassembly.AssemblyPlugin.autoImport.MergeStrategy
import sbtassembly.PathList

object Assembly {

  lazy val classifier: String = "with-dependencies"

  def projectSettings: Seq[Setting[_]] =
    Seq(
      assembly / assemblyMergeStrategy := {
        case "META-INF/services/org.apache.spark.sql.sources.DataSourceRegister" =>
          MergeStrategy.concat
        case PathList("META-INF", xs @ _*) => MergeStrategy.discard
        case "application.conf"            => MergeStrategy.concat
        case x                             => MergeStrategy.first
      },
      // JAR file settings
      assembly / assemblyJarName := s"${name.value}_${scalaBinaryVersion.value}_${version.value}-$classifier.jar"
    )

  def publishAssemblyJar: Seq[Setting[_]] =
    Seq(
      Compile / assembly / artifact := {
        val art: Artifact = (Compile / assembly / artifact).value
        art.withClassifier(Some(classifier))
      }
    ) ++
      addArtifact(Compile / assembly / artifact, assembly)
}
