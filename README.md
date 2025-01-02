# Teckel

[![Release](https://github.com/rafafrdz/teckel/actions/workflows/release.yml/badge.svg?branch=master)](https://github.com/rafafrdz/teckel/actions/workflows/release.yml) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Teckel is a framework designed to simplify the creation of Apache Spark ETL (Extract, Transform,
Load) processes using YAML configuration files. This tool aims to standardize and streamline ETL workflow creation by
enabling the definition of data transformations in a declarative, user-friendly format without writing extensive code.

![Logo](./docs/images/teckel-banner.png)

This concept is further developed on my
blog: [Big Data with Zero Code](https://blog.rafaelfernandez.dev/posts/big-data-with-zero-code/)

## Features

- **Declarative ETL Configuration:** Define your ETL processes with simple YAML files.
- **Support for Multiple Data Sources:** Easily integrate inputs in CSV, JSON, and Parquet formats.
- **Flexible Transformations:** Perform joins, aggregations, and selections with clear syntax.
- **Spark Compatibility:** Leverage the power of Apache Spark for large-scale data processing.

## Getting Started

### Prerequisites

- **Apache Spark**: Ensure you have Apache Spark installed and properly configured.
- **YAML files**: Create configuration files specifying your data sources and transformations.

#### Deployment on Docker or Kubernetes

In case of you don't have Apache Spark installed previously, you can deploy an Apache Spark cluster using the following
docker image [
`eff3ct/spark:latest`](https://hub.docker.com/r/eff3ct/spark) available in
the [eff3ct0/spark-docker](https://github.com/eff3ct0/spark-docker) GitHub repository.

### Installation

Clone the Teckel repository and integrate it with your existing Spark setup:

```bash
git clone https://github.com/rafafrdz/teckel.git
cd teckel
```

#### Building the Teckel ETL Uber JAR

Build the Teckel ETL CLI into an Uber JAR using the following command:

```bash
sbt cli/assembly
```

The resulting JAR, `teckel-etl_2.13.jar`, will be located in the `cli/target/scala-2.13/` directory.

> [!IMPORTANT]
>
> **Teckel CLI as dependency / Teckel ETL as framework.**
>
> The Teckel CLI is a standalone application that can be used as a dependency in your project. Notice that the uber jar
> name is `teckel-etl` and not `teckel-cli` or `teckel-cli-assembly`. This is because
> we want to distinguish between the Teckel CLI dependency and the ETL framework.

### Usage in Apache Spark

Once the `teckel-etl_2.13.jar`is ready, use it to execute ETL processes on Apache Spark with the following arguments:

- `-f` or `--file`: The path to the ETL file.
- `-c` or `--console`: Run the ETL in the console.

#### Example: Running ETL in Apache Spark using STDIN

To run the ETL in the **console**, you can use the following command:

```bash
cat << EOF | /opt/spark/bin/spark-submit --class com.eff3ct.teckel.app.Main teckel-etl_2.13.jar -c
input:
  - name: table1
    format: csv
    path: '/path/to/data/file.csv'
    options:
      header: true
      sep: '|'


output:
  - name: table1
    format: parquet
    mode: overwrite
    path: '/path/to/output/'
EOF
```

#### Example: Running ETL in Apache Spark using a file

To run the ETL from a **file**, you can use the following command:

```bash
/opt/spark/bin/spark-submit --class com.eff3ct.teckel.app.Main teckel-etl_2.13.jar -f /path/to/etl/file.yaml
```

## Integration with Apache Spark

### As Dependency

Teckel can be integrated with Apache Spark easily just adding either the Teckel CLI or Teckel Api as a
dependency in your project.

#### SBT

In your `build.sbt` file, add the following dependency:

```scala
libraryDependencies += "com.eff3ct" %% "teckel-cli" % "<version>"
// or
libraryDependencies += "com.eff3ct" %% "teckel-api" % "<version>"
```

#### Example: Running ETL in a Standalone Application

```scala
import cats.effect.{ExitCode, IO, IOApp}
import com.eff3ct.teckel.api._
import com.eff3ct.teckel.semantic.execution._
import org.apache.spark.sql.SparkSession

object Example extends IOApp {

  /**
   * Name of the ETL
   */

  implicit val spark: SparkSession = ???

  val data: String =
    """input:
      |  - name: table1
      |    format: csv
      |    path: 'data/csv/example.csv'
      |    options:
      |      header: true
      |      sep: '|'
      |
      |
      |output:
      |  - name: table1
      |    format: parquet
      |    mode: overwrite
      |    path: 'data/parquet/example'"""".stripMargin


  override def run(args: List[String]): IO[ExitCode] =
    etl[IO, Unit](data).as(ExitCode.Success)
}
```

You can use either the `etl`,  `etlIO` or `unsafeETL` methods to run the ETL from the api package.

```scala
def etl[F[_] : Run, O: EvalContext](data: String): F[O]
def etl[F[_] : Run, O: EvalContext](data: ETL): F[O]

def etlIO[O: EvalContext](data: String): IO[O]
def etlIO[O: EvalContext](data: ETL): IO[O]

def unsafeETL[O: EvalContext](data: String): O
def unsafeETL[O: EvalContext](data: ETL): O
```

### The set of Evaluation Contexts

The Teckel API offers the `EvalContext[T]`, a versatile construct designed to evaluate ETL contexts and provide results
of type `T`. This enables flexible evaluation strategies for ETL processes, with two primary derivations:

- `EvalContext[Unit]`: This context executes the ETL process, performing all specified operations, and ultimately
  produces the spected output files. It is ideal for scenarios where the primary objective is the completion of data
  transformations and load operations.
- `EvalContext[Context[DataFrame]]`: This context evaluates the ETL instructions with a focus on debugging and analysis.
  Instead of executing transformations outright, it returns a `Context[DataFrame]`, which maps ETL component names to
  their corresponding DataFrames. This allows developers to inspect intermediate DataFrames, facilitating a deeper
  understanding of the data flow and transformation logic within the ETL process.

### As Framework

Teckel can also be used as a framework in your Apache Spark project by including the Teckel ETL Uber JAR in your Apache
Spark ecosystem.

Build the Teckel ETL CLI into an Uber JAR using the following command:

```bash
sbt cli/assembly
```

#### Local Spark Environment Setup

Copy the Teckel ETL Uber JAR to the `/opt/spark/jars/` directory in your Apache Spark ecosystem:

```bash
cp cli/target/scala-2.13/teckel-etl_2.13.jar /opt/spark/jars/
```

#### Docker Usage

Mount the Teckel ETL Uber JAR in your Docker container:

```bash
docker run -v ./cli/target/scala-2.13/teckel-etl_2.13.jar:/app/teckel-etl_2.13.jar -it eff3ct/spark:latest /bin/bash

```

## ETL Yaml Example

Here's an example of a fully defined ETL configuration using a YAML file:

### SQL ETL

- Simple Example: [here](./docs/etl/simple.yaml)
- Complex Example: [here](./docs/etl/complex.yaml)
- Other Example: [here](./docs/etl/example.yaml)

### SQL Transformations

- `Select` Example: [here](./docs/etl/select.yaml)
- `Where` Example: [here](./docs/etl/where.yaml)
- `Group By` Example: [here](./docs/etl/group-by.yaml)
- `Order By` Example: [here](./docs/etl/order-by.yaml)

## Development and Contribution

Contributions to Teckel are welcome. If you'd like to contribute, please fork the repository and create a pull request
with your changes.

## License

Teckel is available under the MIT License. See the [LICENSE](./LICENSE) file for more details.

If you have any questions regarding the license, feel free to contact Rafael Fernandez.

For any issues or questions, feel free to open an issue on the GitHub repository.
