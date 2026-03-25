# Getting Started

## Installation

### As a library

```scala
// build.sbt
libraryDependencies += "com.eff3ct" %% "teckel-api" % "@VERSION@"

// Spark is `provided` — add it to your runtime classpath
libraryDependencies += "org.apache.spark" %% "spark-sql" % "@SPARK_VERSION@"
```

### As a CLI tool

```bash
git clone https://github.com/eff3ct/teckel.git
cd teckel
sbt cli/assembly
# Output: cli/target/scala-2.13/teckel-etl_2.13.jar
```

## Your First Pipeline

Create `my-pipeline.yaml`:

```yaml
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

Preview before running:

```bash
java -jar teckel-etl_2.13.jar -f my-pipeline.yaml --dry-run
```

Run it:

```bash
java -jar teckel-etl_2.13.jar -f my-pipeline.yaml
```

Or from Scala:

```scala
import com.eff3ct.teckel.api._

val yaml = scala.io.Source.fromFile("my-pipeline.yaml").mkString
unsafeETL[Unit](yaml)
```

## Pipeline Structure

Every Teckel pipeline has three sections:

```yaml
input:           # Data sources (required)
  - ...

transformation:  # Transformation DAG (optional)
  - ...

output:          # Data sinks (required)
  - ...
```

### Input

```yaml
input:
  - name: my_source          # Asset reference used downstream
    format: csv              # csv, parquet, json, orc, avro, jdbc
    path: 'data/input.csv'
    options:
      header: true
      sep: '|'
```

### Output

```yaml
output:
  - name: my_sink            # Must match a transformation or input name
    format: parquet
    mode: overwrite           # overwrite, append, ignore, error
    path: 'data/output'
```

### Transformation

Each transformation references an upstream asset by name. Teckel resolves the dependency graph automatically:

```yaml
transformation:
  - name: step1
    select:
      from: my_source
      columns: [id, name]

  - name: step2
    where:
      from: step1
      filter: "id > 10"
```

## Next Steps

- [Transformations](transformations.md) — full reference for all 30+ operations
- [Plugins](plugins.md) — custom readers, transformers, and writers
- [CLI](cli.md) — all command-line options
- [API](api.md) — programmatic integration
