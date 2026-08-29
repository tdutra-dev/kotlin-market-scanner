# Kotlin Market Scanner

A real-time market data scanner built in Kotlin, designed as a hands-on exploration of event-driven and reactive system design — the kind of architecture used in trading and fintech infrastructure.

## What it is

A backend service that ingests streaming market data, processes it through a reactive pipeline, and surfaces signals (price movements, volume spikes, custom alert conditions) with low latency. It's built around an event-driven architecture using Kafka for message streaming and Kotlin coroutines / WebFlux for non-blocking, reactive processing.

## What it does

- Consumes real-time market data from a streaming source (exchange API / market data feed)
- Publishes normalized events to Kafka topics
- Processes event streams reactively to detect configurable conditions (thresholds, moving averages, volume anomalies)
- Exposes results via a reactive API (WebFlux) for downstream consumption or a simple dashboard
- Handles backpressure and failure recovery as first-class concerns, not afterthoughts

## What it solves

Most portfolio backend projects are CRUD apps that don't reflect what trading and fintech companies actually build: systems that process continuous, high-throughput data streams under latency and reliability constraints. This project is a deliberate exercise in that problem space — event-driven ingestion, reactive processing, and idiomatic Kotlin — rather than a synchronous request/response API wrapped around a database.

It also serves as a practical vehicle for learning Kotlin in a production-shaped context (coroutines, WebFlux, Kafka client) instead of through isolated exercises.

## Tech stack

- **Language**: Kotlin
- **Reactive stack**: Spring WebFlux / Kotlin Coroutines
- **Streaming**: Apache Kafka
- **Build**: Gradle

## Status

🚧 Work in progress — architecture and module structure evolving as the project develops.

## Roadmap

- [ ] Market data ingestion module
- [ ] Kafka producer/consumer setup
- [ ] Reactive alert/condition engine
- [ ] API layer (WebFlux)
- [ ] Tests + CI pipeline
- [ ] Deployment setup (Docker)
