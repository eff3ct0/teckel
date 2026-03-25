# Intermediate Representation Layer

## Purpose

Decouple the YAML frontend (parsing/serialization) from the execution backend (Spark, DataFusion, etc.) by introducing an explicit IR that serves as the canonical representation of a pipeline.

Currently, the `Source` sealed trait in `teckel-model` acts as an implicit IR. This design document proposes making it explicit with optimization capabilities.

## IR Node Design

IR nodes map directly to the existing `Source` sealed trait hierarchy:

```
IRNode
  |-- ReadNode(format, options, path)
  |-- WriteNode(ref, format, mode, options, path)
  |-- ProjectNode(ref, columns)        // Select
  |-- FilterNode(ref, condition)        // Where
  |-- AggregateNode(ref, keys, exprs)   // GroupBy, Rollup, Cube
  |-- SortNode(ref, keys, order)        // OrderBy
  |-- JoinNode(ref, others, type, on)   // Join
  |-- SetOpNode(ref, others, op, all)   // Union, Intersect, Except
  |-- WindowNode(ref, partition, order, funcs)
  |-- TransformNode(ref, kind, params)  // AddColumns, DropColumns, Rename, Cast, etc.
  |-- SCD2Node(ref, keys, track, dates) // SCD Type 2
  |-- EnrichNode(ref, url, method, ...)  // API Enrichment
```

Each node carries:
- A unique `NodeId` (currently `AssetRef`)
- Input dependency references
- Output schema (inferred or declared)

## Optimization Passes

The IR enables query optimization passes applied before execution:

### Pass 1: Predicate Pushdown
Move `FilterNode` closer to `ReadNode` when the filter references only columns available at the source.

```
Before: Read -> Project -> Filter
After:  Read -> Filter -> Project
```

### Pass 2: Projection Pruning
Remove columns from intermediate nodes that are never referenced downstream.

```
Before: Read(a,b,c,d) -> Project(a,b) -> Filter(a > 1)
After:  Read(a,b) -> Filter(a > 1) -> Project(a,b)
```

### Pass 3: Join Reordering
Reorder joins based on estimated cardinality when statistics are available.

### Pass 4: Common Subexpression Elimination
Detect shared subtrees and materialize them once.

## Implementation Plan

1. Define `IRNode` sealed trait in a new `teckel-ir` module
2. Add `Rewrite.toIR: Context[Asset] => IRPlan` conversion
3. Add `Optimizer.optimize: IRPlan => IRPlan` pass pipeline
4. Add `Backend.execute: IRPlan => F[Unit]` interface
5. Migrate `SparkETL` to consume `IRPlan` instead of `Context[Asset]`

## References

- Apache Calcite: relational algebra + cost-based optimization
- DataFusion: Rust query engine with logical/physical plan separation
- Spark Catalyst: internal optimizer with rules-based and cost-based passes
