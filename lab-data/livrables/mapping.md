# Mapping JSON → tables

Réponse de `GET /v1/clients/{id}` : un client, un objet `adresse` imbriqué, un tableau `comptes`.
Complétez la colonne « Table.colonne » et « Type SQL », puis comparez avec les CSV produits par le script.

| Chemin JSON | Table.colonne | Type SQL | Règle / remarque |
|---|---|---|---|
| `id` | | | |
| `reference` | | | |
| `nom`, `prenom` | | | |
| `telephone` | | | peut être null |
| `statut` | | | enum : ACTIF, INACTIF, PROSPECT |
| `adresse.rue` | | | objet imbriqué |
| `adresse.codePostal` | | | attention au type ! |
| `adresse.ville`, `adresse.pays` | | | |
| `comptes[]` | | | tableau → combien de lignes ? |
| `comptes[].numero` | | | |
| `comptes[].solde` | | | |
| `comptes[].ouvertLe` | | | |
| `creeLe`, `modifieLe` | | | fuseau ? |
