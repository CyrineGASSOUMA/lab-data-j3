# Lab J3 — Brancher une API dans un ETL : ce que fait le connecteur

```
API Référentiel Clients (8090)  ──►  extraire.py (le « connecteur »)  ──►  clients.csv · comptes.csv · operations.csv
      catalogue OpenAPI                 token, pagination, 429,              tables prêtes à charger
      clé d'API · OAuth2                incrémental, aplatissement
```

## Lancer l'API (Codespaces ou local, JDK 21 + Maven)
```
cd lab-data/api-donnees && mvn spring-boot:run
```
Catalogue : port 8090 (Swagger), ou `/v3/api-docs` pour le JSON brut.
Requêtes prêtes à l'emploi : `lab-data/api-donnees/requetes.http`.

## Lancer l'extraction (second terminal)
```
cd lab-data/extraction
python3 extraire.py                 # extraction complète
python3 extraire.py --incremental   # seulement ce qui a changé
python3 extraire.py --operations    # + opérations (curseur)
```
Résultats dans `lab-data/extraction/sortie/`.

## Identifiants de démonstration
| Méthode | Valeur |
|---|---|
| Clé d'API | header `X-API-Key: cle-demo-etl-2026` |
| OAuth2 client credentials | `connecteur-etl` / `secret-etl-2026` (tous les scopes) |
| OAuth2, droits réduits | `rapport-bi` / `secret-bi-2026` (`clients:lire` uniquement) |

## Contenu
| Dossier | Rôle |
|---|---|
| `api-donnees/` | l'API source (fournie, rien à coder) |
| `extraction/extraire.py` | le script d'extraction commenté (bibliothèque standard Python) |
| `livrables/` | grille de lecture, fiche connecteur, mapping à remplir, checklist d'intégration |
| `informatica/` | fichier Swagger 2.0 et étapes pour rejouer l'extraction dans IDMC (bonus) |
| `correction/` | corrigés des livrables et du fichier Swagger |
| `port-public.sh` | rend le port 8090 public (bonus Informatica) |

## Réglages de démonstration
`POST /dev/reglages?requetesParFenetre=5` : limite de débit réduite, pour voir des 429.
`POST /dev/reglages?dureeTokenSecondes=20` : tokens courts, pour voir l'expiration.
`DELETE /dev/reglages` : valeurs par défaut (30 requêtes / 10 s, tokens de 300 s).
