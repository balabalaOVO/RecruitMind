# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
mvn clean compile          # compile only
mvn clean package -DskipTests   # full jar
mvn spring-boot:run             # run directly (requires MySQL)
mvn test                                   # unit tests only
mvn test -Dtest=RecruitmentAgentServiceTest   # single test class
```

## Architecture Overview

This is a Spring Boot 3.2 monolith that wraps an AI analysis pipeline behind a REST API, with a Vue 3 SPA served as a static resource.

**Request flow**: `Controller → Service → DashScope ChatModel → JSON extraction → DTO validation → JPA persistence`

**Key design decisions**:
- DTOs use Java `record` types (immutable, no setters). `AnalyzeRequest.Material` is a nested record inside `AnalyzeRequest`.
- The AI model response is **unreliable JSON** — it often includes markdown fences or extra text. The `extractJson()` method in `RecruitmentAgentServiceImpl` uses a brace-counting state machine (not regex) to find the first complete `{...}` object, handling nested structures and escaped characters inside strings.
- Prompt templates live in `src/main/resources/prompts/` as `.txt` files with `{placeholder}` variables, loaded by `PromptLoader` from the classpath.
- The `AIConfig.java` `ObjectMapper` bean is the single Jackson instance used everywhere — it has `JavaTimeModule` registered for `LocalDateTime` serialization and `FAIL_ON_UNKNOWN_PROPERTIES=false`.
- CORS is configured as a `CorsFilter` bean (filter level), not via `WebMvcConfigurer.addCorsMappings()`. This is intentional — filter-level CORS runs before Spring MVC `consumes` constraints, so OPTIONS preflight requests don't get rejected.

**Persistence**: Spring Data JPA with `ddl-auto: update` auto-creates tables. The `AnalysisSession` entity has `@OneToMany` to `AnalysisResult`. Repositories use `@EntityGraph(attributePaths = "results")` to avoid lazy-loading issues during JSON serialization. Analysis results are persisted **after** the API response is assembled (fire-and-forget within a try/catch, so DB failures never break the API response).

**Frontend**: Single `index.html` in `static/`, Vue 3 via CDN. No build step. The page auto-detects whether it's served from the backend (port 8080) or an IDE preview (port 63342) and shows an API base URL config bar accordingly. History view fetches from `/api/v1/agent/history` and parses stored JSON strings for tags/risks/questions.

## Dual-name conflict

`AnalysisResult` exists as **both** a DTO (`dto.response.AnalysisResult`, used in the API response) and a JPA entity (`entity.AnalysisResult`, stored in DB). When editing `RecruitmentAgentServiceImpl`, fully-qualify the entity as `com.example.recruitmentagent.entity.AnalysisResult` — the DTO import is already there.

## API endpoints summary

| Method | Path | Controller |
|--------|------|------------|
| POST | `/api/v1/agent/analyze` | `RecruitmentAgentController` (JSON) |
| POST | `/api/v1/agent/analyze/text` | `RecruitmentAgentController` (text/plain) |
| POST | `/api/v1/agent/analyze/upload` | `RecruitmentAgentController` (multipart) |
| GET | `/api/v1/agent/sample-run` | `SampleDataController` |
| GET | `/api/v1/agent/history` | `HistoryController` |
| GET | `/api/v1/agent/history/{id}` | `HistoryController` |

## Configuration

- `application.yml`: DashScope API key + model, MySQL datasource, JPA settings, sample-runner toggle (`app.sample-runner.enabled`)
- `SampleDataRunner` is gated behind `@ConditionalOnProperty(name = "app.sample-runner.enabled", havingValue = "true")` — disabled by default
- Default job description is hardcoded in `RecruitmentAgentServiceImpl.DEFAULT_JOB_DESCRIPTION` (used when no `jobDescription` is provided in the request)
