# Mini frontend - Gestion de virements

Ce frontend Angular + Ng Zorro consomme l'API Spring Boot que tu as fournie.

## Fonctionnalites

- Ecran d'initiation de virement
- Ecran de consultation des virements
- Validation ou rejet des virements manuels en attente
- Affichage des comptes disponibles et de leurs soldes
- Proxy Angular vers `http://localhost:8081` pour eviter les problemes CORS

## API attendue

Le frontend utilise les endpoints suivants :

- `GET /api/accounts`
- `POST /api/transfers`
- `GET /api/transfers`
- `PUT /api/transfers/{id}/status`

## Comportement metier pris en compte

- `INSTANT` : le virement est execute tout de suite et passe en `COMPLETED`
- `MANUAL` : le virement est cree en `PENDING`, puis peut etre valide ou rejete depuis l'ecran de consultation

## Pre-requis

- Node.js compatible Angular 20
- npm
- Backend Spring Boot lance sur `http://localhost:8080`

## Installation

```bash
npm install
```

## Lancement en local

```bash
npm start
```

L'application sera disponible sur :

```text
http://localhost:4200
```

Le script `npm start` utilise la configuration Angular `development` qui applique automatiquement le proxy `proxy.conf.json`.

## Build de production

```bash
npm run build
```

## Arborescence utile

```text
src/
  app/
    core/
      models/
      services/
    features/
      initiate-transfer/
      transfers-list/
    shared/
      status-tag/
```

## Remarque importante

Si tu veux appeler le backend directement sans proxy Angular, ajoute un CORS global cote Spring Boot ou un `@CrossOrigin` aussi sur l'endpoint `/api/accounts`.
