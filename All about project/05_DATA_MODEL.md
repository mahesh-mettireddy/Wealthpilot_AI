# Data Model
## WealthPilot AI — Synthetic Fund Dataset

This is a static resource (`src/main/resources/data/funds.json`) loaded into
memory at application startup. No database is used for the MVP.

## Fund schema

```json
{
  "fundId": "EQ-001",
  "name": "string",
  "category": "EQUITY | DEBT | GOLD",
  "subCategory": "string, e.g. Large Cap, Flexi Cap, Short Duration, Gold ETF",
  "historicalAnnualReturnPct": "number, 3-year average",
  "riskRating": "LOW | MODERATE | HIGH",
  "suitableFor": ["CONSERVATIVE", "MODERATE", "AGGRESSIVE"],
  "expenseRatioPct": "number"
}
```

## Seed data (minimum 15 funds — 6 equity, 6 debt, 3 gold)

```json
[
  { "fundId": "EQ-001", "name": "Nifty 50 Index Fund", "category": "EQUITY", "subCategory": "Large Cap Index", "historicalAnnualReturnPct": 13.2, "riskRating": "MODERATE", "suitableFor": ["MODERATE", "AGGRESSIVE"], "expenseRatioPct": 0.2 },
  { "fundId": "EQ-002", "name": "Bluechip Growth Fund", "category": "EQUITY", "subCategory": "Large Cap", "historicalAnnualReturnPct": 12.8, "riskRating": "MODERATE", "suitableFor": ["MODERATE", "AGGRESSIVE"], "expenseRatioPct": 1.1 },
  { "fundId": "EQ-003", "name": "Flexi Cap Growth Fund", "category": "EQUITY", "subCategory": "Flexi Cap", "historicalAnnualReturnPct": 14.5, "riskRating": "HIGH", "suitableFor": ["AGGRESSIVE"], "expenseRatioPct": 1.3 },
  { "fundId": "EQ-004", "name": "Mid Cap Opportunities Fund", "category": "EQUITY", "subCategory": "Mid Cap", "historicalAnnualReturnPct": 16.1, "riskRating": "HIGH", "suitableFor": ["AGGRESSIVE"], "expenseRatioPct": 1.4 },
  { "fundId": "EQ-005", "name": "Small Cap Growth Fund", "category": "EQUITY", "subCategory": "Small Cap", "historicalAnnualReturnPct": 17.8, "riskRating": "HIGH", "suitableFor": ["AGGRESSIVE"], "expenseRatioPct": 1.6 },
  { "fundId": "EQ-006", "name": "Conservative Equity Fund", "category": "EQUITY", "subCategory": "Large Cap Value", "historicalAnnualReturnPct": 10.9, "riskRating": "MODERATE", "suitableFor": ["CONSERVATIVE", "MODERATE"], "expenseRatioPct": 0.9 },

  { "fundId": "DT-001", "name": "Short Duration Debt Fund", "category": "DEBT", "subCategory": "Short Duration", "historicalAnnualReturnPct": 7.1, "riskRating": "LOW", "suitableFor": ["CONSERVATIVE", "MODERATE"], "expenseRatioPct": 0.5 },
  { "fundId": "DT-002", "name": "Corporate Bond Fund", "category": "DEBT", "subCategory": "Corporate Bond", "historicalAnnualReturnPct": 7.6, "riskRating": "LOW", "suitableFor": ["CONSERVATIVE", "MODERATE"], "expenseRatioPct": 0.6 },
  { "fundId": "DT-003", "name": "Liquid Fund", "category": "DEBT", "subCategory": "Liquid", "historicalAnnualReturnPct": 6.4, "riskRating": "LOW", "suitableFor": ["CONSERVATIVE"], "expenseRatioPct": 0.2 },
  { "fundId": "DT-004", "name": "Gilt Fund", "category": "DEBT", "subCategory": "Government Securities", "historicalAnnualReturnPct": 7.3, "riskRating": "LOW", "suitableFor": ["CONSERVATIVE", "MODERATE"], "expenseRatioPct": 0.4 },
  { "fundId": "DT-005", "name": "Dynamic Bond Fund", "category": "DEBT", "subCategory": "Dynamic Bond", "historicalAnnualReturnPct": 7.9, "riskRating": "MODERATE", "suitableFor": ["MODERATE", "AGGRESSIVE"], "expenseRatioPct": 0.8 },
  { "fundId": "DT-006", "name": "Ultra Short Term Fund", "category": "DEBT", "subCategory": "Ultra Short Duration", "historicalAnnualReturnPct": 6.8, "riskRating": "LOW", "suitableFor": ["CONSERVATIVE"], "expenseRatioPct": 0.3 },

  { "fundId": "GD-001", "name": "Gold ETF Fund of Fund", "category": "GOLD", "subCategory": "Gold ETF", "historicalAnnualReturnPct": 8.2, "riskRating": "MODERATE", "suitableFor": ["CONSERVATIVE", "MODERATE", "AGGRESSIVE"], "expenseRatioPct": 0.5 },
  { "fundId": "GD-002", "name": "Sovereign Gold Bond Linked Fund", "category": "GOLD", "subCategory": "Sovereign Gold Bond", "historicalAnnualReturnPct": 8.5, "riskRating": "LOW", "suitableFor": ["CONSERVATIVE", "MODERATE"], "expenseRatioPct": 0.1 },
  { "fundId": "GD-003", "name": "Digital Gold Growth Fund", "category": "GOLD", "subCategory": "Gold Savings", "historicalAnnualReturnPct": 7.9, "riskRating": "MODERATE", "suitableFor": ["MODERATE", "AGGRESSIVE"], "expenseRatioPct": 0.6 }
]
```

## User request models (not persisted — request/response only, no DB tables)

These exist only as in-memory Java records/DTOs mapping to the JSON schemas in
`04_API_CONTRACT.md`. There is no persistence layer in the MVP; nothing about
a user or their session is stored after the response is returned.

## Future data model note (for the roadmap, not MVP)

When integrating with IDBI's live fund/product APIs, the `Fund` schema above
should map directly onto whatever the bank's product catalog API returns,
with `fundId` becoming the bank's internal product identifier. No other
structural change should be needed downstream (service and controller layers
are already decoupled from the data source via `FundDataRepository`).
