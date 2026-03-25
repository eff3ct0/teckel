# Getting Started

Antes de entrar en detalles, la idea es simple: escribes un YAML que describe qué datos tienes, qué quieres hacerles y dónde quieres dejarlos. Teckel hace el resto — parsea la configuración, construye el plan de ejecución y lo lanza sobre Spark.

Esta guía te lleva desde la instalación hasta ejecutar tu primer pipeline real, paso a paso.

## Requisitos previos

No hay muchos, pero hay que tenerlos claros:

- **JDK 8 o 11** — Spark aún no soporta Java 17 en todas las configuraciones
- **Apache Spark 3.5.x** — necesario en tiempo de ejecución (se aporta como `provided`)
- **sbt** — solo si vas a construir desde fuente o usar Teckel como librería

## Instalación

### Como librería en tu proyecto Scala

Añade `teckel-api` a tu `build.sbt`. Este módulo incluye el parser, el motor de ejecución y la API pública:

```scala
libraryDependencies += "com.eff3ct" %% "teckel-api" % "@VERSION@"
```

Spark viene declarado como `provided`, así que necesitas añadirlo también si tu entorno no lo aporta:

```scala
libraryDependencies += "org.apache.spark" %% "spark-sql" % "@SPARK_VERSION@"
```

### Como herramienta CLI

Si prefieres usarlo como un binario independiente, clona el repositorio y genera el uber JAR:

```bash
git clone https://github.com/eff3ct/teckel.git
cd teckel
sbt cli/assembly
```

El JAR resultante, con Spark ya empaquetado, queda en `cli/target/scala-2.13/teckel-etl_2.13.jar`.

## Tu primer pipeline

Vamos a construir algo concreto: leer un fichero CSV de empleados, filtrar los activos, agruparlos por departamento y escribir el resultado como Parquet.

### 1. Define el pipeline

Crea el fichero `my-pipeline.yaml`:

```yaml
input:
  - name: employees
    format: csv
    path: 'data/employees.csv'
    options:
      header: true
      inferSchema: true

transformation:
  - name: active_employees
    where:
      from: employees
      filter: "status = 'active'"

  - name: department_summary
    group:
      from: active_employees
      by: [department]
      agg:
        - "count(*) as headcount"
        - "avg(salary) as avg_salary"

  - name: sorted_departments
    order:
      from: department_summary
      by: [headcount]
      order: desc

output:
  - name: sorted_departments
    format: parquet
    mode: overwrite
    path: 'data/output/department_summary'
```

Cada transformación tiene un `name` que actúa como referencia para las siguientes. Así se forma el DAG: `employees` → `active_employees` → `department_summary` → `sorted_departments`.

### 2. Haz primero un dry-run

Antes de lanzar el job, es buena práctica revisar el plan de ejecución. Dry-run analiza el YAML, valida las referencias cruzadas y muestra cada paso — sin tocar Spark:

```bash
java -jar teckel-etl_2.13.jar -f my-pipeline.yaml --dry-run
```

Verás algo así:

```
=== Pipeline Execution Plan ===

--- Inputs ---
  [employees] Format: csv | Path: data/employees.csv

--- Transformations ---
  [active_employees] WHERE employees | Condition: status = 'active'
  [department_summary] GROUP BY active_employees | By: department
  [sorted_departments] ORDER BY department_summary | By: headcount (desc)

--- Outputs ---
  [sorted_departments] Format: parquet | Mode: overwrite
```

Si hay referencias rotas o errores de configuración, aparecerán aquí antes de que Spark arranque.

### 3. Ejecútalo

**Con la CLI:**

```bash
java -jar teckel-etl_2.13.jar -f my-pipeline.yaml
```

**Desde código Scala:**

```scala
import com.eff3ct.teckel.api._

val yaml = scala.io.Source.fromFile("my-pipeline.yaml").mkString
unsafeETL[Unit](yaml)
```

## Estructura de un pipeline

Todo fichero YAML de Teckel tiene tres secciones. Dos son obligatorias:

```yaml
input:            # Fuentes de datos (obligatorio)
  - ...

transformation:   # Transformaciones del DAG (opcional)
  - ...

output:           # Destinos de escritura (obligatorio)
  - ...
```

### `input`

Cada entrada define una fuente de datos. El campo `name` es la referencia que usarás en las transformaciones posteriores:

```yaml
input:
  - name: my_source         # Referencia única en el pipeline
    format: csv              # csv, parquet, json, orc, avro, jdbc...
    path: 'data/input.csv'   # Ruta al fichero o URL JDBC
    options:                 # Opciones específicas del formato
      header: true
      sep: '|'
```

### `output`

Cada salida escribe un asset al destino indicado. El `name` debe corresponder a una transformación o a una entrada existente:

```yaml
output:
  - name: my_sink            # Debe coincidir con un asset del pipeline
    format: parquet
    mode: overwrite           # overwrite, append, ignore, error
    path: 'data/output'
    options: {}               # Opciones opcionales del formato
```

### `transformation`

Las transformaciones encadenan operaciones referenciándose por nombre. El orden en el YAML importa solo para la lectura humana — Teckel construye el DAG de dependencias automáticamente:

```yaml
transformation:
  - name: step1
    select:
      from: my_source        # Referencia a un input o transformación anterior
      columns: [id, name]

  - name: step2
    where:
      from: step1            # Referencia a la transformación anterior
      filter: "id > 10"
```

## Siguientes pasos

Con esto ya tienes lo suficiente para escribir pipelines reales. Cuando quieras ir más lejos:

- [Transformations](transformations.md) — referencia completa de las 30+ operaciones disponibles
- [Plugins](plugins.md) — cómo extender Teckel con componentes custom
- [CLI](cli.md) — todas las opciones de línea de comandos
- [API](api.md) — integración programática con Scala y Cats Effect
