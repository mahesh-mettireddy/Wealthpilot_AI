$env:ANTHROPIC_API_KEY = (Get-Content .env | Where-Object { $_ -match '^ANTHROPIC_API_KEY=(.*)' } | ForEach-Object { $matches[1] })
.\mvnw.cmd spring-boot:run
