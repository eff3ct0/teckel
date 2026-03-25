# CLI

## Build

```bash
sbt cli/assembly
# Output: cli/target/scala-2.13/teckel-etl_2.13.jar
```

## Running a Pipeline

```bash
# From a file
java -jar teckel-etl_2.13.jar -f pipeline.yaml

# From stdin
cat pipeline.yaml | java -jar teckel-etl_2.13.jar -c
```

## Dry Run

Validates references and prints the execution plan. Does not start Spark.

```bash
java -jar teckel-etl_2.13.jar -f pipeline.yaml --dry-run
```

## Documentation Generation

Generates a Markdown summary of inputs, transformations, and outputs.

```bash
java -jar teckel-etl_2.13.jar -f pipeline.yaml --doc
```

## DAG Visualization

Generates a Mermaid diagram of the pipeline graph.

```bash
java -jar teckel-etl_2.13.jar -f pipeline.yaml --graph
```

## Environment Variables

Reference environment variables in YAML using `${VAR}` syntax:

```yaml
input:
  - name: source
    format: csv
    path: '${INPUT_PATH}'
    options:
      header: true
```

```bash
INPUT_PATH=data/input.csv java -jar teckel-etl_2.13.jar -f pipeline.yaml
```

## REST API Server

```bash
java -jar teckel-etl_2.13.jar --server --port 8080
```

| Method | Path        | Description                        |
|--------|-------------|------------------------------------|
| GET    | `/health`   | Health check                       |
| POST   | `/dry-run`  | Execution plan (body: YAML)        |
| POST   | `/doc`      | Markdown documentation (body: YAML)|
| POST   | `/graph`    | DAG visualization (body: YAML)     |
| POST   | `/validate` | Validate pipeline (body: YAML)     |
