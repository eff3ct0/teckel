# Transformations

Las transformaciones son el núcleo de Teckel. Cada una representa una operación sobre un DataFrame de Spark, expresada como un bloque YAML tipado. Se encadenan por nombre formando un DAG: la salida de una es la entrada de la siguiente.

Teckel incluye más de 30 transformaciones agrupadas por categoría. Todas siguen el mismo patrón: un campo `name` que identifica el asset resultante, y un bloque de configuración con el tipo de operación.

```yaml
transformation:
  - name: <nombre_del_asset_resultante>
    <tipo_de_operacion>:
      from: <asset_fuente>
      # ... parámetros específicos
```

---

## Operaciones básicas

Las más habituales en cualquier pipeline. Si vienes del mundo SQL, te resultarán inmediatamente familiares.

### Select

Proyecta columnas específicas del dataset. Útil para reducir el esquema antes de operaciones costosas o para limpiar columnas innecesarias aguas abajo.

```yaml
transformation:
  - name: selected_data
    select:
      from: source_table
      columns: [id, name, email]
```

### Where

Filtra filas basándose en una condición SQL arbitraria. El campo `filter` acepta cualquier expresión válida de Spark SQL.

```yaml
transformation:
  - name: filtered_data
    where:
      from: source_table
      filter: "age > 18 AND status = 'active'"
```

### GroupBy

Agrupa por una o más columnas y aplica funciones de agregación expresadas como strings de Spark SQL. Puedes usar cualquier función que acepte `expr()` de Spark.

```yaml
transformation:
  - name: summary
    group:
      from: source_table
      by: [department]
      agg:
        - "count(*) as total"
        - "avg(salary) as avg_salary"
        - "max(hire_date) as last_hire"
```

### OrderBy

Ordena el dataset por una o más columnas. La dirección es global: todas las columnas del `by` se ordenan en el mismo sentido.

```yaml
transformation:
  - name: sorted_data
    order:
      from: source_table
      by: [created_at, name]
      order: desc  # asc (default) o desc
```

### Distinct

Elimina filas duplicadas. Si se especifica `columns`, la deduplicación se hace solo sobre esas columnas (proyecta y deduplica). Si se omite, se aplica sobre toda la fila.

```yaml
transformation:
  - name: unique_emails
    distinct:
      from: source_table
      columns: [email]  # omitir para distinct completo
```

### Limit

Toma los primeros N registros del dataset. Útil en desarrollo para trabajar con muestras pequeñas, o para outputs acotados.

```yaml
transformation:
  - name: top_10
    limit:
      from: source_table
      count: 10
```

---

## Join

Combina múltiples fuentes de datos. A diferencia de SQL donde un JOIN une dos tablas, aquí puedes encadenar varios en un solo bloque especificando múltiples entradas en `right`.

```yaml
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
```

Los tipos de join soportados son los de Spark: `inner`, `left`, `right`, `full`, `cross`, `semi`, `anti`. El campo `on` acepta una lista de condiciones que se combinan con `AND`.

---

## Operaciones sobre columnas

Para manipular el esquema sin cambiar las filas.

### AddColumns

Añade columnas calculadas mediante expresiones Spark SQL. Las expresiones se evalúan en el contexto del DataFrame, por lo que puedes referenciar cualquier columna existente, usar UDFs registradas, funciones de fecha, etc.

```yaml
transformation:
  - name: enriched
    addColumns:
      from: source_table
      columns:
        - name: full_name
          expression: "concat(first_name, ' ', last_name)"
        - name: year
          expression: "year(created_at)"
        - name: is_senior
          expression: "years_of_experience > 5"
```

### DropColumns

Elimina columnas por nombre. Conveniente después de joins para quitar columnas de clave o columnas temporales.

```yaml
transformation:
  - name: cleaned
    dropColumns:
      from: source_table
      columns: [temp_col, debug_col, join_key]
```

### RenameColumns

Renombra columnas mediante un mapa `nombre_viejo: nombre_nuevo`. Especialmente útil para normalizar esquemas tras unir fuentes con nomenclaturas distintas.

```yaml
transformation:
  - name: renamed
    renameColumns:
      from: source_table
      mappings:
        old_name: new_name
        created: created_at
        usr_id: user_id
```

### CastColumns

Cambia el tipo de dato de columnas existentes. Los tipos son los tipos de Spark SQL (`integer`, `double`, `string`, `boolean`, `timestamp`, `date`, etc.).

```yaml
transformation:
  - name: casted
    castColumns:
      from: source_table
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

Para los casos donde la expresividad del YAML se queda corta, puedes escribir SQL arbitrario directamente. Teckel registra todos los assets del pipeline como vistas temporales antes de ejecutar la query, así que puedes referenciar cualquiera de ellos por nombre.

```yaml
transformation:
  - name: result
    sql:
      from: source_table
      query: >
        SELECT t1.*, t2.category, t3.region_name
        FROM source_table t1
        LEFT JOIN categories t2 ON t1.cat_id = t2.id
        JOIN regions t3 ON t1.region_id = t3.id
        WHERE t1.amount > 100
        ORDER BY t1.created_at DESC
```

---

## Operaciones de conjunto

Cuando necesitas combinar datasets verticalmente o calcular diferencias e intersecciones.

### Union

Apila datasets con el mismo esquema. Con `all: true` usa `unionAll` (preserva duplicados), con `all: false` elimina duplicados tras la unión.

```yaml
transformation:
  - name: combined
    union:
      sources: [table1, table2, table3]
      all: true
```

### Intersect

Devuelve solo las filas presentes en todos los datasets listados.

```yaml
transformation:
  - name: common_records
    intersect:
      sources: [table1, table2]
      all: false
```

### Except

Resta el dataset `right` del `left` — devuelve las filas que están en `left` pero no en `right`.

```yaml
transformation:
  - name: new_records
    except:
      left: current_data
      right: previous_data
      all: false
```

---

## Window Functions

Las funciones de ventana son una de las herramientas más potentes de SQL analítico, y en YAML su estructura es especialmente legible. Defines la partición, el orden dentro de cada partición, y las funciones a calcular.

```yaml
transformation:
  - name: ranked
    window:
      from: employees
      partitionBy: [department]
      orderBy: [salary]           # opcional
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

## Transformaciones avanzadas

### Flatten

Aplana structs anidados en columnas planas, y opcionalmente explota arrays. Útil cuando recibes datos JSON con estructura jerárquica y necesitas trabajar con columnas planas.

El separador controla cómo se construyen los nombres de las columnas resultantes: `parent_child` con `_` como separador, o `parent.child` si lo prefieres con otro carácter.

```yaml
transformation:
  - name: flat_data
    flatten:
      from: nested_source
      separator: "_"          # default: "_"
      explodeArrays: true     # default: true
```

### Sample

Toma una muestra aleatoria del dataset. Útil para desarrollo rápido o para generar conjuntos de test reproducibles (con `seed`).

```yaml
transformation:
  - name: sample_data
    sample:
      from: large_table
      fraction: 0.1           # 10% de las filas
      withReplacement: false  # default: false
      seed: 42                # opcional, para reproducibilidad
```

### Repartition

Redistribuye los datos en un número determinado de particiones. Provoca un shuffle completo. Si especificas columnas, reparticiona por hash de esas columnas — útil antes de joins o escrituras particionadas.

```yaml
transformation:
  - name: repartitioned
    repartition:
      from: source_table
      numPartitions: 8
      columns: [region]     # opcional
```

### Coalesce

Reduce el número de particiones sin shuffle. Más eficiente que `repartition` cuando solo necesitas reducir (no aumentar) las particiones, por ejemplo antes de escribir un fichero único.

```yaml
transformation:
  - name: single_file
    coalesce:
      from: source_table
      numPartitions: 1
```

### Pivot

Convierte valores únicos de una columna en columnas separadas. El patrón clásico para transformar datos "long" a "wide". Si no especificas `values`, Spark los infiere de los datos (más costoso).

```yaml
transformation:
  - name: sales_by_quarter
    pivot:
      from: sales
      groupBy: [region, product]
      pivotColumn: quarter
      values: [Q1, Q2, Q3, Q4]   # opcional, pero recomendado para el rendimiento
      agg:
        - "sum(revenue) as revenue"
```

### Unpivot

La operación inversa al pivot: convierte columnas en filas. Transforma datos "wide" a "long". Define qué columnas son identificadores (`ids`) y cuáles son los valores a apilar (`values`).

```yaml
transformation:
  - name: long_format
    unpivot:
      from: wide_table
      ids: [id, product_name]
      values: [jan, feb, mar, apr, may, jun]
      variableColumn: month
      valueColumn: amount
```

### Rollup / Cube

Extensiones del `GROUP BY` para análisis multidimensional. `Rollup` genera subtotales jerárquicos (de izquierda a derecha), `Cube` genera todas las combinaciones posibles de agrupación.

```yaml
transformation:
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

## Transformaciones de lógica de negocio

Estas transformaciones encapsulan patrones habituales que de otro modo requerirían SQL verboso o código Spark específico.

### Conditional

Añade una columna calculada mediante lógica condicional (equivalente a `CASE WHEN`). Las ramas se evalúan en orden — la primera que cumple la condición gana.

```yaml
transformation:
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
      otherwise: "'minimal'"   # opcional
```

### Split

Divide un dataset en dos paths según una condición. En lugar de crear dos `where` separados, `split` genera dos assets en una sola declaración: `pass` recibe las filas que cumplen la condición, y `fail` las que no.

```yaml
transformation:
  - name: routing         # Este nombre no se usa directamente
    split:
      from: transactions
      condition: "amount >= 1000 AND status = 'completed'"
      pass: large_transactions    # asset con filas que cumplen la condición
      fail: small_transactions    # asset con el resto
```

Puedes referenciar `large_transactions` y `small_transactions` en outputs o transformaciones posteriores.

### SCD2

Implementa el patrón Slowly Changing Dimension Type 2 añadiendo las columnas de control de historicidad. Teckel añade la fecha de inicio (`current_timestamp()`), la fecha de fin (`null`) y el flag de registro activo (`true`). La gestión del histórico completo queda en manos del proceso de merge contra el destino.

```yaml
transformation:
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

Enriquece cada fila con datos externos mediante llamadas HTTP. Por cada registro, Teckel llama al endpoint con el valor de `keyColumn` y almacena la respuesta en `responseColumn`. Diseñado para enrichments ligeros — para volúmenes grandes considera materializar los datos de referencia primero.

```yaml
transformation:
  - name: enriched_users
    enrich:
      from: users
      url: "https://api.example.com/profile"
      method: GET                             # default: GET
      keyColumn: user_id
      responseColumn: profile_data
      headers:
        Authorization: "Bearer ${API_TOKEN}"
        Accept: "application/json"
```

---

## Calidad de datos

### SchemaEnforce

Impone un esquema específico sobre el dataset. Tiene tres modos:

- **`strict`**: Proyecta solo las columnas definidas, las castea a sus tipos y filtra nulos si `nullable: false`
- **`permissive`**: Castea las columnas definidas sin proyectar ni filtrar
- **`evolve`**: Como permissive, pero añade las columnas que faltan con su valor `default` (o `null`)

```yaml
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
        - name: score
          dataType: double
          default: "0.0"   # usado en modo evolve
```

### Assertion

Ejecuta checks de calidad de datos y gestiona los fallos según la política configurada. Con `onFailure: fail` lanza una excepción que detiene el pipeline; con `onFailure: warn` imprime los mensajes de error y continúa.

Las reglas pueden ser específicas de una columna (`not_null` es la única regla predefinida) o expresiones SQL arbitrarias a nivel de fila.

```yaml
transformation:
  - name: validated_data
    assertion:
      from: source_table
      onFailure: fail   # fail | warn
      checks:
        - column: email
          rule: not_null
          description: "El email no puede ser nulo"
        - column: age
          rule: "age > 0 AND age < 150"
          description: "La edad debe ser un valor razonable"
        - rule: "amount IS NOT NULL AND amount >= 0"
          description: "El importe debe ser positivo"
```

---

## Componentes custom

### Custom

Delega la transformación a un componente plugable externo. Útil cuando necesitas lógica que no encaja en ninguna de las transformaciones estándar: modelos de ML, llamadas a sistemas externos, transformaciones propietarias, etc.

```yaml
transformation:
  - name: ml_scored
    custom:
      from: features_table
      component: "com.example.MLScoringTransformer"   # clase o nombre registrado
      options:
        model_path: "models/scoring_v2"
        threshold: "0.7"
```

El campo `component` acepta un nombre corto registrado en el `PluginRegistry` o un nombre de clase completamente cualificado. Consulta la guía de [Plugins](plugins.md) para más detalles sobre cómo crear y registrar componentes custom.
