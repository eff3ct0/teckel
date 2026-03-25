# Teckel

Escribir pipelines ETL con Spark suele ser una historia de boilerplate: leer de aquí, transformar allá, escribir acullá... y todo en código que en tres meses ni tú mismo recuerdas. Teckel parte de la idea contraria: si la lógica es declarativa, debería poder expresarse como datos.

**Teckel** es un framework Scala que convierte ficheros YAML en pipelines Apache Spark completos. Defines qué quieres hacer, no cómo hacerlo. El framework construye el DAG, lo valida y lo ejecuta sobre Spark — tú te quedas con la lógica de negocio, no con el scaffolding.

Construido con Scala @SCALA_VERSION@, Apache Spark @SPARK_VERSION@, y el ecosistema Cats/Cats-Effect.

## ¿Qué incluye?

- **Pipelines declarativos en YAML** — define fuentes, transformaciones y destinos sin una línea de Spark
- **Más de 30 transformaciones** — Select, Where, GroupBy, Join, Window, Pivot, SCD2, Assertion y muchas más
- **Componentes plugables** — extiende Teckel con lectores, transformadores y escritores custom
- **Dry-run** — previsualiza el plan de ejecución antes de lanzar ningún job
- **Generación de documentación** — convierte cualquier pipeline en Markdown legible
- **Visualización del DAG** — exporta el grafo en Mermaid, DOT o ASCII
- **Validación de pipelines** — detecta referencias rotas y errores de configuración en tiempo de análisis
- **Servidor REST embebido** — expone tus pipelines como endpoints HTTP sin dependencias adicionales

## Quickstart

### Añadir como dependencia

```scala
libraryDependencies += "com.eff3ct" %% "teckel-api" % "@VERSION@"
```

Spark se declara como `provided` — debes aportarlo desde tu entorno de ejecución.

### Tu primer pipeline

Crea un fichero `pipeline.yaml`:

```yaml
input:
  - name: users
    format: csv
    path: 'data/users.csv'
    options:
      header: true
      inferSchema: true

transformation:
  - name: active_users
    where:
      from: users
      filter: "status = 'active'"

  - name: sorted_users
    order:
      from: active_users
      by: [created_at]
      order: desc

output:
  - name: sorted_users
    format: parquet
    mode: overwrite
    path: 'data/output/active_users'
```

Ejecútalo desde la CLI:

```bash
java -jar teckel-etl_2.13.jar -f pipeline.yaml
```

O directamente desde código Scala:

```scala
import com.eff3ct.teckel.api._
import cats.effect.IO

val yaml: String = /* contenido del YAML */
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
val program: IO[Unit] = etlIO[Unit](yaml)
```

## Módulos

El proyecto está dividido en capas con responsabilidades bien separadas:

| Módulo | Artefacto | Responsabilidad |
|--------|-----------|-----------------|
| `model` | `teckel-model` | Tipos de dominio: `Asset`, `Source`, `Transformation` |
| `serializer` | `teckel-serializer` | Parseo de YAML con Circe + circe-yaml |
| `semantic` | `teckel-semantic` | Motor de ejecución Spark |
| `api` | `teckel-api` | API pública de la librería |
| `cli` | `teckel-cli` | Punto de entrada por línea de comandos |

## Arquitectura

```
model --> serializer --> api --> cli
  \-------> semantic -->/
```

El módulo `model` define los tipos de dominio centrales — es la lingua franca del sistema. El `serializer` parsea el YAML, lo convierte a representaciones intermedias y las reescribe al modelo de dominio. El módulo `semantic` opera sobre ese modelo para ejecutar las transformaciones Spark. El `api` une todo y expone tres entry points según el estilo de uso. Finalmente, `cli` es la capa más externa: recibe argumentos, llama al `api` y gestiona el ciclo de vida.

Ninguna capa conoce los detalles de las capas superiores, lo que hace que añadir backends o frontends alternativos sea una cuestión de extender sin modificar.
