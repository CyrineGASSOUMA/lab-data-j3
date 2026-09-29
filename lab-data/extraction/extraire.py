#!/usr/bin/env python3
"""
Extraction de l'API Référentiel Clients, comme le ferait un connecteur REST d'ETL.

Ce que fait le script, dans l'ordre :
  1. obtient un token OAuth2 (client credentials) et le renouvelle avant expiration ;
  2. parcourt TOUTES les pages (lien "suivant", ou curseur pour les opérations) ;
  3. respecte la limite de débit : sur 429, attend Retry-After secondes puis réessaie ;
  4. aplatit le JSON imbriqué en tables CSV (clients, comptes, opérations) ;
  5. mémorise la date de modification la plus récente pour la prochaine extraction incrémentale.

Utilisation :
  python3 extraire.py                      extraction complète des clients
  python3 extraire.py --incremental        seulement les clients modifiés depuis la dernière extraction
  python3 extraire.py --depuis 2026-09-20T00:00:00Z
  python3 extraire.py --taille 5           pages de 5 clients (plus d'appels, pratique pour voir un 429)
  python3 extraire.py --operations         extraire aussi les opérations (pagination par curseur)

Aucune bibliothèque à installer : uniquement la bibliothèque standard de Python.
"""
import argparse
import base64
import csv
import decimal
import json
import os
import time
import urllib.error
import urllib.parse
import urllib.request

# ------------------------------------------------------------------ PARAMÈTRES DE LA CONNEXION
URL_API = os.environ.get("URL_API", "http://localhost:8090")
CLIENT_ID = "connecteur-etl"
CLIENT_SECRET = "secret-etl-2026"
DOSSIER_SORTIE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "sortie")
FICHIER_ETAT = os.path.join(DOSSIER_SORTIE, "etat.json")
TENTATIVES_MAX = 5

# ------------------------------------------------------------------ AUTHENTIFICATION
_token = {"valeur": None, "expire_a": 0.0}


def obtenir_token():
    """Flux client credentials : POST /oauth2/token avec les identifiants du connecteur en Basic."""
    identifiants = base64.b64encode(f"{CLIENT_ID}:{CLIENT_SECRET}".encode()).decode()
    requete = urllib.request.Request(
        URL_API + "/oauth2/token",
        data=urllib.parse.urlencode({"grant_type": "client_credentials"}).encode(),
        headers={"Authorization": "Basic " + identifiants,
                 "Content-Type": "application/x-www-form-urlencoded"},
        method="POST")
    with urllib.request.urlopen(requete, timeout=10) as reponse:
        corps = json.load(reponse)
    _token["valeur"] = corps["access_token"]
    _token["expire_a"] = time.time() + corps["expires_in"]
    print(f"  🔑 nouveau token (valable {corps['expires_in']} s, scopes : {corps['scope']})")


def token_valide():
    """Renouvelle le token s'il expire dans moins de 30 secondes."""
    if _token["valeur"] is None or time.time() > _token["expire_a"] - 30:
        obtenir_token()
    return _token["valeur"]


# ------------------------------------------------------------------ APPEL HTTP ROBUSTE
def appeler(chemin):
    """GET avec gestion du 429 (Retry-After), du 401 (token expiré) et des pannes passagères (5xx, réseau)."""
    for tentative in range(1, TENTATIVES_MAX + 1):
        requete = urllib.request.Request(URL_API + chemin,
                                         headers={"Authorization": "Bearer " + token_valide(),
                                                  "Accept": "application/json"})
        try:
            with urllib.request.urlopen(requete, timeout=10) as reponse:
                restant = reponse.headers.get("X-RateLimit-Remaining", "?")
                return json.load(reponse, parse_float=decimal.Decimal), restant   # montants exacts, jamais de float
        except urllib.error.HTTPError as erreur:
            if erreur.code == 429:
                attente = int(erreur.headers.get("Retry-After", "1"))
                print(f"  ⏳ 429 Too Many Requests : attente de {attente} s (Retry-After), puis nouvelle tentative")
                time.sleep(attente)
            elif erreur.code == 401 and tentative == 1:
                print("  🔑 401 : token refusé, on en redemande un")
                _token["valeur"] = None
            elif erreur.code >= 500:
                attente = 2 ** (tentative - 1)
                print(f"  ⚠️  {erreur.code} : panne côté serveur, nouvelle tentative dans {attente} s")
                time.sleep(attente)
            else:
                detail = erreur.read().decode(errors="replace")
                raise SystemExit(f"  ✘ {erreur.code} sur {chemin} : erreur non récupérable\n    {detail}")
        except urllib.error.URLError as erreur:
            attente = 2 ** (tentative - 1)
            print(f"  ⚠️  réseau ({erreur.reason}) : nouvelle tentative dans {attente} s")
            time.sleep(attente)
    raise SystemExit(f"  ✘ abandon après {TENTATIVES_MAX} tentatives sur {chemin}")


# ------------------------------------------------------------------ EXTRACTION
def extraire_clients(taille, depuis):
    """Pagination par numéro de page : on suit liens.suivant jusqu'à ce qu'il soit null."""
    parametres = {"taille": taille}
    if depuis:
        parametres["modifieDepuis"] = depuis
    chemin = "/v1/clients?" + urllib.parse.urlencode(parametres)
    clients = []
    while chemin:
        page, restant = appeler(chemin)
        infos = page["pagination"]
        clients.extend(page["donnees"])
        print(f"  ← page {infos['page'] + 1}/{max(infos['totalPages'], 1)} : {len(page['donnees'])} clients "
              f"(cumul {len(clients)}/{infos['totalElements']}) · quota restant : {restant}")
        chemin = page["liens"]["suivant"]
    return clients


def extraire_operations():
    """Pagination par curseur : on renvoie curseurSuivant tant que aSuite vaut true."""
    operations, curseur = [], None
    while True:
        parametres = {"limite": 100}
        if curseur:
            parametres["curseur"] = curseur
        page, restant = appeler("/v1/operations?" + urllib.parse.urlencode(parametres))
        operations.extend(page["donnees"])
        print(f"  ← {len(page['donnees'])} opérations (cumul {len(operations)}) · quota restant : {restant}")
        if not page["aSuite"]:
            return operations
        curseur = page["curseurSuivant"]


# ------------------------------------------------------------------ TRANSFORMATION : JSON → TABLES
def ecrire_csv(nom_fichier, colonnes, lignes):
    """CSV séparé par ';' et encodé UTF-8 avec BOM : s'ouvre correctement dans Excel en français."""
    chemin = os.path.join(DOSSIER_SORTIE, nom_fichier)
    with open(chemin, "w", newline="", encoding="utf-8-sig") as fichier:
        ecrivain = csv.DictWriter(fichier, fieldnames=colonnes, delimiter=";")
        ecrivain.writeheader()
        ecrivain.writerows(lignes)
    print(f"  💾 {nom_fichier} : {len(lignes)} lignes")


def aplatir(clients):
    """1 client → 1 ligne dans clients.csv (adresse aplatie) + N lignes dans comptes.csv (clé client_id)."""
    lignes_clients, lignes_comptes = [], []
    for c in clients:
        adresse = c.get("adresse") or {}
        lignes_clients.append({
            "client_id": c["id"], "reference": c["reference"], "nom": c["nom"], "prenom": c["prenom"],
            "email": c["email"], "telephone": c["telephone"] or "",        # null JSON → cellule vide
            "statut": c["statut"],
            "adresse_rue": adresse.get("rue", ""), "adresse_code_postal": adresse.get("codePostal", ""),
            "adresse_ville": adresse.get("ville", ""), "adresse_pays": adresse.get("pays", ""),
            "nombre_comptes": len(c["comptes"]), "cree_le": c["creeLe"], "modifie_le": c["modifieLe"]})
        for compte in c["comptes"]:
            lignes_comptes.append({
                "numero": compte["numero"], "client_id": c["id"], "type": compte["type"],
                "solde": compte["solde"], "devise": compte["devise"], "ouvert_le": compte["ouvertLe"]})
    return lignes_clients, lignes_comptes


# ------------------------------------------------------------------ ÉTAT DE L'EXTRACTION INCRÉMENTALE
def lire_derniere_extraction():
    if not os.path.exists(FICHIER_ETAT):
        return None
    with open(FICHIER_ETAT, encoding="utf-8") as fichier:
        return json.load(fichier).get("derniereModification")


def memoriser(clients):
    """On garde la date de modification la plus récente : c'est le point de départ de la prochaine fois."""
    if not clients:
        return
    plus_recente = max(c["modifieLe"] for c in clients)
    with open(FICHIER_ETAT, "w", encoding="utf-8") as fichier:
        json.dump({"derniereModification": plus_recente}, fichier, indent=2)
    print(f"  📌 prochaine extraction incrémentale à partir de {plus_recente}")


# ------------------------------------------------------------------ PROGRAMME PRINCIPAL
def main():
    arguments = argparse.ArgumentParser(description="Extraction de l'API Référentiel Clients")
    arguments.add_argument("--taille", type=int, default=20, help="clients par page (1 à 100)")
    arguments.add_argument("--depuis", help="date ISO-8601 UTC, ex. 2026-09-20T00:00:00Z")
    arguments.add_argument("--incremental", action="store_true", help="reprendre depuis la dernière extraction")
    arguments.add_argument("--operations", action="store_true", help="extraire aussi les opérations")
    options = arguments.parse_args()

    os.makedirs(DOSSIER_SORTIE, exist_ok=True)
    depuis = options.depuis
    if options.incremental:
        depuis = lire_derniere_extraction()
        print(f"Mode incrémental : depuis {depuis or 'le début (aucune extraction précédente)'}")

    print(f"\n1. Extraction des clients ({URL_API})")
    clients = extraire_clients(options.taille, depuis)

    print("\n2. Transformation JSON → tables")
    lignes_clients, lignes_comptes = aplatir(clients)
    ecrire_csv("clients.csv", list(lignes_clients[0].keys()) if lignes_clients else ["client_id"], lignes_clients)
    ecrire_csv("comptes.csv", ["numero", "client_id", "type", "solde", "devise", "ouvert_le"], lignes_comptes)
    memoriser(clients)

    if options.operations:
        print("\n3. Extraction des opérations (curseur)")
        operations = extraire_operations()
        ecrire_csv("operations.csv", ["id", "numeroCompte", "dateOperation", "montant", "sens", "libelle",
                                      "categorie"], [{**o, "categorie": o["categorie"] or ""} for o in operations])

    print("\n✔ Terminé. Fichiers dans", DOSSIER_SORTIE)


if __name__ == "__main__":
    main()
