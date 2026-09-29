# Bonus — Rejouer l'extraction dans Informatica Cloud (IDMC)

Facultatif : nécessite un compte d'essai Informatica Intelligent Cloud Services (Cloud Data Integration).
Les écrans varient selon l'édition et la version : repérez les éléments ci-dessous plutôt que des libellés exacts.

## 1. Rendre l'API joignable depuis le cloud
IDMC ne voit pas `localhost` : l'API doit avoir une URL publique.
```
bash lab-data/port-public.sh
```
Notez l'URL et la valeur de `host` affichées.

## 2. Préparer le fichier Swagger 2.0
Le connecteur REST V2 d'IDMC s'appuie sur un fichier Swagger **2.0** (pas OpenAPI 3).
1. Ouvrez `lab-data/informatica/clients-swagger2.json`.
2. Remplacez `VOTRE-CODESPACE-8090.app.github.dev` par la valeur de `host` (sans `https://`).
3. Vérifiez-le sur https://editor.swagger.io (collez le contenu : aucune erreur).

## 3. Créer la connexion (Administrator › Connections › New Connection)
| Champ | Valeur |
|---|---|
| Type | REST V2 |
| Runtime environment | l'agent disponible (Secure Agent ou agent hébergé) |
| Authentification | **API Key** si proposée : header `X-API-Key` = `cle-demo-etl-2026`. Sinon **OAuth 2.0 Client Credentials** : Access Token URL = `https://<URL publique>/oauth2/token`, Client ID = `connecteur-etl`, Client Secret = `secret-etl-2026`. À défaut, ajouter le header `X-API-Key` dans les en-têtes de la connexion. |
| Swagger File Path | le fichier de l'étape 2. Selon l'édition : import dans *Administrator › Swagger Files*, ou chemin sur la machine du Secure Agent. |

Cliquez **Test Connection**.

## 4. Créer le mapping (Data Integration › New › Mapping)
1. **Source** : la connexion REST V2, opération `listerClients`, paramètre `taille` = 100 (57 clients : une seule page suffit).
2. Observez les champs proposés : le connecteur aplatit la hiérarchie (`adresse`, `comptes`), comme le script.
3. **Target** : fichier plat (flat file) ou table.
4. Lancez le mapping, puis comparez le nombre de lignes avec `clients.csv` produit par le script (57).

## 5. Ce qu'il faut retrouver dans l'outil
| Notion du lab | Où dans IDMC |
|---|---|
| URL de base, chemins, paramètres | le fichier Swagger chargé dans la connexion |
| Clé d'API / OAuth2 client credentials | les champs d'authentification de la connexion |
| Pagination | paramètres de pagination de l'opération ou boucle dans le mapping |
| Aplatissement JSON → colonnes | les champs de sortie de la source REST V2 |
| 429, retry | options de relance du connecteur / de la tâche |
