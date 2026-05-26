# Billing Invoice Frontend

Frontend Angular + Ng Zorro pour l'application de billing.

L'application consomme les microservices Spring Boot via l'API Gateway et couvre la consultation des factures, creanciers, points de vente, customers et transactions payment.

## Fonctionnalites

- Dashboard d'accueil avec indicateurs et acces rapides.
- Navigation responsive avec sidebar desktop et navigation mobile.
- Gestion des creanciers :
  - recherche multi-criteres ;
  - creation, modification et suppression ;
  - pagination serveur ;
  - tri serveur ;
  - notifications toast sur les actions.
- Gestion des points de vente :
  - recherche multi-criteres ;
  - creation, modification et suppression ;
  - export PDF ;
  - pagination serveur ;
  - tri serveur ;
  - notifications toast.
- Consultation des factures :
  - recherche par reference et status ;
  - affichage des vrais noms customer, creancier et point de vente au lieu des IDs seuls ;
  - page detail facture `/factures/:id` ;
  - suivi simple de l'etat facture/paiement ;
  - pagination serveur ;
  - tri serveur.
- Test paiement :
  - creation d'une facture de test ;
  - selection des vrais customers depuis le microservice customer ;
  - selection creancier et point de vente ;
  - modes de paiement limites a `ESPECES` et `CARTE` ;
  - verification des transactions creees dans billing-payment ;
  - affichage des vrais noms customer, creancier et point de vente dans les transactions ;
  - pagination serveur ;
  - tri serveur.
- UX :
  - tables responsives avec scroll horizontal controle ;
  - colonnes longues mieux gerees : dates, email, ICE, references ;
  - messages d'erreur lisibles ;
  - toasts de succes, erreur et avertissement ;
  - etat des tableaux conserve dans l'URL.

## Etat conserve dans l'URL

Les pages de liste conservent automatiquement la pagination et le tri dans l'URL.

Exemple factures :

```text
/factures?page=2&size=20&sortBy=createdDate&sortOrder=descend
```

Exemple transactions payment :

```text
/test-paiement?paymentPage=2&paymentSize=20&paymentSortBy=amount&paymentSortOrder=descend
```

Cela permet de rafraichir la page ou partager un lien sans perdre la vue courante.

## API Gateway

Le frontend passe par l'API Gateway sur le port `8085`.

Le proxy Angular redirige :

```text
/api          -> http://localhost:8085
/payment-api  -> http://localhost:8085
/customer-api -> http://localhost:8085
```

La configuration est dans :

```text
proxy.conf.json
```

## Vault

Les secrets applicatifs ont ete externalises dans HashiCorp Vault cote backend.

Objectif :

- ne plus stocker les mots de passe et secrets directement dans les fichiers applicatifs ;
- centraliser la configuration sensible ;
- faciliter le changement de secrets sans modifier le code.

Elements concernes cote backend :

- configuration datasource ;
- credentials applicatifs ;
- configuration sensible des microservices.

Le frontend ne lit pas Vault directement. Il continue d'appeler l'API Gateway et les microservices.

## Pre-requis

- Node.js compatible avec la version Angular du projet.
- npm.
- API Gateway lancee sur `http://localhost:8085`.
- Microservices backend demarres :
  - billing-invoice ;
  - billing-payment ;
  - billing-customer ;
  - API Gateway ;
  - RabbitMQ si le test paiement est utilise ;
  - Vault si les secrets backend sont charges depuis Vault.

## Installation

```bash
npm install
```

## Lancement en local

```bash
npm start
```

Application :

```text
http://localhost:4200
```

Le script utilise la configuration Angular `development` avec `proxy.conf.json`.

## Build de production

```bash
npm run build
```

Le budget Angular a ete ajuste pour une application Ng Zorro :

```text
warning: 2mb
error: 2.5mb
```

## Routes principales

```text
/dashboard
/creanciers
/creanciers/new
/creanciers/:id/edit
/points-de-vente
/points-de-vente/new
/points-de-vente/:id/edit
/factures
/factures/:id
/test-paiement
```

## Arborescence utile

```text
src/
  app/
    core/
      models/
      services/
      utils/
    features/
      dashboard/
      creancier/
      invoices/
      payment-test/
      point-de-vente-form/
      points-de-vente-list/
```

## Notes importantes

- Les customers viennent du microservice `billing-customer`, pas d'une table locale payment.
- Les transactions payment viennent du microservice `billing-payment`.
- Les factures, creanciers et points de vente viennent du microservice `billing-invoice`.
- L'affichage des noms dans les factures et transactions est resolu cote frontend a partir des APIs existantes.
