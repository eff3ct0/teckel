# Integration with Apache Spark

Teckel can be integrated with Apache Spark easily just adding either the Teckel CLI or Teckel Api as a
dependency in your project or using it as a framework in your Apache Spark project.

---

## As Framework

Teckel can also be used as a framework in your Apache Spark project by including the Teckel ETL Uber JAR in your Apache
Spark ecosystem.

Build the Teckel ETL CLI into an Uber JAR using the following command:

```bash
sbt cli/assembly
```

### Local Spark Environment Setup

Copy the Teckel ETL Uber JAR to the `/path/to/teckel/` directory in your Apache Spark ecosystem:

```bash
cp cli/target/scala-2.13/teckel-etl_2.13.jar /path/to/teckel/teckel-etl_2.13.jar
```

### Docker Usage

Mount the Teckel ETL Uber JAR in your Docker container:

```bash
docker run -v ./cli/target/scala-2.13/teckel-etl_2.13.jar:/path/to/teckel/teckel-etl_2.13.jar -it eff3ct/spark:latest /bin/bash

```

### Execution using the CLI

Once the `teckel-etl_2.13.jar`is ready, use it to execute ETL processes on Apache Spark with the following arguments:

- `-f` or `--file`: The path to the ETL file.
- `-c` or `--console`: Run the ETL in the console.

#### Example: Running ETL in Apache Spark using STDIN

```bash
cat << EOF | /opt/spark/bin/spark-submit --class com.eff3ct.teckel.app.Main /path/to/teckel/teckel-etl_2.13.jar -c
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

---

## As Dependency

Teckel can be integrated with Apache Spark easily just adding either the Teckel CLI or Teckel Api as a
dependency in your project.

### SBT

In your `build.sbt` file, add the following dependency:

```scala
libraryDependencies += "com.eff3ct" %% "teckel-cli" % "<version>"
// or
libraryDependencies += "com.eff3ct" %% "teckel-api" % "<version>"
```

### Examples

In the following examples, we will see how to use the Teckel API to run ETL processes in a standalone application. Also,
you can check the [example](./example/src/main/scala/com/eff3ct/teckel/api/example) folder for more examples.

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

#### Example: Debugging ETL's DataFrames in a Standalone Application

```scala
import cats.effect.{ExitCode, IO, IOApp}
import com.eff3ct.teckel.api._
import com.eff3ct.teckel.model.Context
import com.eff3ct.teckel.semantic.evaluation._
import org.apache.spark.sql.{DataFrame, SparkSession}

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
    etl[IO, Context[DataFrame]](data)
      .map { ctx =>
        ctx.foreach { case (tableName, df) =>
          println(s"Table: $tableName")
          df.show(false)
        }
      }
      .as(ExitCode.Success)
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

---

## The set of Evaluation Contexts

The Teckel API offers the `EvalContext[T]`, a versatile construct designed to evaluate ETL contexts and provide results
of type `T`. This enables flexible evaluation strategies for ETL processes, with two primary derivations:

- `EvalContext[Unit]`: This context executes the ETL process, performing all specified operations, and ultimately
  produces the spected output files. It is ideal for scenarios where the primary objective is the completion of data
  transformations and load operations.
- `EvalContext[Context[DataFrame]]`: This context evaluates the ETL instructions with a focus on debugging and analysis.
  Instead of executing transformations outright, it returns a `Context[DataFrame]`, which maps ETL component names to
  their corresponding DataFrames. This allows developers to inspect intermediate DataFrames, facilitating a deeper
  understanding of the data flow and transformation logic within the ETL process.

