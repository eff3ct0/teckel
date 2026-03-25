# Transformations

Every transformation takes a `name` (the output asset reference) and a typed operation block. Transformations chain by name, forming a DAG.

```yaml
transformation:
  - name: <output_asset_name>
    <operation_type>:
      from: <input_asset_name>
      # ... operation-specific fields
```

---

## Basic

### Select

```yaml
- name: selected
  select:
    from: source
    columns: [id, name, email]
```

### Where

```yaml
- name: filtered
  where:
    from: source
    filter: "age > 18 AND status = 'active'"
```

### GroupBy

```yaml
- name: summary
  group:
    from: source
    by: [department]
    agg:
      - "count(*) as total"
      - "avg(salary) as avg_salary"
```

### OrderBy

```yaml
- name: sorted
  order:
    from: source
    by: [created_at, name]
    order: desc    # asc (default) | desc
```

### Distinct

```yaml
- name: unique
  distinct:
    from: source
    columns: [email]    # omit for full-row distinct
```

### Limit

```yaml
- name: top_10
  limit:
    from: source
    count: 10
```

---

## Join

Joins multiple sources in a single block. Supported types: `inner`, `left`, `right`, `full`, `cross`, `semi`, `anti`.

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

### AddColumns

```yaml
- name: enriched
  addColumns:
    from: source
    columns:
      - name: full_name
        expression: "concat(first_name, ' ', last_name)"
      - name: year
        expression: "year(created_at)"
```

### DropColumns

```yaml
- name: cleaned
  dropColumns:
    from: source
    columns: [temp_col, debug_col]
```

### RenameColumns

```yaml
- name: renamed
  renameColumns:
    from: source
    mappings:
      old_name: new_name
      created: created_at
```

### CastColumns

```yaml
- name: casted
  castColumns:
    from: source
    columns:
      - name: age
        targetType: integer
      - name: price
        targetType: double
```

---

## SQL Expression

Executes arbitrary SQL. All pipeline assets are registered as temp views before the query runs.

```yaml
- name: result
  sql:
    from: source
    query: >
      SELECT t1.*, t2.category
      FROM source t1
      LEFT JOIN categories t2 ON t1.cat_id = t2.id
      WHERE t1.amount > 100
```

---

## Set Operations

### Union

```yaml
- name: combined
  union:
    sources: [table1, table2, table3]
    all: true    # true → unionAll, false (default) → union
```

### Intersect

```yaml
- name: common
  intersect:
    sources: [table1, table2]
    all: false
```

### Except

```yaml
- name: diff
  except:
    left: current_data
    right: previous_data
    all: false
```

---

## Window Functions

```yaml
- name: ranked
  window:
    from: source
    partitionBy: [department]
    orderBy: [salary]           # optional
    functions:
      - expression: "row_number()"
        alias: rank
      - expression: "lag(salary, 1, 0)"
        alias: prev_salary
      - expression: "sum(salary)"
        alias: dept_total
```

---

## Advanced

### Flatten

Flattens nested structs and optionally explodes arrays.

```yaml
- name: flat
  flatten:
    from: nested_source
    separator: "_"          # default: "_"
    explodeArrays: true     # default: true
```

### Sample

```yaml
- name: sampled
  sample:
    from: source
    fraction: 0.1
    withReplacement: false  # default: false
    seed: 42                # optional
```

### Repartition

```yaml
- name: repartitioned
  repartition:
    from: source
    numPartitions: 8
    columns: [region]       # optional
```

### Coalesce

No shuffle. Use to reduce (not increase) partition count.

```yaml
- name: single_file
  coalesce:
    from: source
    numPartitions: 1
```

### Pivot

```yaml
- name: pivoted
  pivot:
    from: sales
    groupBy: [region, product]
    pivotColumn: quarter
    values: [Q1, Q2, Q3, Q4]    # optional; recommended for performance
    agg:
      - "sum(revenue) as revenue"
```

### Unpivot

```yaml
- name: long_format
  unpivot:
    from: wide_table
    ids: [id, name]
    values: [jan, feb, mar, apr]
    variableColumn: month
    valueColumn: amount
```

### Rollup / Cube

```yaml
- name: rollup_result
  rollup:
    from: sales
    by: [region, country, city]
    agg:
      - "sum(amount) as total"

- name: cube_result
  cube:
    from: sales
    by: [region, product, quarter]
    agg:
      - "sum(amount) as total"
```

---

## Business Logic

### Conditional

Adds a column using `CASE WHEN` logic. Branches are evaluated in order.

```yaml
- name: categorized
  conditional:
    from: source
    outputColumn: risk_tier
    branches:
      - condition: "amount > 10000"
        value: "'high'"
      - condition: "amount > 1000"
        value: "'medium'"
    otherwise: "'low'"
```

### Split

Splits a dataset into two named assets based on a condition.

```yaml
- name: routing
  split:
    from: source
    condition: "amount >= 1000"
    pass: large_transactions    # rows matching condition
    fail: small_transactions    # rows not matching
```

Reference `large_transactions` and `small_transactions` in downstream outputs or transformations.

### SCD2

Adds Slowly Changing Dimension Type 2 tracking columns.

```yaml
- name: employees_scd2
  scd2:
    from: staging
    keyColumns: [employee_id]
    trackColumns: [department, salary, title]
    startDateColumn: valid_from
    endDateColumn: valid_to
    currentFlagColumn: is_current
```

### Enrich

Enriches each row with an external HTTP lookup.

```yaml
- name: enriched
  enrich:
    from: source
    url: "https://api.example.com/profile"
    method: GET
    keyColumn: user_id
    responseColumn: profile_data
    headers:
      Authorization: "Bearer ${API_TOKEN}"
```

---

## Data Quality

### SchemaEnforce

| Mode | Behavior |
|------|----------|
| `strict` | Projects only defined columns, casts types, filters nulls if `nullable: false` |
| `permissive` | Casts defined columns without projecting or filtering |
| `evolve` | Like `permissive`, but adds missing columns with `default` value |

```yaml
- name: enforced
  schemaEnforce:
    from: source
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
        default: "0.0"
```

### Assertion

```yaml
- name: validated
  assertion:
    from: source
    onFailure: fail    # fail | warn
    checks:
      - column: email
        rule: not_null
        description: "Email must not be null"
      - rule: "amount >= 0"
        description: "Amount must be non-negative"
```

---

## Custom

Delegates to a pluggable external component. The `component` field accepts a short registry name or a fully-qualified class name.

```yaml
- name: ml_scored
  custom:
    from: features
    component: "com.example.MLScoringTransformer"
    options:
      model_path: "models/scoring_v2"
      threshold: "0.7"
```

See [Plugins](plugins.md) for how to create and register custom components.
