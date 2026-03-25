# Plugins

Teckel's 30+ built-in transformations cover most cases, but there are always pipelines that need something specific: an ML model, a proprietary connector, business logic that doesn't fit in SQL. That's what the plugin system is for.

The idea is straightforward: define a class that implements one of Teckel's three interfaces, register it with a name, and reference it from YAML as if it were any other transformation. Teckel handles instantiation and passes the DataFrame at the right moment.

## The Three Interfaces

The plugin system revolves around three traits corresponding to the three phases of a pipeline: reading, transforming, and writing.

### TeckelReader

For custom data sources. Receives a configuration options map and returns a DataFrame.

```scala mdoc:compile-only
import com.eff3ct.teckel.semantic.plugin.TeckelReader
import org.apache.spark.sql.{DataFrame, SparkSession}

class MyCustomReader extends TeckelReader {
  def read(options: Map[String, String])(implicit spark: SparkSession): DataFrame = {
    val path     = options("path")
    val encoding = options.getOrElse("encoding", "utf-8")
    spark.read.option("encoding", encoding).format("text").load(path)
  }
}
```

### TeckelTransformer

For custom transformations. Receives the incoming DataFrame and the options map, returns the transformed DataFrame.

```scala mdoc:compile-only
import com.eff3ct.teckel.semantic.plugin.TeckelTransformer
import org.apache.spark.sql.DataFrame

class ScalingTransformer extends TeckelTransformer {
  def transform(options: Map[String, String], df: DataFrame): DataFrame = {
    val column = options("column")
    val factor = options.getOrElse("factor", "1.0").toDouble
    df.withColumn(column, df(column) * factor)
  }
}
```

### TeckelWriter

For custom data sinks. Receives the final DataFrame and the options, handles the write.

```scala mdoc:compile-only
import com.eff3ct.teckel.semantic.plugin.TeckelWriter
import org.apache.spark.sql.DataFrame

class CustomWriter extends TeckelWriter {
  def write(options: Map[String, String], df: DataFrame): Unit = {
    val path = options("path")
    val mode = options.getOrElse("mode", "overwrite")
    df.write.mode(mode).format("delta").save(path)
  }
}
```

## Registering a Plugin

Plugins are registered in `PluginRegistry` by associating a short name with the class. Registration happens at application startup, before any pipeline executes. Notice that you register the class (not an instance) — Teckel instantiates it when needed.

```scala mdoc:compile-only
import com.eff3ct.teckel.model.plugin.PluginRegistry
import com.eff3ct.teckel.semantic.plugin.TeckelTransformer

class ScaleTransformer extends TeckelTransformer {
  def transform(options: Map[String, String], df: org.apache.spark.sql.DataFrame) = {
    val factor = options.getOrElse("factor", "1.0").toDouble
    df.withColumn("scaled", df("value") * factor)
  }
}

// Register the class (not an instance — Teckel instantiates it when needed)
PluginRegistry.register("transformer", "scale", classOf[ScaleTransformer])
```

## Using a Plugin in YAML

Once registered, a plugin is invoked using the `custom` transformation type. The `component` field accepts either the short registry name or the fully-qualified class name:

```yaml
# Using the short registry name
transformation:
  - name: scaled_values
    custom:
      from: source
      component: "scale"
      options:
        factor: "2.5"
        column: "price"
```

```yaml
# Using the full class name (no prior registration needed)
transformation:
  - name: scaled_values
    custom:
      from: source
      component: "com.example.ScaleTransformer"
      options:
        factor: "2.5"
```

Both forms work. The first requires prior registration and is more readable; the second is useful when the class is on the classpath but you don't want to manage explicit registration.

## Declaring Components in Pipeline Config

You can also declare components directly in the pipeline's `config` section. This makes the pipeline more self-contained — anyone reading it can see what external components it depends on:

```yaml
config:
  components:
    transformer:
      - name: scale
        class: com.example.ScaleTransformer
    reader:
      - name: custom_csv
        class: com.example.CustomCsvReader

input:
  - name: raw
    format: csv
    path: 'data/input.csv'
    options:
      header: true

transformation:
  - name: result
    custom:
      from: raw
      component: scale
      options:
        factor: "2.0"
        column: "amount"

output:
  - name: result
    format: parquet
    mode: overwrite
    path: 'data/output'
```

## Inspecting the Registry

At any point you can check what plugins are registered for a given type:

```scala mdoc:compile-only
import com.eff3ct.teckel.model.plugin.PluginRegistry

val transformers: Set[String] = PluginRegistry.list("transformer")
val readers: Set[String]      = PluginRegistry.list("reader")
val writers: Set[String]      = PluginRegistry.list("writer")
```
