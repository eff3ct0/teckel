# Examples

Una colección de pipelines completos que ilustran patrones de ETL habituales. Cada ejemplo está pensado para ser funcional tal cual — solo necesitas adaptar los paths a tu entorno.

## CSV a Parquet

El pipeline más simple posible: leer un CSV y escribirlo en formato Parquet. Sin transformaciones, solo conversión de formato. Un buen punto de partida para verificar que tu entorno funciona.

```yaml
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

## Join de múltiples fuentes

Un patrón muy común en DWH: enriquecer una tabla de hechos con dimensiones. Aquí enriquecemos órdenes con datos de clientes y productos en un solo bloque `join`.

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
      columns:
        - order_id
        - customer_name
        - product_name
        - quantity
        - price

output:
  - name: order_summary
    format: parquet
    mode: overwrite
    path: 'data/output/order_summary'
```

## Transformaciones de columnas

Un pipeline de limpieza: añadir columnas calculadas, renombrar, castear tipos y eliminar las columnas temporales que ya no necesitas.

```yaml
input:
  - name: raw
    format: csv
    path: 'data/raw.csv'
    options:
      header: true

transformation:
  - name: with_computed
    addColumns:
      from: raw
      columns:
        - name: full_name
          expression: "concat(first_name, ' ', last_name)"
        - name: report_year
          expression: "year(current_date())"

  - name: renamed
    renameColumns:
      from: with_computed
      mappings:
        usr_id: user_id

  - name: casted
    castColumns:
      from: renamed
      columns:
        - name: age
          targetType: integer
        - name: salary
          targetType: double

  - name: final
    dropColumns:
      from: casted
      columns: [first_name, last_name]

output:
  - name: final
    format: parquet
    mode: overwrite
    path: 'data/output/cleaned'
```

## Window Functions — ranking de empleados

Las funciones de ventana son ideales para cálculos que necesitan contexto de grupo sin colapsar las filas. Aquí calculamos rankings dentro de cada departamento y después filtramos el top 3.

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

## Pivot y Unpivot

Dos operaciones complementarias. En el primer caso transformamos datos longitudinales (una fila por venta por trimestre) en un formato tabular (una fila por región con una columna por trimestre). En el segundo, hacemos la operación inversa.

### Pivot: de filas a columnas

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
      values: [Q1, Q2, Q3, Q4]   # especificarlos evita un scan extra
      agg:
        - "sum(revenue) as revenue"

output:
  - name: sales_wide
    format: parquet
    mode: overwrite
    path: 'data/output/sales_pivot'
```

### Unpivot: de columnas a filas

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

## Pipeline de calidad de datos

Un patrón defensivo: antes de escribir el dato validado, imponer el esquema esperado y ejecutar checks de calidad. Si los checks fallan, el pipeline se detiene con un mensaje claro.

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
          description: "El email no puede ser nulo"
        - rule: "amount >= 0"
          description: "El importe debe ser no negativo"
        - rule: "email LIKE '%@%'"
          description: "El email debe contener @"

output:
  - name: validated
    format: parquet
    mode: overwrite
    path: 'data/output/validated'
```

## Lógica condicional y routing

Dos patrones relacionados: añadir una columna categórica basada en condiciones, y después dividir el flujo en dos paths según una condición.

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

  # Split genera dos assets: high_risk y normal
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

## Uso desde Scala

Si prefieres gestionar el ciclo de vida desde código, aquí tienes el patrón más limpio con Cats Effect:

```scala
import com.eff3ct.teckel.api._
import cats.effect.{IO, IOApp}

object MyPipeline extends IOApp.Simple {

  // El YAML puede venir de un fichero, de una base de datos, de un parámetro...
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

  // Requiere SparkSession implícito y EvalContext en scope
  def run: IO[Unit] = etlIO[Unit](pipeline)
}
```
