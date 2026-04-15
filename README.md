# Teckel

[![Release](https://github.com/eff3ct0/teckel/actions/workflows/release.yml/badge.svg?branch=master)](https://github.com/eff3ct0/teckel/actions/workflows/release.yml)
[![codecov](https://codecov.io/gh/eff3ct0/teckel/graph/badge.svg?token=24E1IZ0K2H)](https://codecov.io/gh/eff3ct0/teckel)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Teckel is a **Scala / Apache Spark reference implementation** of the
[Teckel Specification](https://github.com/eff3ct0/teckel-spec) — a declarative YAML language for
defining data transformation pipelines. You describe *what* you want done; Teckel parses the YAML,
builds the DAG, validates references, and executes it on Spark.

![Logo](./docs/images/teckel-banner.png)

Background reading: [Big Data with Zero Code](https://blog.rafaelfernandez.dev/posts/big-data-with-zero-code/).

## Sibling projects

| Project | Role |
|---------|------|
| [eff3ct0/teckel-spec](https://github.com/eff3ct0/teckel-spec) | Language-agnostic specification (current: **v3.0**). Defines syntax, semantics, expression grammar, validation rules, conformance levels, error catalog. |
| **eff3ct0/teckel** *(this repo)* | Scala 2.13 + Apache Spark 3.5.x reference implementation. |
| [eff3ct0/teckel-rs](https://github.com/eff3ct0/teckel-rs) | Rust implementation (parser + model layer for v3.0). |

## Features

- **Declarative YAML pipelines** — sources, transformations, sinks defined as data, not code.
- **45+ built-in transformations** — relational (`select`, `where`, `group`, `order`, `join`,
  `union`/`intersect`/`except`, `distinct`, `limit`), column ops (`addColumns`, `dropColumns`,
  `renameColumns`, `castColumns`), analytical (`window`, `pivot`, `unpivot`, `flatten`, `sample`,
  `rollup`, `cube`, `groupingSets`), warehousing (`scd2`, `merge`, `enrich`, `schemaEnforce`),
  control flow (`conditional`, `split`, `sql`), v3.0 additions (`offset`, `tail`, `fillNa`,
  `dropNa`, `replace`, `parse`, `asOfJoin`, `lateralJoin`, `transpose`, `describe`, `crosstab`,
  `hint`), and pluggable `custom` components.
- **Variable substitution** — `${VAR}` and `${VAR:default}` resolved from env vars.
- **Secrets** — `{{secrets.<alias>}}` placeholders backed by env vars or pluggable providers.
- **Hooks** — `preExecution` / `postExecution` shell commands around the pipeline run.
- **Pipeline config** — backend selection, cache policy, notifications, custom component declarations.
- **Streaming I/O** — `streamingInput` / `streamingOutput` for Structured Streaming pipelines.
- **Dry-run & validation** — preview the execution plan and catch broken references before Spark starts.
- **Doc generation** — render any pipeline as Markdown.
- **DAG visualization** — Mermaid, DOT, or ASCII output.
- **Embedded REST server** — expose pipelines as HTTP endpoints with no extra dependencies.

> Naming note: this implementation uses `group` / `order` as the YAML keys (instead of the
> spec-canonical `groupBy` / `orderBy`). All other top-level keys and operation names match the spec.

## Quick example

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
  - name: active
    where:
      from: employees
      filter: "status = 'active'"

  - name: by_dept
    group:
      from: active
      by: [department]
      agg:
        - "count(*) as headcount"
        - "avg(salary) as avg_salary"

output:
  - name: by_dept
    format: parquet
    mode: overwrite
    path: 'data/output/by_dept'
```

More examples under [`docs/etl/`](./docs/etl/) — `simple.yaml`, `complex.yaml`, `join.yaml`,
`window.yaml`, `merge.yaml`, `quality.yaml`, `secrets.yaml`, `streaming.yaml`, and many more.

## Getting Started

### Prerequisites

- **JDK 8 or 11** (Spark 3.5.x is not fully supported on Java 17 in all configurations).
- **Apache Spark 3.5.x** at runtime (declared `provided` in the library artifacts).
- **YAML pipeline files**.

#### Spark on Docker / Kubernetes

If you don't already run Spark, the
[`eff3ct/spark`](https://hub.docker.com/r/eff3ct/spark) image
([eff3ct0/spark-docker](https://github.com/eff3ct0/spark-docker)) is a ready-to-use cluster.

### Installation

Clone and build:

```bash
git clone https://github.com/eff3ct0/teckel.git
cd teckel
sbt cli/assembly
```

The CLI uber JAR is `cli/target/scala-2.13/teckel-etl_2.13.jar` (Spark bundled in).

### CLI

```
-f, --file     <path>   run the pipeline at <path>
-c, --console           read YAML from stdin
    --dry-run           print the execution plan, no Spark
    --doc               emit Markdown documentation for the pipeline
    --graph             emit the DAG (Mermaid by default)
    --server [--port N] start the embedded REST server
```

#### Run from a file

<details><summary>Demo - Teckel and Apache Spark by Yaml File</summary>

[![Teckel and Apache Spark by Yaml File](https://res.cloudinary.com/marcomontalbano/image/upload/v1735905159/video_to_markdown/images/youtube--eJwJIbNAtto-c05b58ac6eb4c4700831b2b3070cd403.jpg)](https://www.youtube.com/watch?v=eJwJIbNAtto "Teckel and Apache Spark by Yaml File")

</details>

```bash
/opt/spark/bin/spark-submit --class com.eff3ct.teckel.app.Main teckel-etl_2.13.jar -f /path/to/pipeline.yaml
```

#### Run from stdin

<details><summary>Demo - Teckel and Apache Spark by STDIN</summary>

[![Teckel and Apache Spark by STDIN](https://res.cloudinary.com/marcomontalbano/image/upload/v1735905159/video_to_markdown/images/youtube--eJwJIbNAtto-c05b58ac6eb4c4700831b2b3070cd403.jpg)](https://www.youtube.com/watch?v=V9PzMdZ6u2U "Teckel and Apache Spark by STDIN")

</details>

```bash
cat << 'EOF' | /opt/spark/bin/spark-submit --class com.eff3ct.teckel.app.Main teckel-etl_2.13.jar -c
version: "3.0"
input:
  - name: t
    format: csv
    path: '/path/to/data/file.csv'
    options:
      header: true
      sep: '|'
output:
  - name: t
    format: parquet
    mode: overwrite
    path: '/path/to/output/'
EOF
```

> [!IMPORTANT]
>
> **Teckel CLI as dependency / Teckel ETL as framework.**
>
> The Teckel CLI is also usable as a library dependency. The uber JAR is named `teckel-etl`
> (not `teckel-cli`) precisely to distinguish the CLI from the framework when both end up on the
> same classpath.
>
> See [Integration with Apache Spark](./docs/integration-apache-spark.md) for embedding details.

## Integration with Apache Spark

Teckel integrates with any existing Spark application — either as a CLI invoked via
`spark-submit`, or as a library (`teckel-api`) embedded in your Scala code. Three entry points
are exposed: `etl[F, O]` (polymorphic), `etlIO[O]` (fixes `F = IO`), and `unsafeETL[O]`
(synchronous). See [Integration with Apache Spark](./docs/integration-apache-spark.md).

## Documentation

The Docusaurus site under [`teckel-docs/`](./teckel-docs/) contains the full guide:
Getting Started, Transformations, API, CLI, Plugins, and Examples. The canonical language
reference lives in
[teckel-spec v3.0](https://github.com/eff3ct0/teckel-spec/blob/master/spec/v3.0/teckel-spec.md).

## Development and Contribution

Contributions welcome — fork the repository and open a pull request. For changes affecting the
YAML surface, please cross-check against the
[Teckel Specification](https://github.com/eff3ct0/teckel-spec) before submitting.

## License

Teckel is available under the MIT License. See the [LICENSE](./LICENSE) file for details.

For any issues or questions, open an issue on GitHub or contact Rafael Fernandez.
