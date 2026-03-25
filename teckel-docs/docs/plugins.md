# Plugins

Extend Teckel with custom readers, transformers, and writers using the `PluginRegistry`.

## Interfaces

### TeckelReader

```scala mdoc:compile-only
import com.eff3ct.teckel.semantic.plugin.TeckelReader
import org.apache.spark.sql.{DataFrame, SparkSession}

class MyReader extends TeckelReader {
  def read(options: Map[String, String])(implicit spark: SparkSession): DataFrame =
    spark.read.format("custom").load(options("path"))
}
```

### TeckelTransformer

```scala mdoc:compile-only
import com.eff3ct.teckel.semantic.plugin.TeckelTransformer
import org.apache.spark.sql.DataFrame

class MyTransformer extends TeckelTransformer {
  def transform(options: Map[String, String], df: DataFrame): DataFrame = {
    val factor = options.getOrElse("factor", "1.0").toDouble
    df.withColumn("scaled", df("value") * factor)
  }
}
```

### TeckelWriter

```scala mdoc:compile-only
import com.eff3ct.teckel.semantic.plugin.TeckelWriter
import org.apache.spark.sql.DataFrame

class MyWriter extends TeckelWriter {
  def write(options: Map[String, String], df: DataFrame): Unit =
    df.write.mode(options.getOrElse("mode", "overwrite")).format("custom").save(options("path"))
}
```

## Registering

```scala mdoc:compile-only
import com.eff3ct.teckel.model.plugin.PluginRegistry
import com.eff3ct.teckel.semantic.plugin.TeckelTransformer

class ScaleTransformer extends TeckelTransformer {
  def transform(options: Map[String, String], df: org.apache.spark.sql.DataFrame) = {
    val factor = options.getOrElse("factor", "1.0").toDouble
    df.withColumn("scaled", df("value") * factor)
  }
}

// Call at application startup
PluginRegistry.register("transformer", "scale", classOf[ScaleTransformer])
```

## Using in YAML

```yaml
transformation:
  - name: scaled
    custom:
      from: source
      component: "scale"            # short registry name
      options:
        factor: "2.5"
```

The `component` field accepts:
- A short name registered in `PluginRegistry` (e.g., `scale`)
- A fully-qualified class name (e.g., `com.example.ScaleTransformer`)

## Declaring in Pipeline Config

```yaml
config:
  components:
    transformer:
      - name: scale
        class: com.example.ScaleTransformer

input:
  - name: source
    format: csv
    path: 'data/input.csv'
    options:
      header: true

transformation:
  - name: result
    custom:
      from: source
      component: scale
      options:
        factor: "2.0"

output:
  - name: result
    format: parquet
    mode: overwrite
    path: 'data/output'
```

## Inspecting the Registry

```scala mdoc:compile-only
import com.eff3ct.teckel.model.plugin.PluginRegistry

val transformers: Set[String] = PluginRegistry.list("transformer")
val readers: Set[String]      = PluginRegistry.list("reader")
val writers: Set[String]      = PluginRegistry.list("writer")
```
