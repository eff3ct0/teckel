# Plugins

Las 30+ transformaciones de Teckel cubren la mayoría de los casos, pero siempre hay pipelines que necesitan algo específico: un modelo de ML, un conector propietario, una lógica de negocio que no encaja en SQL. Para eso existe el sistema de plugins.

La idea es sencilla: defines una clase que implementa una de las tres interfaces de Teckel, la registras con un nombre, y la referencias desde el YAML como si fuera una transformación cualquiera. Teckel se encarga de instanciarla y pasarle el DataFrame en el momento correcto.

## Las tres interfaces

El sistema de plugins gira en torno a tres traits que corresponden a las tres fases de un pipeline: lectura, transformación y escritura.

### TeckelReader

Para fuentes de datos personalizadas. Recibe un mapa de opciones de configuración y devuelve un DataFrame.

```scala mdoc:compile-only
import com.eff3ct.teckel.semantic.plugin.TeckelReader
import org.apache.spark.sql.{DataFrame, SparkSession}

class MyCustomReader extends TeckelReader {
  def read(options: Map[String, String])(implicit spark: SparkSession): DataFrame = {
    val path     = options("path")
    val encoding = options.getOrElse("encoding", "utf-8")
    // Cualquier lógica de lectura con el SparkSession disponible
    spark.read.option("encoding", encoding).format("text").load(path)
  }
}
```

### TeckelTransformer

Para transformaciones personalizadas. Recibe el DataFrame entrante y el mapa de opciones, y devuelve el DataFrame transformado.

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

Para destinos de escritura personalizados. Recibe el DataFrame final y las opciones, y gestiona la escritura.

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

## Registrar un plugin

Los plugins se registran en `PluginRegistry` asociando un nombre corto a la clase. El registro se hace normalmente al arrancar la aplicación, antes de ejecutar cualquier pipeline.

```scala mdoc:compile-only
import com.eff3ct.teckel.model.plugin.PluginRegistry
import com.eff3ct.teckel.semantic.plugin.TeckelTransformer

class ScaleTransformer extends TeckelTransformer {
  def transform(options: Map[String, String], df: org.apache.spark.sql.DataFrame) = {
    val factor = options.getOrElse("factor", "1.0").toDouble
    df.withColumn("scaled", df("value") * factor)
  }
}

// Registra la clase (no una instancia — Teckel la instancia cuando la necesita)
PluginRegistry.register("transformer", "scale", classOf[ScaleTransformer])
```

El primer argumento es el tipo (`"reader"`, `"transformer"`, o `"writer"`), el segundo es el nombre corto con el que lo referenciarás desde el YAML.

## Usar el plugin en el pipeline

Una vez registrado, el plugin se invoca con el tipo de transformación `custom`. El campo `component` puede ser el nombre corto del registry o el nombre completamente cualificado de la clase:

```yaml
transformation:
  - name: scaled_values
    custom:
      from: source_table
      component: "scale"        # nombre corto registrado
      options:
        factor: "2.5"
        column: "price"
```

```yaml
transformation:
  - name: scaled_values
    custom:
      from: source_table
      component: "com.example.ScaleTransformer"   # clase completa, sin registro previo
      options:
        factor: "2.5"
```

Ambas formas funcionan. La primera requiere registro previo y es más legible; la segunda es útil cuando tienes la clase en el classpath pero no quieres gestionar el registro explícitamente.

## Declarar componentes en el YAML

También puedes declarar los componentes directamente en la configuración del pipeline usando la sección `config`. Esto hace que el pipeline sea más autocontenido — cualquiera que lo lea ve qué componentes externos necesita:

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

## Consultar los plugins registrados

Puedes inspeccionar en cualquier momento qué plugins hay registrados para un tipo:

```scala mdoc:compile-only
import com.eff3ct.teckel.model.plugin.PluginRegistry

val transformers: Set[String] = PluginRegistry.list("transformer")
val readers: Set[String]      = PluginRegistry.list("reader")
val writers: Set[String]      = PluginRegistry.list("writer")
```
