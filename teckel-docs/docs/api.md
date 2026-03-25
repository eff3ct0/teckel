# API Reference

## Entry Points

Three entry points are available, differing in effect abstraction and execution model.

### `etl[F, O]`

Generic, parameterized over effect type `F[_]` and result type `O`.

```scala
import com.eff3ct.teckel.api._
import cats.effect.IO

// F = IO, O = Unit → execute mode (writes output files)
// Requires implicit SparkSession and EvalContext[Unit] in scope
val program: IO[Unit] = etl[IO, Unit](yaml)
```

### `etlIO[O]`

Fixes `F = IO`.

```scala
import com.eff3ct.teckel.api._

val program: IO[Unit] = etlIO[Unit](yaml)
```

### `unsafeETL[O]`

Synchronous, blocking. Convenient for scripts and tests.

```scala
import com.eff3ct.teckel.api._

unsafeETL[Unit](yaml)    // blocks until completion
```

## Evaluation Modes

The type parameter `O` determines what Teckel does with the pipeline.

### Execute (`O = Unit`)

Runs the full DAG and writes outputs.

```scala
unsafeETL[Unit](yaml)
```

### Debug (`O = Context[DataFrame]`)

Returns all intermediate DataFrames without writing to storage. Useful for testing.

```scala
import com.eff3ct.teckel.api._
import com.eff3ct.teckel.model.Context
import org.apache.spark.sql.DataFrame

val assets: Context[DataFrame] = unsafeETL[Context[DataFrame]](yaml)

assets("active_users").show()
```

## Inspect Without Spark

These utilities parse and analyze the pipeline without a `SparkSession`.

### Dry Run

```scala mdoc:compile-only
import com.eff3ct.teckel.api.DryRun

val yaml: String = "..."    // YAML content

DryRun.explain(yaml) match {
  case Right(plan)                  => println(plan)
  case Left(err: Throwable)         => println(s"Error: ${err.getMessage}")
}
```

### Doc Generation

```scala mdoc:compile-only
import com.eff3ct.teckel.api.DocGen

val yaml: String = "..."    // YAML content

DocGen.generate(yaml).foreach(s => println(s))
```

### DAG Visualization

```scala mdoc:compile-only
import com.eff3ct.teckel.api.GraphViz

val yaml: String = "..."    // YAML content

GraphViz.generate(yaml, "mermaid").foreach(s => println(s))    // mermaid | dot | ascii
```
