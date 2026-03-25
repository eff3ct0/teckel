# Multi-Backend Execution

## Purpose

Abstract the execution layer so that pipelines defined in YAML can run on different engines without changing the pipeline definition.

## Backend Trait

```scala
trait Backend[F[_]] {
  def name: String
  def execute(ir: IRPlan): F[Unit]
  def debug(ir: IRPlan): F[Map[String, Any]]
  def validate(ir: IRPlan): F[List[String]]
}
```

The `execute` method runs the full pipeline. The `debug` method returns intermediate results for inspection. The `validate` method checks that all operations are supported by this backend.

## Planned Backends

### Spark Backend (Current)

The existing `SparkETL` and `Debug` implementations become the Spark backend:

```scala
class SparkBackend[F[_]: Sync](implicit spark: SparkSession) extends Backend[F] {
  def name = "spark"
  def execute(ir: IRPlan): F[Unit] = ...
  def debug(ir: IRPlan): F[Map[String, DataFrame]] = ...
  def validate(ir: IRPlan): F[List[String]] = Sync[F].pure(Nil)
}
```

### DataFusion Backend (Future)

Apache Arrow DataFusion for lightweight local execution without JVM overhead:

```scala
class DataFusionBackend[F[_]: Sync] extends Backend[F] {
  def name = "datafusion"
  // Uses DataFusion via JNI or subprocess
}
```

Use cases: local development, CI testing, small datasets.

### DataFusion-Comet Backend (Future)

Apache DataFusion Comet as a Spark plugin for accelerated execution:

```scala
class CometBackend[F[_]: Sync](implicit spark: SparkSession) extends Backend[F] {
  def name = "comet"
  // Spark with Comet plugin enabled
}
```

## Configuration

Backend selection via YAML pipeline config:

```yaml
config:
  backend: spark  # spark | datafusion | comet

input:
  - name: table1
    format: csv
    path: 'data/csv/example.csv'
```

Or via CLI flag:

```bash
teckel-etl -f pipeline.yaml --backend datafusion
```

## Implementation Plan

1. Define `Backend[F[_]]` trait in `teckel-api`
2. Wrap existing Spark code in `SparkBackend`
3. Add backend resolution in `Run` based on config
4. Implement `DataFusionBackend` using arrow-datafusion JNI bindings
5. Add capability matrix: which transformations each backend supports
