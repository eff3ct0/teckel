# Transformations

Transformations are the core of Teckel. Each one represents an operation over a Spark DataFrame, expressed as a typed YAML block. They chain together by name, forming a DAG: the output of one becomes the input of the next.

Every transformation follows the same pattern: a `name` field that identifies the resulting asset, and an operation block with its specific parameters.

```yaml
transformation:
  - name: <output_asset_name>
    <operation_type>:
      from: <input_asset_name>
      # ... operation-specific fields
```

---

## Basic Operations

These are the most common operations in any pipeline. If you come from a SQL background, they'll feel immediately familiar.

### Select

Projects specific columns from the dataset. Useful to reduce the schema before expensive operations or to remove columns you don't need downstream.

```yaml
- name: selected
  select:
    from: source
    columns: [id, name, email]
```

### Where

Filters rows based on an arbitrary SQL condition. The `filter` field accepts any expression valid in Spark SQL.

```yaml
- name: filtered
  where:
    from: source
    filter: "age > 18 AND status = 'active'"
```

### GroupBy

Groups by one or more columns and applies aggregation functions expressed as Spark SQL strings. You can use any function that `expr()` accepts in Spark.

```yaml
- name: summary
  group:
    from: source
    by: [department]
    agg:
      - "count(*) as total"
      - "avg(salary) as avg_salary"
      - "max(hire_date) as last_hire"
```

### OrderBy

Sorts the dataset by one or more columns. The direction is global: all columns in `by` are sorted in the same direction.

```yaml
- name: sorted
  order:
    from: source
    by: [created_at, name]
    order: desc    # asc (default) | desc
```

### Distinct

Removes duplicate rows. If `columns` is specified, deduplication happens only on those columns (project then deduplicate). If omitted, it applies to the full row.

```yaml
- name: unique_emails
  distinct:
    from: source
    columns: [email]    # omit for full-row distinct
```

### Limit

Takes the first N rows. Useful in development to work with small samples, or for bounded outputs.

```yaml
- name: top_10
  limit:
    from: source
    count: 10
```

---

## Join

Unlike SQL where a JOIN unites two tables, Teckel lets you chain multiple joins in a single block by specifying multiple entries in `right`. Supported types: `inner`, `left`, `right`, `full`, `cross`, `semi`, `anti`. The `on` field accepts a list of conditions combined with `AND`.

```yaml
- name: enriched_orders
  join:
    left: orders
    right:
      - name: customers
        type: inner
        on:
          - "orders.customer_id == customers.id"
      - name: products
        type: left
        on:
          - "orders.product_id == products.id"
```

---

## Column Operations

For manipulating the schema without changing rows.

### AddColumns

Adds computed columns using Spark SQL expressions. Expressions are evaluated in the context of the DataFrame, so you can reference any existing column, registered UDFs, date functions, and so on.

```yaml
- name: enriched
  addColumns:
    from: source
    columns:
      - name: full_name
        expression: "concat(first_name, ' ', last_name)"
      - name: year
        expression: "year(created_at)"
      - name: is_senior
        expression: "years_of_experience > 5"
```

### DropColumns

Removes columns by name. Convenient after joins to drop key columns or temporary columns.

```yaml
- name: cleaned
  dropColumns:
    from: source
    columns: [temp_col, debug_col, join_key]
```

### RenameColumns

Renames columns using an `old_name: new_name` map. Especially useful for normalizing schemas after joining sources with different naming conventions.

```yaml
- name: renamed
  renameColumns:
    from: source
    mappings:
      old_name: new_name
      created: created_at
      usr_id: user_id
```

### CastColumns

Changes the data type of existing columns. Types are Spark SQL types: `integer`, `double`, `string`, `boolean`, `timestamp`, `date`, etc.

```yaml
- name: casted
  castColumns:
    from: source
    columns:
      - name: age
        targetType: integer
      - name: price
        targetType: double
      - name: active
        targetType: boolean
```

---

## SQL Expression

When the expressiveness of the YAML operations isn't enough, you can write arbitrary SQL directly. Teckel registers all pipeline assets as temp views before executing the query, so you can reference any of them by name.

```yaml
- name: result
  sql:
    from: source
    query: >
      SELECT t1.*, t2.category, t3.region_name
      FROM source t1
      LEFT JOIN categories t2 ON t1.cat_id = t2.id
      JOIN regions t3 ON t1.region_id = t3.id
      WHERE t1.amount > 100
      ORDER BY t1.created_at DESC
```

---

## Set Operations

When you need to combine datasets vertically or compute differences and intersections.

### Union

Stacks datasets with the same schema. With `all: true` it uses `unionAll` (preserves duplicates); with `all: false` it deduplicates after the union.

```yaml
- name: combined
  union:
    sources: [table1, table2, table3]
    all: true
```

### Intersect

Returns only rows present in all listed datasets.

```yaml
- name: common_records
  intersect:
    sources: [table1, table2]
    all: false
```

### Except

Subtracts `right` from `left` — returns rows that are in `left` but not in `right`.

```yaml
- name: new_records
  except:
    left: current_data
    right: previous_data
    all: false
```

---

## Window Functions

Window functions are one of the most powerful tools in analytical SQL, and in YAML their structure is particularly readable. You define the partition, the ordering within each partition, and the functions to compute.

```yaml
- name: ranked
  window:
    from: employees
    partitionBy: [department]
    orderBy: [salary]            # optional
    functions:
      - expression: "row_number()"
        alias: rank
      - expression: "dense_rank()"
        alias: dense_rank
      - expression: "lag(salary, 1, 0)"
        alias: prev_salary
      - expression: "sum(salary)"
        alias: dept_total_salary
```

---

## Advanced

### Flatten

Flattens nested structs into flat columns, and optionally explodes arrays. Useful when you receive JSON data with hierarchical structure and need to work with flat columns. The separator controls how resulting column names are built: `parent_child` with `_` as separator.

```yaml
- name: flat_data
  flatten:
    from: nested_source
    separator: "_"          # default: "_"
    explodeArrays: true     # default: true
```

### Sample

Takes a random sample of the dataset. Useful for fast development or for generating reproducible test sets (with `seed`).

```yaml
- name: sample_data
  sample:
    from: large_table
    fraction: 0.1           # 10% of rows
    withReplacement: false  # default: false
    seed: 42                # optional, for reproducibility
```

### Repartition

Redistributes data into a specified number of partitions. Causes a full shuffle. If you specify columns, it repartitions by hash of those columns — useful before joins or partitioned writes.

```yaml
- name: repartitioned
  repartition:
    from: source
    numPartitions: 8
    columns: [region]     # optional
```

### Coalesce

Reduces the number of partitions without a shuffle. More efficient than `repartition` when you only need to reduce (not increase) partition count — for example, before writing a single output file.

```yaml
- name: single_file
  coalesce:
    from: source
    numPartitions: 1
```

### Pivot

Converts unique values from one column into separate columns. The classic pattern for transforming "long" data to "wide". If you don't specify `values`, Spark infers them from the data (more expensive).

```yaml
- name: sales_by_quarter
  pivot:
    from: sales
    groupBy: [region, product]
    pivotColumn: quarter
    values: [Q1, Q2, Q3, Q4]    # optional but recommended for performance
    agg:
      - "sum(revenue) as revenue"
```

### Unpivot

The inverse of pivot: converts columns into rows. Transforms "wide" data to "long". Define which columns are identifiers (`ids`) and which are the values to stack (`values`).

```yaml
- name: long_format
  unpivot:
    from: wide_table
    ids: [id, product_name]
    values: [jan, feb, mar, apr, may, jun]
    variableColumn: month
    valueColumn: amount
```

### Rollup / Cube

Extensions of `GROUP BY` for multidimensional analysis. `Rollup` generates hierarchical subtotals (left to right); `Cube` generates all possible grouping combinations.

```yaml
- name: sales_rollup
  rollup:
    from: sales
    by: [region, country, city]
    agg:
      - "sum(amount) as total"

- name: sales_cube
  cube:
    from: sales
    by: [region, product, quarter]
    agg:
      - "sum(amount) as total"
      - "count(*) as transactions"
```

---

## Business Logic

### Conditional

Adds a computed column using conditional logic (equivalent to `CASE WHEN`). Branches are evaluated in order — the first one whose condition matches wins.

```yaml
- name: categorized
  conditional:
    from: transactions
    outputColumn: risk_tier
    branches:
      - condition: "amount > 10000"
        value: "'high'"
      - condition: "amount > 1000"
        value: "'medium'"
      - condition: "amount > 100"
        value: "'low'"
    otherwise: "'minimal'"    # optional
```

### Split

Splits a dataset into two paths based on a condition. Instead of creating two separate `where` blocks, `split` generates two assets in a single declaration: `pass` receives rows that match the condition, and `fail` receives the rest. Both can be referenced in downstream outputs or transformations.

```yaml
- name: routing
  split:
    from: transactions
    condition: "amount >= 1000 AND status = 'completed'"
    pass: large_transactions    # asset with matching rows
    fail: small_transactions    # asset with the rest
```

### SCD2

Implements the Slowly Changing Dimension Type 2 pattern by adding historicity control columns. Teckel adds the start date (`current_timestamp()`), the end date (`null`), and the current record flag (`true`). Full history management against the target is left to the merge process.

```yaml
- name: employees_scd2
  scd2:
    from: employees_staging
    keyColumns: [employee_id]
    trackColumns: [department, salary, title]
    startDateColumn: valid_from
    endDateColumn: valid_to
    currentFlagColumn: is_current
```

### Enrich

Enriches each row with external data via HTTP calls. For each record, Teckel calls the endpoint with the value of `keyColumn` and stores the response in `responseColumn`. Designed for lightweight enrichments — for large volumes, consider materializing the reference data first.

```yaml
- name: enriched_users
  enrich:
    from: users
    url: "https://api.example.com/profile"
    method: GET                              # default: GET
    keyColumn: user_id
    responseColumn: profile_data
    headers:
      Authorization: "Bearer ${API_TOKEN}"
      Accept: "application/json"
```

---

## Data Quality

### SchemaEnforce

Enforces a specific schema on the dataset. Three modes are available:

| Mode | Behavior |
|------|----------|
| `strict` | Projects only defined columns, casts types, filters nulls if `nullable: false` |
| `permissive` | Casts defined columns without projecting or filtering |
| `evolve` | Like `permissive`, but adds missing columns with their `default` value |

```yaml
- name: enforced
  schemaEnforce:
    from: raw_data
    mode: strict
    columns:
      - name: id
        dataType: integer
        nullable: false
      - name: email
        dataType: string
        nullable: false
      - name: score
        dataType: double
        default: "0.0"    # used in evolve mode
```

### Assertion

Runs data quality checks and handles failures according to the configured policy. With `onFailure: fail` it throws an exception that stops the pipeline; with `onFailure: warn` it prints error messages and continues.

Rules can be column-specific (`not_null` is the only predefined rule) or arbitrary SQL expressions evaluated per row.

```yaml
- name: validated
  assertion:
    from: source
    onFailure: fail    # fail | warn
    checks:
      - column: email
        rule: not_null
        description: "Email must not be null"
      - column: age
        rule: "age > 0 AND age < 150"
        description: "Age must be a reasonable value"
      - rule: "amount IS NOT NULL AND amount >= 0"
        description: "Amount must be positive"
```

---

## Custom Components

### Custom

Delegates the transformation to a pluggable external component. Useful when you need logic that doesn't fit any standard transformation: ML models, calls to external systems, proprietary transformations, etc. The `component` field accepts a short name registered in `PluginRegistry` or a fully-qualified class name.

```yaml
- name: ml_scored
  custom:
    from: features_table
    component: "com.example.MLScoringTransformer"
    options:
      model_path: "models/scoring_v2"
      threshold: "0.7"
```

See the [Plugins](plugins.md) guide for details on how to create and register custom components.
