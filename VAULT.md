# Vault local

Les microservices lisent maintenant les secrets depuis HashiCorp Vault via Spring Cloud Vault.

## Demarrer Vault

```powershell
docker compose up -d vault
```

Vault dev est expose sur `http://localhost:8200` avec le token dev `root`.

## Charger les secrets

Definir les variables localement, puis executer le script:

```powershell
$env:DB_URL="jdbc:postgresql://host:5432/db"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="mot-de-passe"
$env:KEYCLOAK_ISSUER_URI="http://localhost:8081/realms/billing"
$env:RABBITMQ_HOST="localhost"
$env:RABBITMQ_PORT="5672"
$env:RABBITMQ_USERNAME="guest"
$env:RABBITMQ_PASSWORD="guest"

.\scripts\vault-put-secrets.ps1
```

Les valeurs sont stockees dans `secret/application`, lues par `billing-customer`, `billing-invoice` et `billing-payment`.

## Demarrer les services

```powershell
cd billing-customer
.\mvnw.cmd spring-boot:run

cd ..\billing-payment
.\mvnw.cmd spring-boot:run

cd ..\billing-invoice
.\mvnw.cmd spring-boot:run
```

Pour utiliser un Vault different:

```powershell
$env:VAULT_URI="http://localhost:8200"
$env:VAULT_TOKEN="root"
```
