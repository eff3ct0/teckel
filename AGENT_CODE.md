# Agent prompt — Evolución de código Scala para Teckel

> Usa este documento como contexto completo antes de tocar cualquier fichero.
> Contiene arquitectura, patrones, deuda técnica y guías de trabajo.

---

## Qué es Teckel

Framework Scala 2.13 para definir pipelines ETL sobre Apache Spark usando YAML
declarativo. El usuario escribe un fichero YAML y Teckel lo ejecuta como un job
Spark: lee datos, aplica transformaciones (Select, Where, GroupBy, OrderBy, Join)
y escribe el resultado.

Repo: `https://github.com/eff3ct0/teckel`
Local: `/home/p3rkins/git/eff3ct/teckel`

---

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Scala 2.13.12 (cross 2.12.18) |
| Computación | Apache Spark 3.5.3 (`% Provided`) |
| Efectos | Cats Effect 3.5.5 + Cats 2.12.0 |
| Serialización | Circe 0.14.4 + circe-yaml 1.15.0 |
| Streaming | FS2 3.9.3 |
| Tests | ScalaTest 3.2.9 + spark-testing-base 3.5.3_2.0.1 |
| Build | sbt + sbt-assembly (Uber JAR) + sbt-ci-release (Sonatype) |

---

## Arquitectura de módulos

```
root
 ├── model       → ADTs del dominio (Source, Asset, transformaciones)
 ├── serializer  → Decode/encode YAML/JSON ↔ modelos Scala (Circe)
 ├── semantic    → Typeclasses de evaluación + integración Spark
 ├── api         → Interfaz pública de alto nivel (Run[F[_]])
 └── cli         → Uber JAR ejecutable en Spark (-f file / -c stdin)
```

**Grafo de dependencias:**

```
Serializer ──┐
             ├──→ API ──→ CLI
Model ───────┤
             └──→ Semantic ──┘
```

### Flujo de ejecución completo

```
YAML/JSON (string o fichero)
  ↓  Serializer.decode[ETL]          (Circe: JSON → case classes)
ETL(input, transformations, output)
  ↓  Rewrite.rewrite                 (serializer → model interno)
Context[Asset]                       (Map[AssetRef, Asset])
  ↓  Run[F].run                      (orquesta evaluación)
  ↓  EvalContext[T].eval(context)
Para cada Asset:
  Input       → Debug.input()        → Spark reader → DataFrame
  Transformation → Debug.*()        → Spark SQL op → DataFrame
  Output      → Exec[Output]         → Spark writer
  ↓
T (Unit si produce, Context[DataFrame] si debug)
```

---

## Typeclasses principales

Antes de hacer cualquier cambio, entiende estos contratos:

```scala
// Evaluador genérico: dado S y una entrada I, produce O
trait Semantic[S, -I, +O] {
  def eval(source: S, input: I): O
}

// Evalúa un Asset contra un contexto ya construido
trait EvalAsset[T] {
  def eval(asset: Asset, context: Context[DataFrame]): T
}

// Evalúa el contexto completo (todos los assets en orden)
trait EvalContext[T] {
  def eval(context: Context[Asset]): T
}

// Escribe la salida (Output → disco)
trait Exec[S] {
  def exec(source: S, df: DataFrame): Unit
}

// Orquesta el flujo completo: String/File → T
trait Run[F[_]] {
  def run[T: EvalContext](etl: String): F[Either[Error, T]]
}
```

Los implicit instances de estos traits viven en `semantic/evaluation.scala`
y `semantic/execution.scala`. Añadir soporte para nuevas transformaciones
significa añadir instancias, no modificar los traits.

---

## Deuda técnica conocida (TODOs en código)

### 1. Estado mutable impuro — `model/package.scala:36`

```scala
// TODO. Use a Effect Mutable State to keep track of the already evaluated assets
type Mutex[T] = MMap[AssetRef, T]   // scala.collection.mutable.Map
```

**Impacto:** el caché de DataFrames evaluados usa un Map mutable compartido.
Dificulta testing, paralelización y razonamiento sobre efectos.

**Solución:** reemplazar con `cats.effect.Ref[F, Map[AssetRef, T]]` y propagar
el efecto F hasta los puntos de uso.

---

### 2. OrderBy sin ASC/DESC — `semantic/sources/Debug.scala:68`

```scala
// TODO: implement the asc/desc order
def orderBy[S <: OrderBy](df: DataFrame, source: S): DataFrame =
  df.orderBy(source.by.toList.map(df(_)): _*)
  // ignora source.order: Option[String]
```

**Impacto:** toda ordenación es ascendente. El campo `order` del YAML se parsea
pero se descarta.

**Solución:** mapear `source.order` a `Column.asc` / `Column.desc` de Spark.
Revisar primero el modelo en `serializer/model/transformation.scala` para ver
cómo se representa `order`.

---

### 3. Coverage mínimo demasiado bajo — `project/Extension.scala:42`

```scala
coverageMinimumStmtTotal := 30 // TODO. provisional
```

**Solución:** aumentar progresivamente; objetivo razonable 60-70% statement.

---

## Áreas de mejora prioritarias

### A. Validación semántica del YAML

Hoy la validación es solo sintáctica (Circe). No se detectan:
- Referencias a assets no definidos
- Ciclos en el grafo de transformaciones
- Tipos de formato no soportados por Spark
- Paths vacíos o nulos

Crear un paso de validación entre `Rewrite.rewrite` y `EvalContext.eval`
que devuelva `Either[NonEmptyList[ValidationError], Context[Asset]]`.

---

### B. Mejora del modelo de errores

`Run[F].run` devuelve `F[Either[Error, T]]` donde `Error` es genérico.
Los errores de Circe (decode) y los de Spark (evaluación) se mezclan.

Definir un ADT de errores específico:

```scala
sealed trait TeckelError
case class ParseError(msg: String, cursor: CursorHistory)  extends TeckelError
case class ValidationError(msg: String)                     extends TeckelError
case class EvaluationError(asset: AssetRef, cause: Throwable) extends TeckelError
case class WriteError(output: AssetRef, cause: Throwable)  extends TeckelError
```

---

### C. Nuevas transformaciones

El modelo `Source` en `model/Source.scala` define las transformaciones como ADT.
Añadir una nueva transformación requiere:

1. Añadir case class al sealed trait `Transformation` en `model/Source.scala`
2. Añadir decodificador Circe en `serializer/model/transformation.scala`
3. Añadir la reescritura en `serializer/transform/Rewrite.scala`
4. Añadir la función de evaluación Spark en `semantic/sources/Debug.scala`
5. Añadir ejemplo YAML en `docs/etl/`
6. Añadir tests

**Candidatos concretos:**
- `Limit(n: Int)` — `df.limit(n)`
- `Distinct` — `df.distinct()`
- `Rename(from: String, to: String)` — `df.withColumnRenamed(from, to)`
- `WithColumn(name: String, expr: String)` — `df.withColumn(name, expr(expression))`
- `Union` — `df.union(other)`
- `Drop(columns: List[String])` — `df.drop(columns: _*)`

---

### D. Soporte de nuevos formatos de entrada/salida

Hoy los formatos soportados son los que Spark acepta como string (`csv`, `json`,
`parquet`). No hay validación. Ampliar a:

- `delta` — Delta Lake (requiere dependencia opcional)
- `orc`
- `avro` (requiere `spark-avro`)
- `jdbc` — requiere opciones específicas (url, driver, dbtable)

---

### E. Tests

**Qué falta:**

| Módulo | Fichero | Qué testear |
|---|---|---|
| cli | Console, Parser | Parseo de args, lectura de STDIN/fichero |
| semantic | Exec | Escritura a disco (Parquet, CSV, JSON) |
| semantic | Debug.orderBy | ASC y DESC una vez implementado |
| serializer | PrimitiveType | Todos los tipos primitivos |
| api | Run[IO] | Flujo completo con ficheros de test |
| api | Error paths | YAML malformado, asset no encontrado |

**Patrón de tests con Spark:**

```scala
// Usar spark-testing-base: extiende SparkFunSuite o SharedSparkContext
class MySpec extends AnyFlatSpecLike with SharedSparkContext {
  "Something" should "work" in {
    val df = sc.parallelize(Seq(1, 2, 3)).toDF("value")
    // ...
  }
}
```

---

## Guías de estilo de código

El proyecto usa los siguientes patrones — mantenlos en cualquier contribución:

### No rompas la frontera de efectos

El módulo `model` y `serializer` son **puros** (sin IO, sin Spark, sin Ref).
Los efectos viven en `semantic` y `api`. No introduzcas dependencias de
cats-effect en `model` o `serializer`.

### Implicit instances como objetos anónimos o vals

```scala
// ✓ Correcto — instancia implícita como val/object anónimo
implicit val evalInput: Semantic[Input, Context[DataFrame], DataFrame] =
  (source, ctx) => Debug.input(source)

// ✗ Evitar — clase concreta innecesaria
class InputSemantic extends Semantic[Input, ...] { ... }
```

### ADTs cerrados con sealed trait

Todas las transformaciones y modelos de dominio deben ser `sealed`.
El compilador debe exhaustividad en pattern matching.

### Errores como Either, nunca throw en código funcional

```scala
// ✓
def decode(s: String): Either[Error, ETL] = parser.decode[ETL](s)

// ✗
def decode(s: String): ETL = parser.decode[ETL](s).getOrElse(throw ...)
```

### scalafmt

El proyecto tiene `sbt-scalafmt`. Antes de commitear:

```bash
sbt scalafmtAll
```

---

## Comandos útiles

```bash
# Compilar todo
sbt compile

# Tests de un módulo
sbt "serializer/test"
sbt "semantic/test"
sbt "api/test"

# Todos los tests con coverage
sbt clean coverage test coverageReport coverageAggregate

# Formatear código
sbt scalafmtAll

# Generar el Uber JAR del CLI
sbt "cli/assembly"
# → cli/target/scala-2.13/teckel-etl_2.13.jar

# Ejecutar un ETL de ejemplo
spark-submit --class com.eff3ct.teckel.app.Main \
  cli/target/scala-2.13/teckel-etl_2.13.jar \
  -f docs/etl/simple.yaml
```

---

## Ficheros clave — mapa de referencia rápida

| Qué buscar | Dónde está |
|---|---|
| ADTs del dominio | `model/src/main/scala/com/eff3ct/teckel/model/Source.scala` |
| Type aliases | `model/src/main/scala/com/eff3ct/teckel/model/package.scala` |
| Typeclass Semantic | `semantic/src/main/scala/com/eff3ct/teckel/semantic/core/Semantic.scala` |
| Lógica Spark (transformaciones) | `semantic/src/main/scala/com/eff3ct/teckel/semantic/sources/Debug.scala` |
| Implicit instances de evaluación | `semantic/src/main/scala/com/eff3ct/teckel/semantic/evaluation.scala` |
| Escritura a disco | `semantic/src/main/scala/com/eff3ct/teckel/semantic/sources/Exec.scala` |
| Decode YAML/JSON | `serializer/src/main/scala/com/eff3ct/teckel/serializer/alternative.scala` |
| Modelos serializables | `serializer/src/main/scala/com/eff3ct/teckel/serializer/model/` |
| Reescritura serializer→model | `serializer/src/main/scala/com/eff3ct/teckel/transform/Rewrite.scala` |
| Interfaz pública (Run[F]) | `api/src/main/scala/com/eff3ct/teckel/api/core/Run.scala` |
| SparkETL trait | `api/src/main/scala/com/eff3ct/teckel/api/spark/SparkETL.scala` |
| CLI entrypoint | `cli/src/main/scala/com/eff3ct/teckel/app/Main.scala` |
| Dependencias y versiones | `project/Dependency.scala`, `project/Version.scala` |
| Ejemplos YAML | `docs/etl/` |

---

## Checklist antes de cada PR

- [ ] `sbt scalafmtAll` sin cambios pendientes
- [ ] `sbt compile` sin warnings nuevos
- [ ] Tests del módulo afectado pasan: `sbt "<modulo>/test"`
- [ ] Si añades transformación: ejemplo YAML en `docs/etl/`
- [ ] Si cambias API pública: actualizar `example/` y `docs/`
- [ ] Si corriges un TODO del código: eliminar el comentario TODO
- [ ] Coverage no baja respecto al baseline: `sbt coverage test coverageReport`
