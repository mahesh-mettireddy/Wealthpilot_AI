# Functional Requirements Document (FRD)
## WealthPilot AI

---

## Module 1: Risk & Goal Profiler

### 1.1 Description
Collects a small set of inputs from the user and computes a deterministic risk
category. This module contains no AI — it is pure rule-based logic so it is fast,
testable, and predictable.

### 1.2 Inputs (from user, via questionnaire)

| Field | Type | Allowed values | Required |
|---|---|---|---|
| age | integer | 18–75 | yes |
| monthly_income | number (INR) | > 0 | yes |
| goal_type | enum | RETIREMENT, HOME_PURCHASE, CHILD_EDUCATION, WEALTH_GROWTH, EMERGENCY_FUND | yes |
| investment_horizon_years | integer | 1–40 | yes |
| monthly_investment_amount | number (INR) | > 0 | yes |
| risk_comfort | enum | LOW, MEDIUM, HIGH | yes (self-declared comfort with market fluctuation) |
| existing_investments_pct_equity | integer | 0–100 | optional, default 0 |

### 1.3 Risk Scoring Logic (deterministic, must be implemented exactly as specified)

Compute a numeric score from 0–100 using this weighted formula:

```
score = (age_factor * 0.25)
      + (horizon_factor * 0.35)
      + (risk_comfort_factor * 0.30)
      + (goal_factor * 0.10)
```

Where:
- `age_factor`: age ≤ 30 → 100; 31–45 → 70; 46–60 → 40; > 60 → 15
- `horizon_factor`: horizon ≥ 15 years → 100; 7–14 → 70; 3–6 → 40; ≤ 2 → 10
- `risk_comfort_factor`: HIGH → 100; MEDIUM → 55; LOW → 15
- `goal_factor`: WEALTH_GROWTH → 100; CHILD_EDUCATION → 60; RETIREMENT → 60;
  HOME_PURCHASE → 40; EMERGENCY_FUND → 10

### 1.4 Risk Category Mapping

| Score range | Risk category |
|---|---|
| 0–35 | CONSERVATIVE |
| 36–70 | MODERATE |
| 71–100 | AGGRESSIVE |

### 1.5 Output

```json
{
  "riskScore": 62,
  "riskCategory": "MODERATE",
  "factorBreakdown": {
    "ageFactor": 70,
    "horizonFactor": 70,
    "riskComfortFactor": 55,
    "goalFactor": 60
  }
}
```

### 1.6 Test Cases (must pass)

| Case | age | horizon | risk_comfort | goal | Expected category |
|---|---|---|---|---|---|
| A | 25 | 20 | HIGH | WEALTH_GROWTH | AGGRESSIVE |
| B | 55 | 3 | LOW | EMERGENCY_FUND | CONSERVATIVE |
| C | 35 | 10 | MEDIUM | RETIREMENT | MODERATE |

---

## Module 2: Portfolio Advisor Agent (AI)

### 2.1 Description
Given a risk category and goal, this module calls the LLM (Anthropic API) with the
synthetic fund dataset provided as context, and returns a recommended allocation
plus a plain-language rationale. This is the only module that makes an external
AI call.

### 2.2 Inputs

```json
{
  "riskCategory": "MODERATE",
  "goalType": "RETIREMENT",
  "investmentHorizonYears": 10,
  "monthlyInvestmentAmount": 15000
}
```

### 2.3 Processing

1. Load the full synthetic fund dataset (see `04_DATA_MODEL.md`).
2. Filter or annotate funds by risk category suitability (dataset includes a
   `suitableFor` field per fund listing which risk categories it fits).
3. Construct a prompt to the LLM containing:
   - The user's risk category, goal, horizon, and monthly amount
   - The full list of candidate funds with their category, historical return,
     and risk rating
   - An explicit instruction to return ONLY valid JSON matching the output
     schema below (no markdown, no prose outside the JSON)
4. Parse the LLM's JSON response. If parsing fails, retry once with a stricter
   "return only JSON" instruction. If it fails twice, fall back to a
   deterministic rule-based allocation (see §2.5) so the system never fails to
   respond.

### 2.4 Output Schema (must be enforced by validation after LLM call)

```json
{
  "allocation": {
    "equityPct": 55,
    "debtPct": 35,
    "goldPct": 10
  },
  "recommendedFunds": [
    { "fundId": "EQ-002", "name": "...", "allocationPct": 30 },
    { "fundId": "DT-001", "name": "...", "allocationPct": 35 },
    { "fundId": "GD-001", "name": "...", "allocationPct": 10 },
    { "fundId": "EQ-005", "name": "...", "allocationPct": 25 }
  ],
  "rationale": "3-5 sentence plain-language explanation, no jargon without explanation"
}
```

Validation rules the backend must enforce regardless of what the LLM returns:
- `equityPct + debtPct + goldPct` must equal 100 (reject and retry if not)
- Every `fundId` in `recommendedFunds` must exist in the dataset
- `rationale` must be non-empty

### 2.5 Fallback Logic (rule-based, used only if AI call fails twice)

| Risk category | Equity % | Debt % | Gold % |
|---|---|---|---|
| CONSERVATIVE | 20 | 70 | 10 |
| MODERATE | 55 | 35 | 10 |
| AGGRESSIVE | 80 | 15 | 5 |

Pick the top 2 funds per asset class from the dataset (by historical return) and
split allocation evenly within each class. Rationale text in fallback mode should
state the allocation follows a standard model portfolio for the profile.

---

## Module 3: Goal Projection Calculator

### 3.1 Description
Pure computation, no AI, no external calls. Projects the future value of a
monthly SIP (Systematic Investment Plan) given an assumed annual return derived
from the recommended allocation.

### 3.2 Formula

Assumed blended annual return = weighted average of asset class expected returns
using the allocation percentages from Module 2, where:
- Equity expected annual return = 12%
- Debt expected annual return = 7%
- Gold expected annual return = 8%

Future value of SIP (monthly compounding):

```
FV = P * (((1 + r)^n - 1) / r) * (1 + r)

where:
P = monthly investment amount
r = blended annual return / 12
n = investment_horizon_years * 12
```

### 3.3 Output

```json
{
  "blendedAnnualReturnPct": 9.85,
  "projectedCorpus": 2894531,
  "totalInvested": 1800000,
  "yearlyBreakdown": [
    { "year": 1, "corpus": 188432 },
    { "year": 2, "corpus": 395201 }
  ]
}
```

`yearlyBreakdown` must contain one entry per year up to `investment_horizon_years`,
computed with the same formula at `n = year * 12`.

### 3.4 Test Cases (must pass, verified against manual calculation)

| P (monthly) | r (annual) | years | Expected FV (approx, ±1%) |
|---|---|---|---|
| 10000 | 10% | 10 | ₹20.5 lakh |
| 5000 | 8% | 5 | ₹3.7 lakh |

---

## Module 4: Recommendation Dashboard (Frontend)

### 4.1 Description
Single-page web app. No routing/multi-page needed for MVP.

### 4.2 Screens (single page, sequential sections)

1. **Questionnaire form** — all Module 1 inputs, client-side validation on
   required fields and ranges before submit.
2. **Results view** (shown after submit, same page):
   - Risk category badge (Conservative / Moderate / Aggressive)
   - Allocation pie chart (equity/debt/gold)
   - List of recommended funds with allocation %
   - Rationale text block
   - Growth projection line chart (yearlyBreakdown from Module 3)
   - "Start over" button that resets the form

### 4.3 API calls from frontend
1. `POST /api/risk-profile` → Module 1
2. `POST /api/recommend` → Module 2 (send riskCategory + goal inputs)
3. `POST /api/project` → Module 3 (send allocation + monthly amount + horizon)

Frontend should call these sequentially after form submit and show a loading
state during the `/api/recommend` call (this is the only call with meaningful
latency due to the LLM).

### 4.4 Error handling
- If any API call fails, show a clear inline error message and allow retry
  without losing the user's form inputs.

---

## Module 5: Fund Dataset

See `04_DATA_MODEL.md` for the full schema and seed data. This is a static JSON
file bundled as an application resource — no database required for MVP.

---

## Cross-cutting Functional Requirements

- All three API endpoints must be stateless (no server-side session) — every
  request carries all data it needs.
- All responses must be JSON with a consistent envelope:
  ```json
  { "success": true, "data": { ... }, "error": null }
  ```
  or on failure:
  ```json
  { "success": false, "data": null, "error": "human-readable message" }
  ```
