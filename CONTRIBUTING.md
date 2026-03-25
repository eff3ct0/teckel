# Contributing to Teckel

Thank you for your interest in contributing to Teckel! This guide covers the development setup, testing, code style, and release process.

## Development Setup

### Prerequisites

- **JDK 8 or 11** (CI runs against both)
- **sbt** (Scala Build Tool)
- **Scala 2.13.12**

### Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/eff3ct0/teckel.git
   cd teckel
   ```

2. Compile the project:
   ```bash
   sbt compile
   ```

3. Build the CLI uber JAR:
   ```bash
   sbt cli/assembly
   ```

## Project Structure

The project is organized into the following modules:

```
model --> serializer --> api --> cli
  \-------> semantic -->/
```

- **model**: Core data types (`Asset`, `Source`, transformations)
- **serializer**: YAML parsing via Circe + FS2
- **semantic**: ETL execution engine
- **api**: Public library API
- **cli**: Command-line entry point
- **example**: Reference implementations

## Running Tests

Run all tests with coverage:
```bash
sbt clean coverage test coverageReport coverageAggregate
```

Run tests for a specific module:
```bash
sbt "api/test"
sbt "serializer/test"
sbt "semantic/test"
```

Tests run forked and non-parallel. The test framework is ScalaTest with Spark Testing Base. The coverage minimum is currently set at 30%.

## Code Style

This project uses **scalafmt** for code formatting and **sbt-header** for license headers.

Before submitting a pull request, run:
```bash
sbt headerCreateAll scalafmtAll
```

This ensures all files have the MIT license header and are formatted consistently.

## Release Process

Releases are published to **Maven Central** via `sbt-ci-release` and are triggered by version tags.

### How Releases Work

1. The CI pipeline (GitHub Actions) runs tests on both Java 8 and Java 11.
2. Coverage reports are sent to Codecov.
3. When a version tag is pushed (e.g., `v1.0.0`), `sbt ci-release` automatically:
   - Determines the version from the git tag
   - Publishes artifacts to Maven Central via Sonatype
   - Signs artifacts with GPG

### Publishing Settings

The project's publication metadata is configured in `project/SonatypePublish.scala`:
- **Organization**: `com.eff3ct`
- **Homepage**: https://github.com/eff3ct0/teckel
- **License**: MIT
- **SCM**: https://github.com/eff3ct0/teckel

### Creating a Release

1. Ensure all tests pass on the `master` branch.
2. Tag the commit with a version:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
3. The CI pipeline will handle the rest.

### Snapshot Releases

Commits to `master` without a version tag produce SNAPSHOT versions that are not published to Maven Central.

## Submitting Changes

1. Fork the repository and create a feature branch.
2. Make your changes and ensure tests pass.
3. Run `sbt headerCreateAll scalafmtAll` to format code.
4. Open a pull request against the `master` branch.
5. Provide a clear description of the changes and their purpose.

## Reporting Issues

If you find a bug or have a feature request, please create an issue on GitHub. Provide as much detail as possible to help us address it.

## Example YAML Configurations

Reference ETL configurations are available in `docs/etl/` for various transformation types (simple, complex, join, window, etc.). These serve as documentation and can be used for manual testing.
