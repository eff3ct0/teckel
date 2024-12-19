# Teckel

Teckel is a Domain-Specific Language (DSL) designed to simplify the creation of Apache Spark ETL (Extract, Transform,
Load) processes using YAML configuration files. This tool aims to standardize and streamline ETL workflow creation by
enabling the definition of data transformations in a declarative, user-friendly format without writing extensive code.

This concept is further developed on my
blog: [Big Data with Zero Code](https://blog.rafaelfernandez.dev/posts/big-data-with-zero-code/)

## Features

- **Declarative ETL Configuration:** Define your ETL processes with simple YAML files.
- **Support for Multiple Data Sources:** Easily integrate inputs in CSV, JSON, and Parquet formats.
- **Flexible Transformations:** Perform joins, aggregations, and selections with clear syntax.
- **Spark Compatibility:** Leverage the power of Apache Spark for large-scale data processing.

## Formal Language Definition

Teckel uses a specific set of language constructs to define data flows. Below is the formal syntax for this DSL:

```txt
Asset          := `Asset` <AssetRef> <Source>

Source         := <Input> | <Output> | <Transformation>
Input          := `Input` <Format> <Options> <SourceRef>
Output         := `Output` <AssetRef> <Format> <Options> <SourceRef>

// TODO: It need double-check and define correctly
Transformation ::= JoinOperation | GroupOperation | WindowOperation

// Join
JoinOperation  ::= `Join` <JoinType> <JoinRelation>
JoinType       ::= `Inner` | `Left` | `Right` | `Cross` | ...
JoinRelation   ::= `JoinRelation` <Source> <Source> [ <RelationField> ] 
RelationField  ::= `RelationField` <Column> <Column>

// Group
GroupOperation ::= `Group` <Source> <By> <Agg>
By             ::= `By` [Column]
Agg            ::= `Agg` [Column]

Select         ::= `Select` [Column]
Where          ::= `Where` [Column]

// Type Alias
AssetRef       := String
Format         := String
SourceRef      := String
Options        := `Map` String String
Context<T>     := `Map` <AssetRef> <T>
```

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

- Simple Example: [here](./docs/etl/simple-example.yaml)
- Other Example: [here](./docs/etl/example.yaml)

## Development and Contribution

Contributions to Teckel are welcome. If you'd like to contribute, please fork the repository and create a pull request
with your changes.

## License

Teckel is available under the MIT License. See the [LICENSE](./LICENSE) file for more details.

If you have any questions regarding the license, feel free to contact Rafael Fernandez.

For any issues or questions, feel free to open an issue on the GitHub repository.