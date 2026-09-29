#!/usr/bin/env bash
# Préparation du Codespace : Python complet + dépendances Maven pré-téléchargées.

# Python complet (l'image ne contient qu'un Python minimal, sans le module decimal)
python3 -c "import decimal, csv, json" 2>/dev/null || (sudo apt-get update && sudo apt-get install -y python3)

# Dépendances Maven : le premier lancement en séance est immédiat
for pom in lab-bff/banque-api/pom.xml lab-bff/bff-mobile/pom.xml lab-data/api-donnees/pom.xml lab-securite/banque-api/pom.xml; do
  [ -f "$pom" ] && mvn -q -B -f "$pom" dependency:go-offline || true
done

# Dépendances ajoutées par les participants pendant les labs
mvn -q -B dependency:get -Dartifact=org.springframework.boot:spring-boot-starter-oauth2-resource-server:3.3.5 || true
mvn -q -B dependency:get -Dartifact=org.springframework.security:spring-security-test:6.3.4 || true

python3 -c "import decimal; print('Python complet OK')" || echo "⚠ Python incomplet"
echo "Environnement prêt."
