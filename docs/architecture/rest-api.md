# REST API Design

## Purpose

Provide an HTTP interface for managing and executing Teckel pipelines programmatically, enabling integration with orchestrators, UIs, and monitoring systems.

## Endpoints

### Pipeline Management

```
POST   /api/v1/pipelines              Create a new pipeline from YAML
GET    /api/v1/pipelines              List all registered pipelines
GET    /api/v1/pipelines/:id          Get pipeline details and status
PUT    /api/v1/pipelines/:id          Update pipeline YAML
DELETE /api/v1/pipelines/:id          Delete a pipeline
```

### Execution

```
POST   /api/v1/pipelines/:id/run      Trigger pipeline execution
POST   /api/v1/pipelines/:id/dry-run  Execute dry-run (explain plan)
POST   /api/v1/pipelines/:id/debug    Execute in debug mode (returns intermediate DataFrames as JSON)
GET    /api/v1/pipelines/:id/runs     List execution history
GET    /api/v1/runs/:runId            Get run status and results
DELETE /api/v1/runs/:runId            Cancel a running pipeline
```

### Monitoring

```
GET    /api/v1/health                 Health check
GET    /api/v1/metrics                Prometheus-compatible metrics
GET    /api/v1/pipelines/:id/lineage  Data lineage graph
```

## Data Models

### Pipeline

```json
{
  "id": "uuid",
  "name": "customer-etl",
  "yaml": "input:\n  ...",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z",
  "status": "active",
  "validation": {
    "valid": true,
    "errors": []
  }
}
```

### Run

```json
{
  "id": "uuid",
  "pipelineId": "uuid",
  "status": "running",
  "startedAt": "2024-01-01T00:00:00Z",
  "completedAt": null,
  "backend": "spark",
  "metrics": {
    "rowsRead": 10000,
    "rowsWritten": 9500,
    "duration": null
  }
}
```

## WebSocket for Real-Time Status

```
WS /api/v1/ws/runs/:runId
```

Messages pushed to the client:

```json
{"event": "stage_started",  "stage": "read_customers",  "timestamp": "..."}
{"event": "stage_completed","stage": "read_customers",  "rows": 10000, "duration": "2.3s"}
{"event": "stage_started",  "stage": "transform_filter","timestamp": "..."}
{"event": "run_completed",  "status": "success",        "duration": "15.2s"}
```

## Technology Stack

- HTTP server: http4s (already uses cats-effect)
- JSON: Circe (already a dependency)
- WebSocket: http4s WebSocket support
- Storage: In-memory for MVP, PostgreSQL for production
- Authentication: Bearer token (JWT) for production

## Implementation Plan

1. Add `teckel-server` module depending on `teckel-api` and `http4s`
2. Implement pipeline CRUD with in-memory store
3. Wire `Run[IO]` for execution endpoints
4. Add WebSocket streaming for run status
5. Add Prometheus metrics endpoint
6. Add optional persistence layer
