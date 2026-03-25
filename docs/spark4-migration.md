# Spark 4.x Migration Guide

This document outlines the migration path for Teckel when upgrading to Apache Spark 4.x.

## Overview

Spark 4.x introduces breaking changes by removing long-deprecated APIs. Teckel's abstraction layer isolates users from most direct Spark API changes, but the framework internals need review.

## API Changes

### `DataFrame.unionAll` -> `DataFrame.union`

Spark 4.x removes `unionAll`. Teckel already uses `union`, so no changes are required.

### `DataFrame.toDF` overloads

Some `toDF` overloads are deprecated in Spark 3.x and removed in 4.x. Review usages in the semantic module to ensure compatibility.

### `SparkSession.builder` changes

Spark 4.x introduces `SparkSession.builder` changes around Spark Connect. Teckel's `SparkETL` session creation should be verified for forward compatibility.

### Catalog API changes

Spark 4.x unifies the catalog API. Any `spark.catalog.*` usages should be reviewed.

## Scala Compatibility

- Spark 4.x supports Scala 2.13 and Scala 3.
- Teckel currently targets Scala 2.13.12, which remains compatible.
- A future migration to Scala 3 can be planned independently of the Spark upgrade.

## Teckel Abstraction Benefits

Teckel users define ETL pipelines via YAML configuration, not direct Spark API calls. This means:

- **Input/Output changes**: Format and option strings remain the same across Spark versions.
- **Transformation changes**: SQL expressions and DataFrame operations are abstracted behind the semantic layer.
- **User pipelines**: Existing YAML configurations require no changes when upgrading Spark versions.

The primary work is in the `semantic` module, which translates Teckel's model into Spark DataFrame operations.

## Migration Steps

1. Update the Spark dependency version in `project/Dependency.scala`.
2. Run the full test suite: `sbt clean coverage test coverageReport coverageAggregate`.
3. Review and fix any compilation errors in the `semantic` module.
4. Verify all example YAML pipelines produce correct results.
5. Update CI matrix to include Spark 4.x runtime.
