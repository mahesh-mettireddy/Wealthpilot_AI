# Software Requirements Document (SRD)
## WealthPilot AI — Digital Wealth Management Advisor
### IDBI Innovate 2026 — Problem Statement 1: Digital Wealth Management
Team: LeoStar | Team Leader: Mahesh Mettireddy

---

## 1. Purpose

WealthPilot AI is a digital wealth advisory system that profiles a customer's risk
appetite and financial goal, then recommends a personalized investment allocation
using an AI-based advisory agent grounded in fund data, with a plain-language
explanation of the recommendation.

This document defines the complete software requirements for building the system
end-to-end: backend API, AI integration, frontend, data, and deployment to AWS.
It is written to be sufficient on its own for an autonomous coding agent to build
the system without needing further clarification.

## 2. Background and Business Problem

IDBI Bank's wealth advisory today is delivered manually by relationship managers
(RMs), which is expensive and does not scale. As a result, only high-net-worth
clients receive proactive investment guidance, while millions of retail and
mass-affluent customers get none — their savings sit idle in low-yield accounts
instead of flowing into appropriate investment products.

WealthPilot AI digitizes the first layer of wealth advisory — risk profiling and
allocation guidance — so it can be delivered to every retail customer inside the
banking app, at zero marginal cost per customer, with high-value or complex cases
still routed to a human RM.

## 3. Scope

### 3.1 In scope (MVP)
- Guided risk and goal profiling questionnaire
- Rule-based risk scoring engine (Conservative / Moderate / Aggressive)
- AI-based Portfolio Advisor agent that recommends an asset allocation
  (equity / debt / gold percentages) and specific example funds, with a
  plain-language rationale
- Goal-based projection calculator (SIP/lump-sum compound growth)
- Web dashboard displaying: risk profile, recommended allocation (chart),
  rationale text, and projected growth (chart)
- Synthetic fund dataset (15–20 funds) used as grounding context for the AI agent
- Deployment to AWS (Elastic Beanstalk)

### 3.2 Out of scope (MVP)
- Real integration with IDBI's live fund/product APIs (synthetic data only)
- User authentication / login (single-session, no persisted user accounts)
- Multi-turn conversational chat interface (documented as future work)
- Market Intelligence Agent / live news RAG (future work)
- Regional language support (future work)
- Mobile native app (web-responsive only)

## 4. Stakeholders and Users

| Stakeholder | Interest |
|---|---|
| Retail/mass-affluent bank customer (end user) | Wants clear, trustworthy investment guidance without visiting a branch |
| IDBI Bank (product owner, in the hackathon framing) | Wants to scale advisory reach and increase AUM capture from retail deposits |
| Hackathon judges | Evaluate technical execution, explainability, and business viability |

## 5. Functional Requirements Summary

See `02_FRD.md` for full detail. Summary of modules:

1. Risk & Goal Profiler
2. Portfolio Advisor Agent (AI)
3. Goal Projection Calculator
4. Recommendation Dashboard (frontend)
5. Fund Dataset (data layer)

## 6. Non-Functional Requirements

| Category | Requirement |
|---|---|
| Performance | Risk scoring and goal projection respond in < 200ms. AI-based portfolio recommendation responds in < 4s under normal LLM API latency. |
| Availability | Single-instance MVP; no HA requirement for hackathon prototype. Document a path to multi-AZ for production. |
| Security | No PII persisted beyond the active session. API keys (LLM provider) stored as environment variables, never hardcoded or logged. All external calls over HTTPS. |
| Explainability | Every AI-generated recommendation must include a human-readable rationale — no recommendation without an explanation. |
| Portability | Backend must run as a single self-contained Spring Boot JAR, deployable without a database server. |
| Observability | Structured logging for every recommendation request (input profile → output allocation), excluding any PII, for debugging and demo purposes. |
| Cost | Must run on low-cost AWS resources suitable for a prototype/pilot (see `03_TECHNICAL_ARCHITECTURE.md` §6 for estimates). |

## 7. Assumptions

- No real banking core integration is available; all fund/product data is synthetic
  and stored as a static JSON resource bundled with the application.
- The LLM provider is the Anthropic API (Claude), called server-side only —
  no API key is ever exposed to the browser.
- Users interact with the system anonymously for the MVP; no login is required.
- The system is a single deployable unit (no microservices split) for the MVP.

## 8. Constraints

- Must be buildable and deployable within a hackathon timeframe.
- Must use Java 17 + Spring Boot 3 + Spring AI as the backend stack (aligns with
  team's existing expertise and the architecture already committed to in the
  submission deck).
- Must deploy to AWS Elastic Beanstalk (already stated in the submission).
- No paid third-party APIs other than the LLM provider.

## 9. Success Criteria (Definition of Done)

The system is considered complete when:
1. A user can complete the questionnaire and receive a risk profile.
2. The Portfolio Advisor agent returns a valid allocation + rationale for every
   one of the three risk profiles, tested explicitly.
3. The Goal Calculator produces a correct projection for at least 3 test cases
   verified against manual compound-interest calculation.
4. The dashboard renders all of the above with no visual defects.
5. The full flow works end-to-end against a locally running instance.
6. The application is deployed and reachable on a public AWS Elastic Beanstalk URL.
7. Automated tests exist and pass for the risk scoring logic and the goal
   projection math (deterministic, non-AI logic).
8. A README documents how to run locally and how to deploy.
