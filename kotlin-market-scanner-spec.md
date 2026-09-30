# Project Spec: kotlin-market-scanner

## Purpose
A reactive market-data scanner built in Kotlin to learn idiomatic Kotlin, Spring WebFlux, reactive streaming, and Reactor Kafka through a working, incrementally-committed project. This project is part of a portfolio built to support applications to trading/fintech companies (event-driven & reactive systems focus).

## Global rules (apply to every phase below)

1. **One commit per phase.** After generating the code for a phase, propose a `git commit` with a concise commit message in the format:
   `feat(phaseN): <what was added>` followed by a 2-3 line body explaining what the commit does and why. Do not squash phases together.
2. **Comment the code to explain intent, not syntax.** Every non-trivial function, coroutine/reactive operator chain, and error-handling block needs a short comment explaining *what it's doing and why* — not a restatement of the Kotlin keyword. Comments should help a Kotlin beginner (me) understand reactive/coroutine flow, not just what a line does.
3. **Code must run and be testable at the end of every phase.** Don't leave a phase in a broken or partially-wired state. If a piece is stubbed, say so explicitly in comments and in the commit message.
4. **Containerize wherever practical.** Provide a `Dockerfile` for the app starting from the phase it becomes runnable as a service (Phase 2), and a `docker-compose.yml` once external dependencies appear (Postgres in Phase 4, Kafka in Phase 5). Local/native dependencies (IntelliJ, Gradle wrapper) stay outside Docker.
5. **Explain before generating.** Before writing code for a phase, give a short (5-8 line) explanation of the Kotlin/reactive concepts involved in that phase, in plain language, before the code block.
6. **Stack:** Kotlin, Gradle (Kotlin DSL), Spring Boot WebFlux, Reactor (`Mono`/`Flux`), R2DBC, Reactor Kafka, JUnit5 + `StepVerifier`.

---

## Phase 1 — Setup + idiomatic Kotlin basics
**Goal:** Working Gradle Kotlin DSL project skeleton, plus a standalone script that fetches a price from a public REST API (Binance or CoinGecko) first synchronously, then converted to use coroutines (`suspend fun`, `launch`, `async`).

**Deliverables:**
- Gradle project skeleton (Kotlin DSL, JDK version pinned)
- A `data class PriceTick(...)` modeling a price point, demonstrating null-safety choices explicitly (explain in comments why a field is nullable or not)
- `FetchPriceSync.kt`: blocking HTTP call to a public price API
- `FetchPriceCoroutine.kt`: same call rewritten using `suspend fun` + `runBlocking`/`launch`/`async`, with a comment explaining the practical difference vs. the sync version
- Basic unit test for the data class / parsing logic

**Commit:** `feat(phase1): project setup and sync-to-coroutine price fetch example`

---

## Phase 2 — First reactive endpoint with WebFlux
**Goal:** A real (not tutorial-copied) Spring Boot WebFlux service exposing a streaming endpoint.

**Deliverables:**
- Spring Boot WebFlux application skeleton
- `WebClient` wrapper to call the external price API reactively, returning `Mono`/`Flux`
- One controller endpoint exposing `Flux<PriceTick>` as Server-Sent Events (SSE)
- Comments explaining backpressure implications at the points where they matter (e.g., why `Flux` vs `Mono`, what happens if the client is slow)
- `Dockerfile` for the app (multi-stage build: Gradle build stage + slim JRE runtime stage)
- Test hitting the SSE endpoint and asserting at least one emitted value

**Commit:** `feat(phase2): reactive SSE endpoint with WebClient + WebFlux`

---

## Phase 3 — Continuous streaming ingestion
**Goal:** Continuous ingestion from a real market feed (Binance WebSocket) transformed into an internal reactive stream, with reactive error handling.

**Deliverables:**
- WebSocket client (reactive) connecting to a live Binance market stream
- Transformation pipeline using `filter`, `map`, and `window`/`buffer` to compute an aggregate (e.g., average price every N seconds)
- Reactive error handling: `onErrorResume` and `retry` with backoff, with comments explaining what failure each handler is protecting against
- Update SSE endpoint (or add a new one) to expose the aggregated stream
- Test simulating an error/disconnect and asserting the retry/backoff behavior (can use a fake/mock source instead of live Binance for test determinism — explain this tradeoff in a comment)

**Commit:** `feat(phase3): continuous WS ingestion with reactive aggregation and error handling`

---

## Phase 4 — Reactive persistence + query
**Goal:** Persist ticks reactively and expose a paginated query endpoint.

**Deliverables:**
- R2DBC setup (Postgres) — a `docker-compose.yml` entry for Postgres
- Repository storing incoming `PriceTick`s reactively (non-blocking end to end)
- A comment block explicitly flagging where a blocking call (e.g., accidental JDBC usage) *would* break the reactive chain, even if not intentionally introduced — this is meant to make the "blocking call ruins the chain" lesson visible in the code itself
- Query endpoint returning a paginated `Flux<PriceTick>`
- `docker-compose.yml` wiring app + Postgres together
- Tests using `StepVerifier` against the reactive repository (embedded/test Postgres or Testcontainers if feasible)

**Commit:** `feat(phase4): R2DBC persistence and paginated query endpoint`

---

## Phase 5 — Reactive Kafka (optional but recommended)
**Goal:** Replace or complement direct streaming with Reactor Kafka (`KafkaReceiver`/`KafkaSender`), not the imperative `@KafkaListener`.

**Deliverables:**
- Local Kafka + Zookeeper (or KRaft) in `docker-compose.yml`
- A producer publishing ticks to a topic using `KafkaSender`
- A consumer reading from that topic using `KafkaReceiver`, feeding into the existing reactive pipeline
- Comments explaining how this differs from `@KafkaListener` and why that distinction matters
- Test verifying a message published is received end-to-end (can use an embedded/test Kafka broker)

**Commit:** `feat(phase5): reactive Kafka producer/consumer via Reactor Kafka`

---

## Phase 6 — Testing, refinement, and documentation
**Goal:** Full reactive test coverage and a README suitable for a portfolio/interview context.

**Deliverables:**
- `StepVerifier`-based tests covering the key reactive chains built in earlier phases (not just Phase 4/5)
- `README.md` containing:
  - Architecture overview (diagram in text/ASCII or Mermaid is fine)
  - Key technical decisions and why they were made
  - What would be done differently in production (e.g., schema evolution, observability, scaling Kafka partitions, retry/backoff tuning)
- A short "explain this project in 2-3 minutes" talking-point outline in the README, aimed at an interview setting

**Commit:** `feat(phase6): full StepVerifier test coverage and README documentation`

---

## Working agreement with Copilot for this session
- Generate one phase at a time. Wait for confirmation before moving to the next phase.
- If something in the roadmap is ambiguous, ask before generating rather than guessing silently.
- Prefer idiomatic Kotlin over Java-translated-to-Kotlin patterns, and point out when a more idiomatic alternative exists.
