# Teckel

[![Release](https://github.com/rafafrdz/teckel/actions/workflows/release.yml/badge.svg?branch=master)](https://github.com/rafafrdz/teckel/actions/workflows/release.yml)

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

## Getting Started

### Prerequisites

- **Apache Spark**: Ensure you have Apache Spark installed and properly configured.
- **YAML files**: Create configuration files specifying your data sources and transformations.

### Installation

To use Teckel, you can clone the repository and integrate it into your Spark setup:

```bash
git clone https://github.com/rafafrdz/teckel.git
cd teckel
```

**TODO: Add instructions for building the project and integrating it into your Spark setup.**

### Usage

Once you have installed Teckel, you can use it to run ETL processes.

**TODO: Add instructions for running ETL processes using Teckel.**

## ETL Yaml Example Specification

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


## Development and Contribution

Contributions to Teckel are welcome. If you'd like to contribute, please fork the repository and create a pull request
with your changes.

## License

Teckel is available under the MIT License. See the [LICENSE](./LICENSE) file for more details.

If you have any questions regarding the license, feel free to contact Rafael Fernandez.

For any issues or questions, feel free to open an issue on the GitHub repository.
