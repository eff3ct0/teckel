import sbt._
import sbt.io.IO

import scala.jdk.CollectionConverters._

/**
 * Repository configuration
 * @param protocol http or https
 * @param host localhost or 127.0.0.1 or ip
 * @param port 8081 or 8082 or other
 * @param scope snapshot or release or other
 * @param repository maven-snapshots or maven-releases or other
 */
case class Repository(
    protocol: String = "http",
    host: String = "localhost",
    port: Int = 8081,
    scope: String = "snapshot",
    repository: String = "maven-snapshots"
)
object Repository {

  /**
   * Endpoint for the repository
   * @param repository repository
   * @return endpoint
   */
  def endpoint(repository: Repository): String =
    s"${repository.protocol}://${repository.host}:${repository.port}/"

  /**
   * Maven repository
   * @param repository repository
   * @return maven repository
   */
  def maven(repository: Repository): Option[MavenRepository] =
    Some(repository.scope at endpoint(repository) + s"repository/${repository.repository}")

  /**
   * Load credentials from file
   * @param file file
   * @return repository
   */
  def from(file: File): Repository = {
    loadCredentials(file) match {
      case Right(repository) => repository
      case Left(error)       => throw new Exception(error)
    }
  }

  /**
   * Load credentials from file
   * @param path path
   * @return repository
   */
  private def loadCredentials(path: File): Either[String, Repository] =
    if (path.exists) {
      val properties: Map[String, String] = read(path)
      def get(keys: List[String]): Option[String] =
        keys
          .flatMap(properties.get)
          .headOption

      val repository: Option[Repository] = for {
        https      <- get(List("prefix", "protocol")).orElse(Some("http"))
        host       <- get(List("host", "hostname")).orElse(Some("localhost"))
        port       <- get(List("port")).map(_.toInt).orElse(Some(8081))
        scope      <- get(List("scope")).orElse(Some("snapshot"))
        repository <- get(List("repository")).orElse(Some("maven-snapshots"))
      } yield Repository(host = host, port = port, scope = scope, repository = repository)

      repository.map(Right(_)).getOrElse(Left("Invalid credentials file"))
    } else
      Left("Repository file " + path + " does not exist")

  /**
   * Read properties from file
   * @param from file
   * @return properties
   */
  private def read(from: File): Map[String, String] = {
    val properties = new java.util.Properties
    IO.load(properties, from)
    properties.asScala.map { case (k, v) => (k, v.trim) }.toMap
  }
}
