# Ejecuta tests + cobertura (JaCoCo) y sube el analisis al SonarQube local.
#   Ejecutar desde la raiz del repo (tras provision.ps1):  .\sonar\analyze.ps1
$ErrorActionPreference = 'Stop'
$ProjectKey = 'g3_testing'
if (-not (Test-Path 'sonar\token.txt')) { throw "Falta sonar\token.txt. Ejecuta antes:  .\sonar\provision.ps1" }
$token = (Get-Content 'sonar\token.txt' -Raw).Trim()

.\mvnw.cmd -B clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar `
  "-Dmaven.test.failure.ignore=true" `
  "-Dsonar.host.url=http://localhost:9000" `
  "-Dsonar.token=$token" `
  "-Dsonar.projectKey=$ProjectKey" `
  "-Dsonar.projectName=$ProjectKey"

Write-Host ""
Write-Host "Panel:  http://localhost:9000/dashboard?id=$ProjectKey"
