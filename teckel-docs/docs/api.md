# API Reference

When the CLI isn't enough — because you need to integrate Teckel into an existing application, control the SparkSession lifecycle, or compose pipelines with other logic — you use the `teckel-api` module directly from Scala.

The API is built on Cats Effect and offers three entry points with different levels of abstraction. Choose the one that fits your code style best.

## Entry Points

### `etl[F, O]` — Fully polymorphic

The most generic entry point. Parameterized over the effect type `F[_]` and the result type `O`. Useful if you already have an effect stack defined and want to integrate Teckel without fixing `IO`.

```scala
import com.eff3ct.teckel.api._
import cats.effect.IO

val yaml: String =
  """
  |input:
  |  - name: source
  |    format: csv
  |    path: 'data/input.csv'
  |    options:
  |      header: true
  |output:
  |  - name: source
  |    format: parquet
  |    mode: overwrite
  |    path: 'data/output'
  """.stripMargin

// F = IO, O = Unit → executes the pipeline and writes the output
// Requires implicit SparkSession and EvalContext[Unit] in scope
val program: IO[Unit] = etl[IO, Unit](yaml)
```

### `etlIO[O]` — Fixes F = IO

The most common choice when working with Cats Effect. Fixes `F = IO` so you don't have to specify it:

```scala
import com.eff3ct.teckel.api._
import cats.effect.IO

val yaml: String = "..."    // YAML content

val program: IO[Unit] = etlIO[Unit](yaml)
```

### `unsafeETL[O]` — Synchronous and blocking

Runs the pipeline synchronously. No effects, no `IO`. Convenient for scripts, simple batch jobs, or tests where you don't want to manage a Cats Effect runtime:

```scala
import com.eff3ct.teckel.api._

val yaml: String = "..."    // YAML content

unsafeETL[Unit](yaml)    // blocks until completion
```

## Evaluation Modes

The type `O` you pass to any entry point determines what Teckel does with the pipeline. Two evaluation contexts are available.

### Execute (`O = Unit`)

The default mode. Executes the full DAG and writes outputs to their destinations. This is the behavior you'd expect from a normal ETL job.

```scala
import com.eff3ct.teckel.api._

unsafeETL[Unit](yaml)    // executes and writes
```

### Debug (`O = Context[DataFrame]`)

Instead of writing outputs, returns all intermediate DataFrames as a `Map[String, DataFrame]`. Extremely useful for testing and debugging — you can inspect the state of the pipeline at every step without touching storage.

```scala
import com.eff3ct.teckel.api._
import com.eff3ct.teckel.model.Context
import org.apache.spark.sql.DataFrame

val assets: Context[DataFrame] = unsafeETL[Context[DataFrame]](yaml)

// All pipeline assets are available by name
assets.foreach { case (name, df) =>
  println(s"\n=== $name ===")
  df.show(10)
  df.printSchema()
}

// Or access one specific asset
val activeUsers: DataFrame = assets("active_users")
```

This mode is particularly valuable in integration tests: you can verify the result of intermediate transformations without needing a real filesystem.

## Inspect Without Spark

These utilities parse and analyze the pipeline without a `SparkSession` — they're purely functional and very fast.

### Dry Run

Generates the execution plan as text: what steps will run, in what order, and with what configuration. It also validates asset references.

```scala mdoc:compile-only
import com.eff3ct.teckel.api.DryRun

val yaml: String = "..."    // YAML content

DryRun.explain(yaml) match {
  case Right(plan)          => println(plan)
  case Left(err: Throwable) => println(s"Error: ${err.getMessage}")
}
```

### Doc Generation

Converts the pipeline into Markdown documentation. It describes all sources, transformations, and sinks with their parameters. Useful for keeping documentation in sync with the actual pipeline.

```scala mdoc:compile-only
import com.eff3ct.teckel.api.DocGen

val yaml: String = "..."    // YAML content

DocGen.generate(yaml).foreach(s => println(s))
```

### DAG Visualization

Generates a graphical representation of the DAG. Supports three formats: `mermaid` (most common for GitHub and Notion), `dot` (for Graphviz), and `ascii` (for terminals and logs).

```scala mdoc:compile-only
import com.eff3ct.teckel.api.GraphViz

val yaml: String = "..."    // YAML content

// Mermaid — paste directly into GitHub or Notion
GraphViz.generate(yaml, "mermaid").foreach(s => println(s))

// DOT — render with graphviz
GraphViz.generate(yaml, "dot").foreach(s => println(s))

// ASCII — for terminals and logs
GraphViz.generate(yaml, "ascii").foreach(s => println(s))
```
