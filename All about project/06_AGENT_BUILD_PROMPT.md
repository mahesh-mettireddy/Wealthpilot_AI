# Agent Build Prompt — WealthPilot AI
Paste this whole prompt into Antigravity / Kiro as the task. Attach the 5 spec
files alongside it: `01_SRD.md`, `02_FRD.md`, `03_TECHNICAL_ARCHITECTURE.md`,
`04_API_CONTRACT.md`, `05_DATA_MODEL.md`.

---

## PROMPT (copy everything below this line)

You are building **WealthPilot AI**, a digital wealth advisory web application,
end-to-end, from the attached specification documents:

- `01_SRD.md` — requirements, scope, success criteria
- `02_FRD.md` — exact functional logic for every module, including formulas and
  test cases that must pass
- `03_TECHNICAL_ARCHITECTURE.md` — stack, package structure, deployment plan
- `04_API_CONTRACT.md` — exact request/response JSON for every endpoint
- `05_DATA_MODEL.md` — the fund dataset schema and full seed data to use

**Treat these documents as the source of truth.** Do not deviate from the stack,
package structure, formulas, or API contracts defined in them unless you find an
internal contradiction — if you find one, resolve it in favor of `02_FRD.md` for
functional behavior and `03_TECHNICAL_ARCHITECTURE.md` for structure, and note
the resolution in your final summary.

### Your objective

Build, test, and prepare for AWS deployment a fully working version of this
application. You are not done until every item in the Definition of Done
checklist below is true and verified — not assumed. This is a long-running,
multi-step task. Keep working through it autonomously. Only stop and ask the
user a question if you hit a genuine ambiguity that isn't resolved by the spec
documents (e.g., you need an actual AWS account credential or an actual
Anthropic API key value — those you cannot invent, ask for them). Everything
else — code structure, logic, tests, configuration — should be resolved by you
from the specs without asking.

### Build order (work through these phases in sequence; do not skip ahead)

**Phase 1 — Project scaffold**
- Set up the Maven project with Spring Boot 3, Spring Web, Spring AI dependencies
- Create the package structure exactly as defined in `03_TECHNICAL_ARCHITECTURE.md`
- Set up `application.yml` reading `ANTHROPIC_API_KEY` from environment
- Verify: project builds with `mvn clean compile` with zero errors

**Phase 2 — Data layer**
- Create `src/main/resources/data/funds.json` using the full seed dataset in
  `05_DATA_MODEL.md` exactly as given
- Implement `FundDataRepository` to load this at startup into memory
- Write a unit test confirming all funds load and no fund is malformed

**Phase 3 — Module 1: Risk Profiler**
- Implement `RiskProfilerService` exactly per the formula in `02_FRD.md` §1.3
- Implement the `/api/risk-profile` endpoint per `04_API_CONTRACT.md`
- Write unit tests for all three test cases in `02_FRD.md` §1.6 — they must pass
- Write validation for missing/out-of-range fields per the API contract

**Phase 4 — Module 3: Goal Projection Calculator**
- Implement `GoalProjectionService` exactly per the formula in `02_FRD.md` §3.2
- Implement the `/api/project` endpoint
- Write unit tests for both test cases in `02_FRD.md` §3.4, asserting the
  result is within 1% of the expected value
- Do this module before Module 2 since it has no external dependency and lets
  you validate your math independently

**Phase 5 — Module 2: Portfolio Advisor Agent (AI)**
- Implement `AnthropicClient` as a thin wrapper for the LLM call
- Implement `PortfolioAdvisorService`:
  - Build the prompt exactly as described in `02_FRD.md` §2.3, including the
    full fund dataset as context
  - Parse and validate the LLM's JSON response per the schema in §2.4
  - Implement the retry-once-then-fallback logic per §2.3 and the deterministic
    fallback per §2.5
- Implement the `/api/recommend` endpoint
- Write a test that mocks the LLM client to verify: (a) valid responses pass
  through correctly, (b) malformed responses trigger the retry, (c) two
  failures trigger the fallback and the fallback allocation matches §2.5
- Do NOT commit a real API key anywhere. Use an environment variable and
  document it in the README.

**Phase 6 — Frontend**
- Build the single-page dashboard per `02_FRD.md` Module 4: questionnaire form,
  loading state, results view with pie chart (allocation) and line chart
  (growth projection) using Chart.js, rationale text block, and a "start over"
  button
- Serve it as a static resource from the same Spring Boot app (no separate
  frontend server, no CORS needed)
- Wire it to call the three endpoints in sequence as described in FRD §4.3

**Phase 7 — Integration testing**
- Write an end-to-end test (Spring Boot Test + MockMvc or a simple scripted
  HTTP test) that: submits a full questionnaire for each of the three risk
  categories, confirms a valid allocation and rationale come back, confirms the
  projection calculator produces a sane result, and confirms the response
  envelope format matches `04_API_CONTRACT.md` exactly for both success and
  error cases
- Manually exercise the app by running it locally (`mvn spring-boot:run`) and
  walking through the full UI flow yourself; fix anything broken

**Phase 8 — AWS deployment readiness**
- Confirm the app builds a runnable JAR via `mvn clean package`
- Add a `GET /api/health` endpoint returning 200 with `{"status":"UP"}`
- Write the README section covering: how to run locally, how to set
  `ANTHROPIC_API_KEY`, and the exact `eb init` / `eb create` / `eb deploy`
  commands from `03_TECHNICAL_ARCHITECTURE.md` §6
- If you have AWS CLI/EB CLI access in this environment and credentials are
  available, actually run the deployment and confirm the live URL responds. If
  you do not have credentials, stop at this exact point and tell the user
  precisely what command to run and what value they need to supply — do not
  guess a fake URL or claim deployment succeeded when it did not.

### Definition of Done (do not report completion until every line is checked)

- [ ] Project builds with zero errors (`mvn clean package`)
- [ ] All unit tests pass, including every explicit test case listed in
      `02_FRD.md` (§1.6 risk categories, §3.4 projection values)
- [ ] `/api/risk-profile`, `/api/recommend`, `/api/project`, `/api/health` all
      implemented and match `04_API_CONTRACT.md` exactly, including the error
      envelope on failure
- [ ] Portfolio Advisor fallback logic verified to trigger correctly when the
      LLM call fails (test this by temporarily breaking the API key and
      confirming the fallback allocation from FRD §2.5 is returned, then
      restoring the correct key)
- [ ] Full UI flow works end-to-end locally for all three risk categories, with
      no console errors and no visual defects
- [ ] No API key or secret is committed to source control — confirm by
      grepping the repo before final commit
- [ ] README exists with local run instructions and deployment instructions
- [ ] Either the app is actually deployed and reachable on AWS with a verified
      live URL, or the user has been told exactly what credential/step is
      missing to complete that step themselves

### Reporting

At the end, give a concise summary: what was built, what tests exist and their
pass status, any deviation from spec and why, and the exact current state of
AWS deployment (deployed + URL, or blocked + what's needed). Do not declare the
task complete if any Definition of Done item is unchecked — instead, state
clearly which items remain and why.
