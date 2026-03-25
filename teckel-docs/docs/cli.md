# CLI

The CLI is the most direct way to use Teckel: build the uber JAR, point it at your YAML pipeline, and you're done. No Scala code required.

## Building the JAR

The CLI artifact packages Spark inside, so you don't need a separate Spark installation:

```bash
sbt cli/assembly
# Output: cli/target/scala-2.13/teckel-etl_2.13.jar
```

## Running a Pipeline

The most common usage: point to a YAML file with `-f`.

```bash
java -jar teckel-etl_2.13.jar -f pipeline.yaml
```

If you generate the YAML dynamically or receive it from another tool, you can pipe it via stdin with `-c`:

```bash
cat pipeline.yaml | java -jar teckel-etl_2.13.jar -c

# Or generated dynamically
envsubst < pipeline.template.yaml | java -jar teckel-etl_2.13.jar -c
```

## Inspecting Before Running

Teckel has several tools to understand what's going to happen before launching the Spark job. They're especially useful in CI, during code reviews, or when you're debugging a new pipeline.

### Dry-run

Parses the pipeline, validates all cross-references between assets, and prints the execution plan step by step. It doesn't instantiate any SparkSession — it's completely lightweight:

```bash
java -jar teckel-etl_2.13.jar -f pipeline.yaml --dry-run
```

If there are broken references (a `from` pointing to an asset that doesn't exist, for example), they'll appear in the report before you attempt to execute anything.

### Generate documentation

Converts the pipeline into a human-readable Markdown document. It describes each source, transformation, and sink with its parameters. Useful for keeping documentation in sync with code — if the YAML changes, the doc reflects the change automatically:

```bash
java -jar teckel-etl_2.13.jar -f pipeline.yaml --doc
```

### Visualize the DAG

Generates a graph of the pipeline showing dependencies between assets. The default output is a Mermaid diagram, which you can paste directly into GitHub, Notion, or any tool that supports it:

```bash
java -jar teckel-etl_2.13.jar -f pipeline.yaml --graph
```

## Environment Variables

Values in the YAML can reference environment variables using the `${VARIABLE}` syntax. This lets you keep a generic pipeline and parameterize it at runtime:

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

Pass the variables when launching the JAR:

```bash
INPUT_PATH=data/raw/2024-01.csv \
OUTPUT_PATH=data/processed/2024-01 \
java -jar teckel-etl_2.13.jar -f pipeline.yaml
```

## Embedded REST Server

For cases where you want to expose your pipelines as an HTTP service — integration with orchestrators, data platforms, or just to invoke them from other tools — Teckel includes an embedded server with no additional dependencies:

```bash
java -jar teckel-etl_2.13.jar --server --port 8080
```

All `POST` endpoints expect the pipeline YAML as the request body and return plain text. The server is ready to receive requests immediately after startup.

| Method | Path        | Description                         |
|--------|-------------|-------------------------------------|
| GET    | `/health`   | Health check                        |
| POST   | `/dry-run`  | Returns the execution plan          |
| POST   | `/doc`      | Generates Markdown documentation    |
| POST   | `/graph`    | Generates the DAG in Mermaid format |
| POST   | `/validate` | Validates syntax and references     |
