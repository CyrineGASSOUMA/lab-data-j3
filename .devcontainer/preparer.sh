#!/usr/bin/env bash
# Télécharge les dépendances Maven à la création du Codespace : le premier lancement en séance est immédiat.
for pom in lab-bff/banque-api/pom.xml lab-bff/bff-mobile/pom.xml lab-data/api-donnees/pom.xml lab-securite/banque-api/pom.xml; do
  [ -f "$pom" ] && mvn -q -B -f "$pom" dependency:go-offline || true
done
# Dépendances ajoutées par les participants pendant les labs
mvn -q -B dependency:get -Dartifact=org.springframework.boot:spring-boot-starter-oauth2-resource-server:3.3.5 || true
mvn -q -B dependency:get -Dartifact=org.springframework.security:spring-security-test:6.3.4 || true
python3 --version || echo "Python absent"
echo "Environnement prêt."
