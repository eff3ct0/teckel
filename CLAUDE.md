# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Teckel** is a Scala framework for creating Apache Spark ETL pipelines using declarative YAML configuration. Built with Scala 2.13.12, Apache Spark 3.5.3, and the Cats/Cats-Effect ecosystem.

## Build & Development Commands

```bash
# Build the CLI uber JAR
sbt cli/assembly

# Run all tests with coverage
sbt clean coverage test coverageReport coverageAggregate

# Run tests for a specific module
sbt "api/test"
sbt "serializer/test"
sbt "semantic/test"

# Format code and update license headers
sbt headerCreateAll scalafmtAll

# Clean build
sbt clean
```

The main output artifact is `cli/target/scala-2.13/teckel-etl_2.13.jar`.

## Architecture

The modules form a layered dependency graph:

```
model --> serializer --> api --> cli
  \-------> semantic -->/
```

### Modules

- **model** (`teckel-model`): Core data types. `Asset` wraps a reference + `Source`. `Source` is a sealed trait with `Input` (file source), `Output` (file sink), and `Transformation` subtypes (Select, Where, GroupBy, OrderBy, Join).

- **serializer** (`teckel-serializer`): Parses YAML into the model using Circe + FS2. `Serializer.scala` is the main entry point. The `model/` subpackage contains intermediate serializable classes separate from the domain model.

- **semantic** (`teckel-semantic`): Executes ETL operations. `Semantic[S, -I, +O]` is the core trait. Two evaluation contexts exist: `EvalContext[Unit]` for execution (writes files) and `EvalContext[Context[DataFrame]]` for debug mode (returns intermediate DataFrames).

- **api** (`teckel-api`): Public library API. Three entry points: `etl[F, O](data)` (effectful), `etlIO[O](data)` (IO-wrapped), `unsafeETL[O](data)` (synchronous). `SparkETL` in `spark/` provides the Spark-specific integration.

- **cli** (`teckel-cli`): CLI entry point at `com.eff3ct.teckel.app.Main`. Accepts `-f <file>` or `-c` (stdin) to read YAML, then runs the ETL.

- **example**: Reference implementations showing API usage.

## Testing Notes

- Tests run forked and non-parallel (`Test / parallelExecution := false`).
- Test framework: ScalaTest + Spark Testing Base (holdenkarau).
- Coverage minimum is 30% (provisional).
- Example YAML ETL configs for reference: `docs/etl/` (simple.yaml, complex.yaml, join.yaml, etc.).

## Key Dependencies

- **Cats/Cats-Effect 3**: All effectful code uses `IO` from cats-effect with `cats-effect-std`.
- **Circe + circe-yaml**: YAML is parsed via `circe-yaml` then decoded with standard Circe decoders.
- **FS2**: Used for streaming file I/O in the serializer.
- **Apache Spark**: Declared as `provided` — not bundled in library artifacts, but included in the CLI uber JAR via `sbt-assembly`.

## Specification Reference

The Teckel expression specification lives in the repo `eff3ct0/teckel-spec`. The key files are:

- `spec/v2.0/teckel-spec.md` — full language and expression specification
- `spec/v2.0/teckel-schema.json` — JSON schema for YAML validation

**Always consult the spec** when working on expression parsing, evaluation, the model layer, serializer decoders, or any change that affects the YAML DSL surface. Use the GitHub MCP tools (`mcp__github-eff3ct__get_file_contents`) to read the spec files on demand from `eff3ct0/teckel-spec`.

## CI/CD

- CI runs tests on both Java 8 and Java 11.
- Releases publish to Maven Central via `sbt ci-release` (triggered by version tags).
- Coverage reports go to Codecov.
