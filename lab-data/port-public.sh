#!/usr/bin/env bash
# Rend le port 8090 public dans Codespaces (nécessaire pour Informatica Cloud) et affiche l'URL.
# Lancer l'API avant.
if [ -z "$CODESPACE_NAME" ]; then
  echo "Hors Codespaces : Informatica Cloud ne peut pas joindre localhost. Utilisez un Codespace."
  exit 0
fi
DOMAINE=${GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN:-app.github.dev}
if gh codespace ports visibility 8090:public -c "$CODESPACE_NAME" > /dev/null 2>&1; then
  echo "✔ Port 8090 public."
else
  echo "✘ Changement automatique impossible : onglet Ports › clic droit sur 8090 › Port Visibility › Public."
fi
echo
echo "URL publique de l'API : https://$CODESPACE_NAME-8090.$DOMAINE"
echo "Valeur de \"host\" dans clients-swagger2.json : $CODESPACE_NAME-8090.$DOMAINE"
