$apiKey = (Get-Content .env | Where-Object { $_ -match '^ANTHROPIC_API_KEY=(.*)' } | ForEach-Object { $matches[1] })
c:\users\admin\appdata\roaming\python\python311\Scripts\eb.exe create wealthpilot-env --single --envvars ANTHROPIC_API_KEY=$apiKey
