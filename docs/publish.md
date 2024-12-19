# Publishing in own local repository

## Prerequisites

In order to publish your project, you need to:

- Add your own credentials to the `~/.sbt/.credentials` file.

```text
realm=Sonatype Nexus Repository Manager
host=<host>
user=<user>
password=<password>
```

- Add the repository configuration for every publish scope in `~/.sbt/.nexus-<scope>` file.

```text
protocol=[http|https]
host=<host>
port=<port>
scope=[snapshot|releases|other]
repository=[maven-snapshot|maven-releases|other]
```

For this case, we have the following configuration:

* Credentials for publishing to the` repository in `.sbt/.credentials`

```text
realm=Sonatype Nexus Repository Manager
host=localhost
user=admin
password=admin
```

* Configuration for the `publish` scope in `.sbt/.nexus-releases`

```text
protocol=http
host=localhost
port=9999
scope=releases
repository=maven-releases
```

* Configuration for the `publish` scope in `.sbt/.nexus-snapshots`

```text
protocol=http
host=localhost
port=9999
scope=snapshots
repository=maven-snapshots
```

### Publish

```shell
sbt clean
sbt publish
```

The first command cleans the build and the second command publishes the project to the Sonatype repository.