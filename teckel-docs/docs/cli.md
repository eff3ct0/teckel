# CLI

La CLI de Teckel es la forma más directa de usar el framework: construyes el uber JAR, apuntas a tu pipeline YAML y listo. No hace falta escribir ni una línea de Scala.

## Construir el JAR

El artefacto CLI incluye Spark empaquetado, así que no necesitas tener Spark instalado por separado:

```bash
sbt cli/assembly
```

El JAR resultante queda en `cli/target/scala-2.13/teckel-etl_2.13.jar`. Este es el binario que usarás para todo.

## Ejecutar un pipeline

### Desde un fichero YAML

La forma más habitual. Apuntas al fichero con `-f`:

```bash
java -jar teckel-etl_2.13.jar -f pipeline.yaml
```

### Desde stdin

Si generas el YAML dinámicamente o lo recibes de otra herramienta, puedes pasarlo por stdin con `-c`:

```bash
cat pipeline.yaml | java -jar teckel-etl_2.13.jar -c

# O generado dinámicamente
envsubst < pipeline.template.yaml | java -jar teckel-etl_2.13.jar -c
```

## Inspeccionar antes de ejecutar

Teckel tiene varias herramientas para entender lo que va a pasar antes de lanzar el job de Spark. Son especialmente útiles en CI, en revisiones de código, o simplemente cuando estás depurando un pipeline nuevo.

### Dry-run

Analiza el pipeline, valida las referencias cruzadas entre assets y muestra el plan de ejecución paso a paso. No instancia ningún SparkSession — es completamente ligero:

```bash
java -jar teckel-etl_2.13.jar -f pipeline.yaml --dry-run
```

Si hay referencias rotas (un `from` que apunta a un asset que no existe, por ejemplo), aparecerán en el report antes de que intentes ejecutar nada.

### Generar documentación

Convierte el pipeline en un documento Markdown legible por humanos. Describe cada fuente, transformación y destino con sus parámetros:

```bash
java -jar teckel-etl_2.13.jar -f pipeline.yaml --doc
```

Útil para mantener la documentación sincronizada con el código — si el YAML cambia, el doc refleja el cambio automáticamente.

### Visualizar el DAG

Genera un grafo del pipeline que muestra las dependencias entre assets:

```bash
java -jar teckel-etl_2.13.jar -f pipeline.yaml --graph
```

La salida por defecto es un diagrama Mermaid, que puedes pegar directamente en GitHub, Notion o cualquier herramienta que lo soporte.

## Variables de entorno

Los valores en el YAML pueden referenciar variables de entorno usando la sintaxis `${VARIABLE}`. Esto permite tener un pipeline genérico y parametrizarlo en tiempo de ejecución:

```yaml
input:
  - name: source
    format: csv
    path: '${INPUT_PATH}'
    options:
      header: true

output:
  - name: source
    format: parquet
    mode: overwrite
    path: '${OUTPUT_PATH}'
```

Pasa las variables como variables de entorno al lanzar el JAR:

```bash
INPUT_PATH=data/raw/2024-01.csv \
OUTPUT_PATH=data/processed/2024-01 \
java -jar teckel-etl_2.13.jar -f pipeline.yaml
```

## Servidor REST embebido

Para casos donde quieres exponer tus pipelines como un servicio HTTP — integración con orquestadores, plataformas de datos, o simplemente para invocarlo desde otras herramientas — Teckel incluye un servidor embebido sin dependencias adicionales:

```bash
java -jar teckel-etl_2.13.jar --server --port 8080
```

Los endpoints disponibles son:

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/health` | Comprueba que el servidor está activo |
| `POST` | `/dry-run` | Devuelve el plan de ejecución de un pipeline (body: YAML) |
| `POST` | `/doc` | Genera documentación Markdown de un pipeline |
| `POST` | `/graph` | Genera el DAG del pipeline en Mermaid |
| `POST` | `/validate` | Valida la sintaxis y referencias de un pipeline |

Todos los endpoints `POST` esperan el YAML del pipeline como cuerpo de la petición y devuelven texto plano.
