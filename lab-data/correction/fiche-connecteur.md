# Fiche connecteur — corrigé

**Source :** API Référentiel Clients v1  **Objectif :** alimenter l'entrepôt clients et comptes  **Fréquence :** quotidienne

## 1. Connexion
| Élément | Valeur |
|---|---|
| URL de base | `https://<codespace>-8090.app.github.dev` (formation) |
| Authentification | OAuth2 client credentials (préférable à la clé d'API : droits limités par scope, token court) |
| URL du token | `/oauth2/token`, client authentifié en Basic |
| Identifiants | `connecteur-etl` / secret stocké dans la connexion de l'outil |
| Scopes | `clients:lire` (+ `operations:lire` si les opérations sont chargées) |
| Durée de vie du token | 300 s : le connecteur doit le renouveler |

## 2. Endpoints
| Donnée | Verbe + chemin | Paramètres | Pagination |
|---|---|---|---|
| Clients et comptes | `GET /v1/clients` | `taille=100`, `modifieDepuis` | page, lien `suivant` |
| Opérations | `GET /v1/operations` | `limite=200` | curseur |

## 3. Extraction
| Élément | Valeur |
|---|---|
| Taille de page | 100 (maximum autorisé) : 1 appel pour 57 clients |
| Arrêt | `liens.suivant` null / `aSuite` false |
| Incrémental | `modifieDepuis` = plus grande valeur de `modifieLe` du dernier chargement, borne incluse |
| 429 | attendre `Retry-After`, puis reprendre la même page |
| 401 | redemander un token, rejouer une fois |
| 5xx / timeout | 3 à 5 tentatives, délai croissant, puis alerte |

## 4. Tables cibles
| Table | Clé | Lignes (extraction complète) | Chargement |
|---|---|---|---|
| CLIENT | client_id | 57 | upsert |
| COMPTE | numero (FK client_id) | 114 | upsert |
| OPERATION | id | 342 | upsert |

## 5. Points d'attention
- Code postal en texte (codes belges, zéros en tête possibles).
- Téléphone et catégorie peuvent être null.
- Borne incluse en incrémental : le dernier client est relu, l'upsert évite le doublon.
