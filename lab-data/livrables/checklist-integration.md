# Checklist d'intégration d'une API (à réutiliser en mission)

## Avant de commencer
- [ ] J'ai le catalogue (OpenAPI / Swagger) et je sais qui contacter côté fournisseur.
- [ ] J'ai des identifiants de **test** distincts de la production.
- [ ] Je connais la limite de débit et les plages de maintenance.

## Connexion
- [ ] Méthode d'authentification choisie : clé d'API, Basic ou OAuth2 client credentials.
- [ ] Secrets stockés dans un coffre ou dans la connexion de l'outil, jamais en clair dans un job.
- [ ] Renouvellement automatique du token avant son expiration.

## Extraction
- [ ] Pagination suivie jusqu'au bout (lien suivant, curseur), condition d'arrêt claire.
- [ ] Taille de page adaptée : assez grande pour limiter les appels, pas au-delà du maximum.
- [ ] Extraction incrémentale : champ de date, dernière valeur mémorisée, borne incluse (≥).
- [ ] 429 : attendre `Retry-After`. 5xx et timeout : réessayer avec délai croissant. 4xx : ne pas réessayer, alerter.

## Chargement
- [ ] Objets imbriqués aplatis, tableaux transformés en tables filles avec clé étrangère.
- [ ] Types respectés : codes postaux en texte, montants en décimal, dates en UTC.
- [ ] Valeurs null gérées explicitement.
- [ ] Chargement idempotent : upsert sur la clé, une relance ne crée pas de doublons.

## Exploitation
- [ ] Journal : nombre d'appels, de lignes, de 429, durée.
- [ ] Alerte en cas d'échec définitif.
