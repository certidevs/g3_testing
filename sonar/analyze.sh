#!/usr/bin/env bash
# Ejecuta tests + cobertura (JaCoCo) y sube el analisis al SonarQube local.
# Ejecutar desde la raiz del repo (tras provision.sh): ./sonar/analyze.sh
set -euo pipefail
PROJECT_KEY='g3_testing'
[ -f sonar/token.txt ] || { echo "Falta sonar/token.txt. Ejecuta antes: ./sonar/provision.sh"; exit 1; }
TOKEN=$(cat sonar/token.txt)

./mvnw -B clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dmaven.test.failure.ignore=true \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token="$TOKEN" \
  -Dsonar.projectKey="$PROJECT_KEY" \
  -Dsonar.projectName="$PROJECT_KEY"

echo ""
echo "Panel:  http://localhost:9000/dashboard?id=$PROJECT_KEY"
