# Examples

## CSV to Parquet

```yaml
input:
  - name: raw
    format: csv
    path: 'data/input.csv'
    options:
      header: true
      sep: '|'

output:
  - name: raw
    format: parquet
    mode: overwrite
    path: 'data/output'
```

## Multi-Source Join

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
    path: 'data/output/orders'
```

## Window Functions

```yaml
input:
  - name: employees
    format: parquet
    path: 'data/employees'

transformation:
  - name: ranked
    window:
      from: employees
      partitionBy: [department]
      orderBy: [salary]
      functions:
        - expression: "row_number()"
          alias: rank
        - expression: "lag(salary, 1, 0)"
          alias: prev_salary

  - name: top_3
    where:
      from: ranked
      filter: "rank <= 3"

output:
  - name: top_3
    format: parquet
    mode: overwrite
    path: 'data/output/top_employees'
```

## Pivot and Unpivot

```yaml
# Pivot: rows → columns
- name: sales_wide
  pivot:
    from: sales
    groupBy: [region, product]
    pivotColumn: quarter
    values: [Q1, Q2, Q3, Q4]
    agg:
      - "sum(revenue) as revenue"

# Unpivot: columns → rows
- name: sales_long
  unpivot:
    from: wide_sales
    ids: [region, product]
    values: [jan, feb, mar, apr]
    variableColumn: month
    valueColumn: revenue
```

## Data Quality Pipeline

```yaml
input:
  - name: raw
    format: csv
    path: 'data/input.csv'
    options:
      header: true
      inferSchema: true

transformation:
  - name: enforced
    schemaEnforce:
      from: raw
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
      from: enforced
      onFailure: fail
      checks:
        - column: email
          rule: not_null
          description: "Email must not be null"
        - rule: "amount >= 0"
          description: "Amount must be non-negative"

output:
  - name: validated
    format: parquet
    mode: overwrite
    path: 'data/output/validated'
```

## Conditional Routing

```yaml
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

```scala
import com.eff3ct.teckel.api._
import cats.effect.{IO, IOApp}

object MyPipeline extends IOApp.Simple {
  val yaml: String = /* YAML content */
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

  def run: IO[Unit] = etlIO[Unit](yaml)
}
```
