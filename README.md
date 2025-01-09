# Teckel

[![Release](https://github.com/rafafrdz/teckel/actions/workflows/release.yml/badge.svg?branch=master)](https://github.com/rafafrdz/teckel/actions/workflows/release.yml) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Teckel is a framework designed to simplify the creation of Apache Spark ETL (Extract, Transform,
Load) processes using YAML configuration files. This tool aims to standardize and streamline ETL workflow creation by
enabling the definition of data transformations in a declarative, user-friendly format without writing extensive code.

![Logo](./docs/images/teckel-banner.png)

This concept is further developed on my
blog: [Big Data with Zero Code](https://blog.rafaelfernandez.dev/posts/big-data-with-zero-code/)

## Features

- **Declarative ETL Configuration:** Define your ETL processes with simple YAML files.
- **Support for Multiple Data Sources:** Easily integrate inputs in CSV, JSON, and Parquet formats.
- **Flexible Transformations:** Perform joins, aggregations, and selections with clear syntax.
- **Spark Compatibility:** Leverage the power of Apache Spark for large-scale data processing.

## ETL Yaml Example

Here's an example of a fully defined ETL configuration using a YAML file:

### SQL ETL

- Simple Example: [here](./docs/etl/simple.yaml)
- Complex Example: [here](./docs/etl/complex.yaml)
- Other Example: [here](./docs/etl/example.yaml)

### SQL Transformations

- `Select` Example: [here](./docs/etl/select.yaml)
- `Where` Example: [here](./docs/etl/where.yaml)
- `Group By` Example: [here](./docs/etl/group-by.yaml)
- `Order By` Example: [here](./docs/etl/order-by.yaml)

## Getting Started

### Prerequisites

- **Apache Spark**: Ensure you have Apache Spark installed and properly configured.
- **YAML files**: Create configuration files specifying your data sources and transformations.

#### Deployment on Docker or Kubernetes

In case of you don't have Apache Spark installed previously, you can deploy an Apache Spark cluster using the following
docker image [
`eff3ct/spark:latest`](https://hub.docker.com/r/eff3ct/spark) available in
the [eff3ct0/spark-docker](https://github.com/eff3ct0/spark-docker) GitHub repository.

### Installation

Clone the Teckel repository and integrate it with your existing Spark setup:

```bash
git clone https://github.com/rafafrdz/teckel.git
cd teckel
```

#### Building the Teckel ETL Uber JAR

Build the Teckel ETL CLI into an Uber JAR using the following command:

```bash
sbt cli/assembly
```

The resulting JAR, `teckel-etl_2.13.jar`, will be located in the `cli/target/scala-2.13/` directory.

### Usage in Apache Spark Ecosystem with the CLI

Once the `teckel-etl_2.13.jar`is ready, use it to execute ETL processes on Apache Spark with the following arguments:

- `-f` or `--file`: The path to the ETL file.
- `-c` or `--console`: Run the ETL in the console.

#### Example: Running ETL in Apache Spark using STDIN

<details><summary>Demo - Teckel and Apache Spark by STDIN</summary>

[![Teckel and Apache Spark by Yaml File](https://res.cloudinary.com/marcomontalbano/image/upload/v1735905159/video_to_markdown/images/youtube--eJwJIbNAtto-c05b58ac6eb4c4700831b2b3070cd403.jpg)](https://www.youtube.com/watch?v=oxNjnxIdbig "Teckel and Apache Spark by STDIN")

</details>

To run the ETL in the **console**, you can use the following command:

```bash
cat << EOF | /opt/spark/bin/spark-submit --class com.eff3ct.teckel.app.Main teckel-etl_2.13.jar -c
input:
  - name: table1
    format: csv
    path: '/path/to/data/file.csv'
    options:
      header: true
      sep: '|'


output:
  - name: table1
    format: parquet
    mode: overwrite
    path: '/path/to/output/'
EOF
```

#### Example: Running ETL in Apache Spark using a file

<details><summary>Demo - Teckel and Apache Spark by Yaml File</summary>

[![Teckel and Apache Spark by Yaml File](https://res.cloudinary.com/marcomontalbano/image/upload/v1735905159/video_to_markdown/images/youtube--eJwJIbNAtto-c05b58ac6eb4c4700831b2b3070cd403.jpg)](https://www.youtube.com/watch?v=eJwJIbNAtto "Teckel and Apache Spark by Yaml File")

</details>

To run the ETL from a **file**, you can use the following command:

```bash
/opt/spark/bin/spark-submit --class com.eff3ct.teckel.app.Main teckel-etl_2.13.jar -f /path/to/etl/file.yaml
```

> [!IMPORTANT]
>
> **Teckel CLI as dependency / Teckel ETL as framework.**
>
> The Teckel CLI is a standalone application that can be used as a dependency in your project. Notice that the uber jar
> name is `teckel-etl` and not `teckel-cli` or `teckel-cli-assembly`. This is because
> we want to distinguish between the Teckel CLI dependency and the ETL framework.
>
> Check the [Integration with Apache Spark](./docs/integration-apache-spark.md) documentation for more information.

## Integration with Apache Spark

Teckel can be integrated with Apache Spark easily just adding either the Teckel CLI or Teckel Api as a
dependency in your project or using it as a framework in your Apache Spark project.

Check the [Integration with Apache Spark](./docs/integration-apache-spark.md) documentation for more information.

## Development and Contribution

Contributions to Teckel are welcome. If you'd like to contribute, please fork the repository and create a pull request
with your changes.

## License

Teckel is available under the MIT License. See the [LICENSE](./LICENSE) file for more details.

If you have any questions regarding the license, feel free to contact Rafael Fernandez.

For any issues or questions, feel free to open an issue on the GitHub repository.
