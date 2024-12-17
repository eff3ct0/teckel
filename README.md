# hrc-spark-etl

An example application built from the [ce3.g8 template](https://github.com/typelevel/ce3.g8).

## Run application

```shell
sbt run
```

## Run tests

```shell
sbt test
```

## sbt-tpolecat

This template uses the `sbt-tpolecat` sbt plugin to set Scala compiler options to recommended defaults. If you want to
change these defaults or find out about the different modes the plugin can operate in you can find
out [here](https://github.com/typelevel/sbt-tpolecat/).

## Publishing in own repository

### Prerequisites

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