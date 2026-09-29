# Mapping JSON → tables — corrigé

| Chemin JSON | Table.colonne | Type SQL | Règle / remarque |
|---|---|---|---|
| `id` | CLIENT.client_id | INTEGER (PK) | |
| `reference` | CLIENT.reference | VARCHAR(20) | unique |
| `nom`, `prenom` | CLIENT.nom, CLIENT.prenom | VARCHAR(100) | apostrophes, accents : UTF-8 |
| `telephone` | CLIENT.telephone | VARCHAR(20) NULL | null → NULL |
| `statut` | CLIENT.statut | VARCHAR(10) | enum : contrôle de valeur |
| `adresse.rue` | CLIENT.adresse_rue | VARCHAR(200) | objet aplati avec un préfixe |
| `adresse.codePostal` | CLIENT.adresse_code_postal | **VARCHAR(10)** | jamais numérique |
| `adresse.ville`, `adresse.pays` | CLIENT.adresse_ville, CLIENT.adresse_pays | VARCHAR(100), CHAR(2) | |
| `comptes[]` | table fille COMPTE | — | 1 ligne par compte, 57 clients → 114 comptes |
| `comptes[].numero` | COMPTE.numero | VARCHAR(20) (PK) | + COMPTE.client_id (FK) |
| `comptes[].solde` | COMPTE.solde | DECIMAL(15,2) | jamais FLOAT pour de l'argent |
| `comptes[].ouvertLe` | COMPTE.ouvert_le | DATE | |
| `creeLe`, `modifieLe` | CLIENT.cree_le, CLIENT.modifie_le | TIMESTAMP (UTC) | le « Z » signifie UTC |
