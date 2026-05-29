param(
    [string]$VaultAddr = "http://localhost:8200",
    [string]$VaultToken = "root"
)

$required = @(
    "DB_URL",
    "DB_USERNAME",
    "DB_PASSWORD",
    "KEYCLOAK_ISSUER_URI",
    "RABBITMQ_HOST",
    "RABBITMQ_PORT",
    "RABBITMQ_USERNAME",
    "RABBITMQ_PASSWORD"
)

$missing = $required | Where-Object { [string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($_)) }
if ($missing.Count -gt 0) {
    Write-Error "Variables manquantes: $($missing -join ', ')"
    Write-Host ""
    Write-Host "Exemple:"
    Write-Host '$env:DB_URL="jdbc:postgresql://localhost:5432/billing"'
    Write-Host '$env:DB_USERNAME="postgres"'
    Write-Host '$env:DB_PASSWORD="<mot-de-passe-postgres>"'
    Write-Host '$env:KEYCLOAK_ISSUER_URI="http://localhost:8081/realms/billing"'
    Write-Host '$env:RABBITMQ_HOST="localhost"'
    Write-Host '$env:RABBITMQ_PORT="5672"'
    Write-Host '$env:RABBITMQ_USERNAME="guest"'
    Write-Host '$env:RABBITMQ_PASSWORD="guest"'
    exit 1
}

$body = @{
    data = @{
        DB_URL = [Environment]::GetEnvironmentVariable("DB_URL")
        DB_USERNAME = [Environment]::GetEnvironmentVariable("DB_USERNAME")
        DB_PASSWORD = [Environment]::GetEnvironmentVariable("DB_PASSWORD")
        KEYCLOAK_ISSUER_URI = [Environment]::GetEnvironmentVariable("KEYCLOAK_ISSUER_URI")
        RABBITMQ_HOST = [Environment]::GetEnvironmentVariable("RABBITMQ_HOST")
        RABBITMQ_PORT = [Environment]::GetEnvironmentVariable("RABBITMQ_PORT")
        RABBITMQ_USERNAME = [Environment]::GetEnvironmentVariable("RABBITMQ_USERNAME")
        RABBITMQ_PASSWORD = [Environment]::GetEnvironmentVariable("RABBITMQ_PASSWORD")
    }
} | ConvertTo-Json -Depth 4

Invoke-RestMethod `
    -Method Post `
    -Uri "$VaultAddr/v1/secret/data/application" `
    -Headers @{ "X-Vault-Token" = $VaultToken } `
    -ContentType "application/json" `
    -Body $body | Out-Null

Write-Host "Secrets charges dans Vault: secret/application"
