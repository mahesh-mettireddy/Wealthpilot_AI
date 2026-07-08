# Technical Architecture Document
## WealthPilot AI

---

## 1. Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.x, Spring Web, Spring AI |
| AI provider | Anthropic API (Claude), called via Spring AI's Anthropic client or a direct REST call |
| Frontend | Static HTML5 + vanilla JavaScript + Chart.js (served as a Spring Boot static resource — no separate frontend server) |
| Data | Static JSON resource file (no database) |
| Build | Maven |
| Testing | JUnit 5 for unit tests (risk scoring, projection math); a small set of integration tests for the REST endpoints using Spring Boot Test / MockMvc |
| Deployment | AWS Elastic Beanstalk (Java platform), single EC2 instance environment |
| Version control | Git + GitHub (public repo) |

## 2. High-level architecture

```
Browser (single HTML page, Chart.js)
        │  HTTPS
        ▼
Spring Boot application (single JAR)
  ├── Controller layer (REST endpoints)
  ├── Service layer
  │     ├── RiskProfilerService      (pure logic, no external calls)
  │     ├── PortfolioAdvisorService  (calls Anthropic API + fund dataset)
  │     └── GoalProjectionService    (pure logic, no external calls)
  ├── Data layer
  │     └── FundDataRepository (reads static JSON resource at startup, in-memory)
  └── Static resources (index.html, app.js, styles.css)
        │
        ▼
  Anthropic API (external, HTTPS, server-side only)
```

All three services are called from a single controller. There is no service
mesh, no message queue, and no database for the MVP — this is intentional to
keep the system deployable as one artifact within the hackathon timeframe.

## 3. Package structure (Maven, Java)

```
com.leostar.wealthpilot
├── WealthPilotApplication.java
├── controller
│   └── AdvisoryController.java
├── service
│   ├── RiskProfilerService.java
│   ├── PortfolioAdvisorService.java
│   └── GoalProjectionService.java
├── client
│   └── AnthropicClient.java          (thin wrapper around the LLM HTTP call)
├── model
│   ├── RiskProfileRequest.java
│   ├── RiskProfileResponse.java
│   ├── RecommendationRequest.java
│   ├── RecommendationResponse.java
│   ├── ProjectionRequest.java
│   ├── ProjectionResponse.java
│   └── Fund.java
├── repository
│   └── FundDataRepository.java
├── config
│   └── AppConfig.java                 (reads ANTHROPIC_API_KEY from env)
└── exception
    └── GlobalExceptionHandler.java    (@ControllerAdvice, returns the standard error envelope)

src/main/resources
├── application.yml
├── static
│   ├── index.html
│   ├── app.js
│   └── styles.css
└── data
    └── funds.json
```

## 4. API endpoints (implementation detail — full contract in `05_API_CONTRACT.md`)

| Method | Path | Module |
|---|---|---|
| POST | /api/risk-profile | Module 1 |
| POST | /api/recommend | Module 2 |
| POST | /api/project | Module 3 |
| GET | /api/health | Health check for AWS EB |

## 5. Configuration and secrets

- `ANTHROPIC_API_KEY` must be read from an environment variable, never
  committed to source control or hardcoded.
- `application.yml` should reference it as `${ANTHROPIC_API_KEY}`.
- On AWS Elastic Beanstalk, set this via Environment Properties in the EB
  console or via `eb setenv ANTHROPIC_API_KEY=...`.
- `.gitignore` must exclude any local `.env` file used for local development.

## 6. Deployment plan (AWS Elastic Beanstalk)

1. Build the deployable artifact: `mvn clean package` → produces
   `target/wealthpilot-0.0.1-SNAPSHOT.jar`.
2. Initialize EB in the project root: `eb init -p corretto-17 wealthpilot-ai`
   (or the current Java 17 Elastic Beanstalk platform name — verify via
   `eb platform list` at build time, platform naming may have changed).
3. Create the environment: `eb create wealthpilot-env`.
4. Set the API key: `eb setenv ANTHROPIC_API_KEY=<key>`.
5. Deploy: `eb deploy`.
6. Verify: `eb open` and confirm `/api/health` returns 200, then run the full
   questionnaire flow against the live URL.

### Estimated cost (see also SRD §6 for NFR)
- EC2 t3.small: ~$15–20/month
- No RDS, no S3, no load balancer required for single-instance MVP
- LLM API usage: ~$0.01–0.03 per recommendation call, scales with traffic

### Rollback / fallback
- Keep the app runnable locally via `mvn spring-boot:run` at all times as a
  fallback demo path if the live AWS deployment has issues during judging.

## 7. Non-functional implementation notes

- Log every `/api/recommend` request/response pair (input risk profile +
  output allocation) at INFO level, with no PII, for demo/debugging purposes.
- The Anthropic API call must have a timeout (recommend 8 seconds) and the two
  retry attempts described in FRD §2.3 must both respect this timeout budget.
- CORS: since the frontend is served from the same Spring Boot app, no CORS
  configuration should be necessary. If a separate frontend host is used
  during development, enable CORS only for `localhost` origins in dev profile.
