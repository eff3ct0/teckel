# Examples

A collection of complete pipelines illustrating common ETL patterns. Each example is meant to be functional as-is — just adapt the paths to your environment.

## CSV to Parquet

The simplest possible pipeline: read a CSV and write it as Parquet. No transformations, just format conversion. A good starting point to verify that your environment works.

```yaml
version: "3.0"

input:
  - name: raw_data
    format: csv
    path: 'data/csv/input.csv'
    options:
      header: true
      sep: '|'

output:
  - name: raw_data
    format: parquet
    mode: overwrite
    path: 'data/parquet/output'
```

## Multi-Source Join

A very common pattern in DWH: enrich a fact table with dimensions. Here we enrich orders with customer and product data in a single `join` block.

```yaml
input:
  - name: orders
    format: csv
    path: 'data/orders.csv'
    options:
      header: true
      inferSchema: true

  - name: customers
    format: parquet
    path: 'data/customers'

  - name: products
    format: parquet
    path: 'data/products'

transformation:
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

  - name: order_summary
    select:
      from: enriched_orders
      columns: [order_id, customer_name, product_name, quantity, price]

output:
  - name: order_summary
    format: parquet
    mode: overwrite
    path: 'data/output/order_summary'
```

## Window Functions — Employee Ranking

Window functions are ideal for calculations that need group context without collapsing rows. Here we compute rankings within each department, then filter the top 3.

```yaml
input:
  - name: employees
    format: parquet
    path: 'data/employees'

transformation:
  - name: ranked_employees
    window:
      from: employees
      partitionBy: [department]
      orderBy: [salary]
      functions:
        - expression: "row_number()"
          alias: rank
        - expression: "dense_rank()"
          alias: dense_rank
        - expression: "lag(salary, 1, 0)"
          alias: prev_salary
        - expression: "sum(salary)"
          alias: dept_total

  - name: top_3_per_dept
    where:
      from: ranked_employees
      filter: "rank <= 3"

output:
  - name: top_3_per_dept
    format: parquet
    mode: overwrite
    path: 'data/output/top_employees'
```

## Pivot and Unpivot

Two complementary operations. In the first case we transform longitudinal data (one row per sale per quarter) into tabular format (one row per region with one column per quarter). In the second, we do the inverse.

### Pivot: rows to columns

```yaml
input:
  - name: quarterly_sales
    format: csv
    path: 'data/sales.csv'
    options:
      header: true
      inferSchema: true

transformation:
  - name: sales_wide
    pivot:
      from: quarterly_sales
      groupBy: [region, product]
      pivotColumn: quarter
      values: [Q1, Q2, Q3, Q4]    # specifying them avoids an extra scan
      agg:
        - "sum(revenue) as revenue"

output:
  - name: sales_wide
    format: parquet
    mode: overwrite
    path: 'data/output/sales_pivot'
```

### Unpivot: columns to rows

```yaml
input:
  - name: wide_sales
    format: parquet
    path: 'data/wide_sales'

transformation:
  - name: long_sales
    unpivot:
      from: wide_sales
      ids: [region, product]
      values: [jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec]
      variableColumn: month
      valueColumn: revenue

output:
  - name: long_sales
    format: parquet
    mode: overwrite
    path: 'data/output/sales_long'
```

## Data Quality Pipeline

A defensive pattern: before writing validated data, enforce the expected schema and run quality checks. If any check fails, the pipeline stops with a clear message.

```yaml
input:
  - name: raw_data
    format: csv
    path: 'data/input.csv'
    options:
      header: true
      inferSchema: true

transformation:
  - name: schema_enforced
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
        - name: amount
          dataType: double

  - name: validated
    assertion:
      from: schema_enforced
      onFailure: fail
      checks:
        - column: email
          rule: not_null
          description: "Email must not be null"
        - rule: "amount >= 0"
          description: "Amount must be non-negative"
        - rule: "email LIKE '%@%'"
          description: "Email must contain @"

output:
  - name: validated
    format: parquet
    mode: overwrite
    path: 'data/output/validated'
```

## Conditional Logic and Routing

Two related patterns: adding a categorical column based on conditions, then splitting the flow into two separate paths.

```yaml
input:
  - name: transactions
    format: parquet
    path: 'data/transactions'

transformation:
  - name: categorized
    conditional:
      from: transactions
      outputColumn: risk_level
      branches:
        - condition: "amount > 10000"
          value: "'high'"
        - condition: "amount > 1000"
          value: "'medium'"
      otherwise: "'low'"

  # split generates two assets: high_risk and normal_risk
  - name: routing
    split:
      from: categorized
      condition: "risk_level = 'high'"
      pass: high_risk
      fail: normal_risk

output:
  - name: high_risk
    format: parquet
    mode: overwrite
    path: 'data/output/high_risk'

  - name: normal_risk
    format: parquet
    mode: overwrite
    path: 'data/output/normal'
```

## Programmatic Usage

When you prefer managing the lifecycle from code, here's the cleanest pattern with Cats Effect. The YAML can come from a file, a database, a parameter — anywhere.

```scala
import com.eff3ct.teckel.api._
import cats.effect.{IO, IOApp}

object MyPipeline extends IOApp.Simple {
  val pipeline: String =
    """
    |input:
    |  - name: source
    |    format: csv
    |    path: 'data/input.csv'
    |    options:
    |      header: true
    |output:
    |  - name: source
    |    format: parquet
    |    mode: overwrite
    |    path: 'data/output'
    """.stripMargin

  // Requires implicit SparkSession and EvalContext in scope
  def run: IO[Unit] = etlIO[Unit](pipeline)
}
```
