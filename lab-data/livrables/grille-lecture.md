# Grille de lecture d'un catalogue d'API

API étudiée : ______________________  Source du catalogue : Swagger UI · /v3/api-docs · PDF · portail

| # | Question | Où regarder dans le catalogue | Votre réponse |
|---|---|---|---|
| 1 | Quelle est l'URL de base ? Y a-t-il une version dans l'URL ? | `servers`, début des chemins | |
| 2 | Quelles méthodes d'authentification sont proposées ? | bouton Authorize, `components.securitySchemes` | |
| 3 | Pour OAuth2 : quel flux, quelle URL de token, quels scopes ? | `flows`, `tokenUrl`, `scopes` | |
| 4 | Quels endpoints permettent de **lire** les données dont j'ai besoin ? | `paths`, verbes GET | |
| 5 | Quels paramètres sont obligatoires ? Lesquels ont une valeur par défaut, une borne ? | `parameters` : `required`, `default`, description | |
| 6 | Comment la pagination fonctionne-t-elle ? (page, curseur, lien suivant, header Link) | paramètres + schéma de réponse | |
| 7 | Peut-on extraire seulement ce qui a changé ? Avec quel champ ? | paramètre de date, champ `modifieLe` / `updatedAt` | |
| 8 | Quelle est la structure de la réponse ? Objets imbriqués ? Tableaux ? | `components.schemas` | |
| 9 | Quels champs peuvent être **null** ? Quels champs sont des listes de valeurs fixes (enum) ? | `nullable`, `enum` | |
| 10 | Formats : dates, montants, devises, pays ? | `format`, `example`, description | |
| 11 | Quelles erreurs sont documentées, et sous quel format ? | `responses` : 4xx, 5xx | |
| 12 | Y a-t-il une limite de débit ? Comment est-elle signalée ? | description générale, réponse 429, headers | |
