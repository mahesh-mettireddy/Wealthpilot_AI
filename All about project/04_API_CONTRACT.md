# API Contract
## WealthPilot AI

All responses use this envelope:
```json
{ "success": true, "data": { }, "error": null }
```
or
```json
{ "success": false, "data": null, "error": "message" }
```

---

## POST /api/risk-profile

### Request
```json
{
  "age": 32,
  "monthlyIncome": 85000,
  "goalType": "RETIREMENT",
  "investmentHorizonYears": 20,
  "monthlyInvestmentAmount": 15000,
  "riskComfort": "MEDIUM",
  "existingInvestmentsPctEquity": 20
}
```

### Response 200
```json
{
  "success": true,
  "data": {
    "riskScore": 68,
    "riskCategory": "MODERATE",
    "factorBreakdown": {
      "ageFactor": 70,
      "horizonFactor": 100,
      "riskComfortFactor": 55,
      "goalFactor": 60
    }
  },
  "error": null
}
```

### Validation errors (400)
- Missing required field → `"error": "Missing required field: <fieldName>"`
- Out-of-range value → `"error": "<fieldName> must be between X and Y"`

---

## POST /api/recommend

### Request
```json
{
  "riskCategory": "MODERATE",
  "goalType": "RETIREMENT",
  "investmentHorizonYears": 20,
  "monthlyInvestmentAmount": 15000
}
```

### Response 200
```json
{
  "success": true,
  "data": {
    "allocation": { "equityPct": 55, "debtPct": 35, "goldPct": 10 },
    "recommendedFunds": [
      { "fundId": "EQ-002", "name": "Nifty 50 Index Fund", "allocationPct": 30 },
      { "fundId": "DT-001", "name": "Short Duration Debt Fund", "allocationPct": 35 },
      { "fundId": "GD-001", "name": "Gold ETF Fund of Fund", "allocationPct": 10 },
      { "fundId": "EQ-005", "name": "Flexi Cap Growth Fund", "allocationPct": 25 }
    ],
    "rationale": "Given your 20-year horizon and moderate comfort with risk, a majority equity allocation lets your money compound over time while the debt allocation cushions short-term volatility..."
  },
  "error": null
}
```

### Failure fallback (still 200, with a fallback flag)
```json
{
  "success": true,
  "data": {
    "allocation": { "equityPct": 55, "debtPct": 35, "goldPct": 10 },
    "recommendedFunds": [ ... ],
    "rationale": "This is a standard model portfolio for your risk profile...",
    "usedFallback": true
  },
  "error": null
}
```

---

## POST /api/project

### Request
```json
{
  "allocation": { "equityPct": 55, "debtPct": 35, "goldPct": 10 },
  "monthlyInvestmentAmount": 15000,
  "investmentHorizonYears": 20
}
```

### Response 200
```json
{
  "success": true,
  "data": {
    "blendedAnnualReturnPct": 9.85,
    "projectedCorpus": 10432876,
    "totalInvested": 3600000,
    "yearlyBreakdown": [
      { "year": 1, "corpus": 188432 },
      { "year": 2, "corpus": 395201 }
    ]
  },
  "error": null
}
```

---

## GET /api/health

### Response 200
```json
{ "success": true, "data": { "status": "UP" }, "error": null }
```
Used by AWS Elastic Beanstalk health checks.
