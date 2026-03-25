---
slug: /
sidebar_position: 1
---

# Teckel

Writing ETL pipelines with Spark usually means a lot of boilerplate: read from here, transform there, write somewhere else — and in three months, even you won't remember what it does. Teckel takes the opposite approach: if the logic is declarative, it should be expressible as data.

**Teckel** is a Scala framework that turns YAML files into complete Apache Spark ETL pipelines. You define *what* you want to do, not *how* to do it. The framework builds the DAG, validates asset references, and executes everything on Spark — you keep the business logic, not the scaffolding.

**[Getting Started](getting-started.md)** · **[Transformations](transformations.md)** · **[API](api.md)** · **[CLI](cli.md)**

## What's included

- **Declarative YAML pipelines** — define sources, transformations, and sinks without a single line of Spark code
- **30+ built-in transformations** — Select, Where, GroupBy, Join, Window, Pivot, SCD2, Assertion, and many more
- **Pluggable components** — extend Teckel with custom readers, transformers, and writers
- **Dry-run mode** — preview the execution plan before launching any Spark job
- **Documentation generation** — convert any pipeline into human-readable Markdown
- **DAG visualization** — export the pipeline graph as Mermaid, DOT, or ASCII
- **Pipeline validation** — catch broken references and config errors before execution
- **Embedded REST server** — expose pipelines as HTTP endpoints with no extra dependencies

## Quick Start

Add Teckel to your `build.sbt`:

```scala
libraryDependencies += "com.eff3ct" %% "teckel-api" % "@VERSION@"
```

Write a pipeline:

```yaml
input:
  - name: users
    format: csv
    path: 'data/users.csv'
    options:
      header: true

transformation:
  - name: active_users
    where:
      from: users
      filter: "status = 'active'"

output:
  - name: active_users
    format: parquet
    mode: overwrite
    path: 'data/output/active_users'
```

Run it from the CLI:

```bash
java -jar teckel-etl_2.13.jar -f pipeline.yaml
```

Or directly from Scala:

```scala
import com.eff3ct.teckel.api._

// Requires implicit SparkSession and EvalContext in scope
unsafeETL[Unit](yaml)
```

## Modules

The project is split into layers with well-separated responsibilities:

| Module        | Artifact             | Description                          |
|---------------|----------------------|--------------------------------------|
| `model`       | `teckel-model`       | Core types: `Asset`, `Source`        |
| `serializer`  | `teckel-serializer`  | YAML parsing with Circe + circe-yaml |
| `semantic`    | `teckel-semantic`    | Spark execution engine               |
| `api`         | `teckel-api`         | Public library API                   |
| `cli`         | `teckel-cli`         | Command-line interface               |

## Architecture

```
model --> serializer --> api --> cli
  \-------> semantic -->/
```

The `model` module defines the core domain types — the lingua franca of the system. The `serializer` parses YAML, converts it to intermediate representations, and rewrites them into domain models. The `semantic` module operates on that model to execute Spark transformations. The `api` ties everything together and exposes three entry points depending on your usage style. Finally, `cli` is the outermost layer: it reads arguments, calls the `api`, and manages the execution lifecycle.

No layer knows the details of the layers above it, which makes adding alternative backends or frontends a matter of extending rather than modifying.

Built with Scala @SCALA_VERSION@, Apache Spark @SPARK_VERSION@, and the Cats/Cats-Effect ecosystem.

## License

[MIT](https://opensource.org/licenses/MIT) — Copyright © 2024-2026 Rafael Fernandez
