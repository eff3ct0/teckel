# Teckel

Declarative, YAML-driven ETL pipelines for Apache Spark.

Define your data pipeline once — inputs, transformations, outputs — and let Teckel build the Spark DAG, validate references, and execute it.

**[Getting Started](getting-started.md)** · **[Transformations](transformations.md)** · **[API](api.md)** · **[CLI](cli.md)**

## Quick Start

```scala
// build.sbt
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

Run it:

```bash
java -jar teckel-etl_2.13.jar -f pipeline.yaml
```

Or from Scala:

```scala
import com.eff3ct.teckel.api._

unsafeETL[Unit](yaml)
```

## Modules

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

Built with Scala @SCALA_VERSION@, Apache Spark @SPARK_VERSION@, and the Cats/Cats-Effect ecosystem.

## License

[MIT](https://opensource.org/licenses/MIT) — Copyright © 2024-2026 Rafael Fernandez
