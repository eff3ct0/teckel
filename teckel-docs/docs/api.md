# API Reference

Cuando la CLI no es suficiente — porque necesitas integrar Teckel en una aplicación existente, controlar el ciclo de vida del SparkSession, o componer pipelines con otra lógica — usas el módulo `teckel-api` directamente desde Scala.

El API está construido sobre Cats Effect y ofrece tres entry points con distintos niveles de abstracción. Elige el que mejor encaje con tu estilo de código.

## Entry Points

### `etl[F, O]` — Fully polymorphic

El entry point más genérico. Parametrizado sobre el efecto `F[_]` y el tipo de resultado `O`. Útil si ya tienes un stack de efectos definido y quieres integrar Teckel sin fijar `IO`.

```scala
import com.eff3ct.teckel.api._
import cats.effect.IO

val yaml: String =
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

// F = IO, O = Unit → ejecuta el pipeline y escribe el output
// Requiere SparkSession implícito y EvalContext[Unit] en scope
val program: IO[Unit] = etl[IO, Unit](yaml)
```

### `etlIO[O]` — Fija F = IO

El más habitual cuando trabajas con Cats Effect. Fija `F = IO` para no tener que especificarlo:

```scala
import com.eff3ct.teckel.api._
import cats.effect.IO

val yaml: String = "..." // contenido YAML

val program: IO[Unit] = etlIO[Unit](yaml)
```

### `unsafeETL[O]` — Síncrono y bloqueante

Ejecuta el pipeline de forma síncrona. Sin efectos, sin IO. Conveniente para scripts, jobs batch simples, o tests donde no quieres gestionar runtime de Cats Effect:

```scala
import com.eff3ct.teckel.api._

val yaml: String = "..." // contenido YAML

unsafeETL[Unit](yaml) // bloquea hasta completar
```

## Modos de evaluación

El tipo `O` que pasas a cualquiera de los entry points determina qué hace Teckel con el pipeline. Hay dos contextos de evaluación disponibles.

### Execute (`O = Unit`)

El modo por defecto. Ejecuta el DAG completo y escribe los outputs a sus destinos. Es el comportamiento que esperarías de un job ETL normal.

```scala
import com.eff3ct.teckel.api._

unsafeETL[Unit](yaml)  // ejecuta y escribe
```

### Debug (`O = Context[DataFrame]`)

En lugar de escribir los outputs, devuelve todos los DataFrames intermedios como un `Map[String, DataFrame]`. Extremadamente útil para testing y depuración — puedes inspeccionar el estado del pipeline en cada paso sin tocar el almacenamiento.

```scala
import com.eff3ct.teckel.api._
import com.eff3ct.teckel.model.Context
import org.apache.spark.sql.DataFrame

val assets: Context[DataFrame] = unsafeETL[Context[DataFrame]](yaml)

// Todos los assets del pipeline están disponibles por nombre
assets.foreach { case (name, df) =>
  println(s"\n=== $name ===")
  df.show(10)
  df.printSchema()
}

// O accede a uno específico
val activeUsers: DataFrame = assets("active_users")
```

Este modo es especialmente valioso en tests de integración: puedes verificar el resultado de transformaciones intermedias sin necesitar un sistema de ficheros real.

## Inspección sin Spark

Estas utilidades analizan el YAML y generan información sobre el pipeline sin necesitar un SparkSession — son puramente funcionales y muy rápidas.

### Dry Run

Genera el plan de ejecución en texto: qué pasos se van a ejecutar, en qué orden y con qué configuración. También valida las referencias entre assets.

```scala mdoc:compile-only
import com.eff3ct.teckel.api.DryRun

val yaml: String = "..." // contenido YAML

DryRun.explain(yaml) match {
  case Right(plan)  => println(plan)
  case Left(error)  => println(s"Error: ${error.getMessage}")
}
```

### DocGen

Convierte el pipeline en documentación Markdown. Describe todas las fuentes, transformaciones y destinos con sus parámetros. Útil para mantener la documentación sincronizada con el pipeline real.

```scala mdoc:compile-only
import com.eff3ct.teckel.api.DocGen

val yaml: String = "..." // contenido YAML

DocGen.generate(yaml).foreach(println)
```

### GraphViz

Genera una representación gráfica del DAG. Soporta tres formatos: `mermaid` (el más habitual para GitHub/Notion), `dot` (para Graphviz) y `ascii` (para terminales).

```scala mdoc:compile-only
import com.eff3ct.teckel.api.GraphViz

val yaml: String = "..." // contenido YAML

// Mermaid — se puede pegar directamente en GitHub o Notion
val mermaid: Either[Throwable, String] = GraphViz.generate(yaml, "mermaid")

// DOT — para renderizar con graphviz o dot
val dot: Either[Throwable, String] = GraphViz.generate(yaml, "dot")

// ASCII — para terminales y logs
val ascii: Either[Throwable, String] = GraphViz.generate(yaml, "ascii")

mermaid.foreach(println)
```
