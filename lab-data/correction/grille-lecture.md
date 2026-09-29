# Grille de lecture — corrigé (API Référentiel Clients)

| # | Réponse |
|---|---|
| 1 | URL de base : celle du serveur (port 8090, ou l'URL publique Codespaces). Version dans le chemin : **/v1**. |
| 2 | Deux méthodes, au choix : clé d'API (header `X-API-Key`) ou OAuth2. |
| 3 | OAuth2 **client credentials**, token sur `POST /oauth2/token`, scopes `clients:lire` et `operations:lire`. |
| 4 | `GET /v1/clients`, `GET /v1/clients/{id}`, `GET /v1/operations`. |
| 5 | Aucun paramètre obligatoire. `page` (défaut 0, ≥ 0), `taille` (défaut 20, 1 à 100), `limite` (défaut 100, 1 à 200). |
| 6 | Clients : par numéro de page, suivre `liens.suivant` (ou le header `Link rel="next"`) jusqu'à null. Opérations : par curseur, renvoyer `curseurSuivant` tant que `aSuite` vaut true. |
| 7 | Oui : paramètre `modifieDepuis` (ISO-8601 UTC), basé sur le champ `modifieLe`. |
| 8 | `PageClients` → `donnees` (tableau de `Client`), `pagination`, `liens`. `Client` contient un objet `adresse` et un tableau `comptes`. |
| 9 | Null : `telephone` (client), `categorie` (opération), `liens.suivant` / `precedent`. Enums : `statut` (ACTIF, INACTIF, PROSPECT), `type` de compte (COURANT, EPARGNE, TITRES). |
| 10 | Dates-heures en ISO-8601 UTC (`2026-09-20T08:00:00Z`), dates en `AAAA-MM-JJ`, montants à 2 décimales, devise ISO 4217, pays ISO 3166 alpha-2. |
| 11 | 400, 401, 403, 404, 429, au format Problem Details (RFC 9457). Le token renvoie des erreurs au format OAuth2 (`error`, `error_description`). |
| 12 | 30 requêtes par fenêtre de 10 s et par client. 429 + `Retry-After`. Headers `X-RateLimit-Limit` et `X-RateLimit-Remaining`. |
