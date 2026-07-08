$ErrorActionPreference = "Stop"

function Test-Recommend($riskCategory) {
    $body = @{
        riskCategory = $riskCategory
        goalType = "RETIREMENT"
        investmentHorizonYears = 10
        monthlyInvestmentAmount = 10000.0
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/recommend" `
                                  -Method Post `
                                  -ContentType "application/json" `
                                  -Body $body
                                  
    Write-Host "==== $riskCategory ===="
    $json = $response | ConvertTo-Json -Depth 5
    Write-Host $json
    Write-Host ""
}

Test-Recommend -riskCategory "CONSERVATIVE"
Test-Recommend -riskCategory "MODERATE"
Test-Recommend -riskCategory "AGGRESSIVE"
