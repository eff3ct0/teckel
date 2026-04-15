# Getting Started

The core idea is simple: you write a YAML file that describes what data you have, what you want to do with it, and where you want to leave it. Teckel does the rest — parses the configuration, builds the execution plan, and runs it on Spark.

This guide takes you from installation to running your first real pipeline, step by step.

## Prerequisites

- **JDK 8 or 11** — Spark doesn't fully support Java 17 in all configurations
- **Apache Spark 3.5.x** — required at runtime (declared as `provided`)
- **sbt** — only if you're building from source or embedding Teckel as a library

## Installation

### As a library

Add `teckel-api` to your `build.sbt`. This module includes the parser, the execution engine, and the public API:

```scala
libraryDependencies += "com.eff3ct" %% "teckel-api" % "@VERSION@"
```

Since Spark is declared as `provided`, you need to add it separately if your environment doesn't supply it:

```scala
libraryDependencies += "org.apache.spark" %% "spark-sql" % "@SPARK_VERSION@"
```

### As a CLI tool

If you prefer using Teckel as a standalone binary, clone the repository and build the uber JAR. This packages Spark inside the JAR so you don't need a separate Spark installation:

```bash
git clone https://github.com/eff3ct/teckel.git
cd teckel
sbt cli/assembly
# Output: cli/target/scala-2.13/teckel-etl_2.13.jar
```

## Your First Pipeline

Let's build something concrete: read a CSV file of employees, filter the active ones, group them by department, and write the result as Parquet.

### 1. Define the pipeline

Create `my-pipeline.yaml`. Each transformation references its upstream asset by name, which is how Teckel builds the dependency graph: `employees` → `active_employees` → `department_summary` → `sorted_departments`.

```yaml
version: "3.0"

input:
  - name: employees
    format: csv
    path: 'data/employees.csv'
    options:
      header: true
      inferSchema: true

transformation:
  - name: active_employees
    where:
      from: employees
      filter: "status = 'active'"

  - name: department_summary
    group:
      from: active_employees
      by: [department]
      agg:
        - "count(*) as headcount"
        - "avg(salary) as avg_salary"

  - name: sorted_departments
    order:
      from: department_summary
      by: [headcount]
      order: desc

output:
  - name: sorted_departments
    format: parquet
    mode: overwrite
    path: 'data/output/department_summary'
```

### 2. Preview first (dry-run)

Before running the job, it's good practice to review the execution plan. Dry-run parses the YAML, validates all cross-references between assets, and prints each step — without starting Spark:

```bash
java -jar teckel-etl_2.13.jar -f my-pipeline.yaml --dry-run
```

If there are broken references or configuration errors, they'll appear here before you try to execute anything.

### 3. Run it

Once the plan looks correct, execute the pipeline:

```bash
java -jar teckel-etl_2.13.jar -f my-pipeline.yaml
```

Or from Scala code:

```scala
import com.eff3ct.teckel.api._

val yaml = scala.io.Source.fromFile("my-pipeline.yaml").mkString
// Requires implicit SparkSession and EvalContext in scope
unsafeETL[Unit](yaml)
```

## Pipeline Structure

A Teckel pipeline YAML follows the [Teckel Specification v3.0](https://github.com/eff3ct0/teckel-spec/blob/master/spec/v3.0/teckel-spec.md). The minimum is `input` and `output`; the rest are optional and unlock additional capabilities:

```yaml
version: "3.0"          # Spec version (recommended)

config:                 # Pipeline-wide config: backend, cache, notifications, components
  ...

secrets:                # Secret aliases referenced as {{secrets.<alias>}}
  keys:
    ...

hooks:                  # Lifecycle commands run before/after the pipeline
  preExecution: [...]
  postExecution: [...]

templates:              # Reusable configuration fragments
  - ...

input:                  # Batch data sources (required, NonEmptyList)
  - ...

streamingInput:         # Structured Streaming sources
  - ...

transformation:         # Transformation DAG (optional)
  - ...

output:                 # Batch data sinks (required, NonEmptyList)
  - ...

streamingOutput:        # Structured Streaming sinks
  - ...
```

Variable substitution `${VAR}` and `${VAR:default}` is applied to the raw YAML before parsing
(env vars resolve them); `$$` escapes a literal `$`. Secrets use `{{secrets.<alias>}}` and are
resolved from the configured provider or env vars (`TECKEL_SECRET__<ALIAS>`). See the spec for
the full set of validation rules (`V-001`..`V-008`).

### Input

Each input defines a data source. The `name` field is the reference that downstream transformations use to refer to this dataset:

```yaml
input:
  - name: my_source          # Unique asset reference in this pipeline
    format: csv              # csv, parquet, json, orc, avro, jdbc
    path: 'data/input.csv'
    options:
      header: true
      sep: '|'
```

### Output

Each output writes an asset to its destination. The `name` must match an existing transformation or input:

```yaml
output:
  - name: my_sink            # Must match a transformation or input name
    format: parquet
    mode: overwrite           # overwrite, append, ignore, error
    path: 'data/output'
    options: {}               # Optional format-specific settings
```

### Transformation

Transformations chain together by referencing upstream assets by name. The order in the YAML is for human readability only — Teckel resolves the dependency graph automatically:

```yaml
transformation:
  - name: step1
    select:
      from: my_source        # References an input or a previous transformation
      columns: [id, name]

  - name: step2
    where:
      from: step1            # References the transformation above
      filter: "id > 10"
```

## Next Steps

- [Transformations](transformations.md) — full reference for all 45+ operations
- [Plugins](plugins.md) — custom readers, transformers, and writers
- [CLI](cli.md) — all command-line options
- [API](api.md) — programmatic integration with Scala and Cats Effect
