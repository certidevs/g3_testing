# Prepara SonarQube local: contrasena de admin, quality gate "Curso Adecco" por defecto,
# proyecto y token de analisis. Idempotente.
#   Ejecutar desde la raiz del repo:  .\sonar\provision.ps1
$ErrorActionPreference = 'Stop'
$Sonar      = 'http://localhost:9000'
$NEWPASS    = 'Adecco.Sonar.2026'
$ProjectKey = 'g3_testing'   # nombre del proyecto en el SonarQube local

function Auth($p) { @{ Authorization = "Basic " + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:$p")) } }
$headers = Auth 'admin'

function Wait-Sonar {
  Write-Host "Esperando a que SonarQube este UP en $Sonar (puede tardar 1-2 min)..."
  for ($i = 0; $i -lt 90; $i++) {
    try {
      $s = Invoke-RestMethod "$Sonar/api/system/status" -TimeoutSec 5
      if ($s.status -eq 'UP') { Write-Host "SonarQube UP."; return }
      Write-Host "  estado: $($s.status) ..."
    } catch { Write-Host "  arrancando ..." }
    Start-Sleep -Seconds 5
  }
  throw "SonarQube no llego a UP a tiempo. Revisa: docker compose -f compose.sonar.yaml logs sonarqube"
}

function Test-Login($p) {
  try { return [bool](Invoke-RestMethod "$Sonar/api/authentication/validate" -Headers (Auth $p) -TimeoutSec 5).valid }
  catch { return $false }
}

function Sonar-Post($path, $body) {
  try { return Invoke-RestMethod -Method Post -Uri "$Sonar$path" -Headers $headers -Body $body }
  catch { Write-Host "  (aviso) POST $path -> $($_.Exception.Message)"; return $null }
}

Wait-Sonar

# Quitar la contrasena por defecto de admin (evita el aviso "Update your password")
if (Test-Login 'admin') {
  try {
    Invoke-RestMethod -Method Post "$Sonar/api/users/change_password" -Headers (Auth 'admin') `
      -Body @{ login = 'admin'; previousPassword = 'admin'; password = $NEWPASS } -ErrorAction Stop | Out-Null
  } catch { Write-Host "  (aviso) no se pudo cambiar la contrasena: $($_.Exception.Message)" }
  $headers = Auth $NEWPASS
} elseif (Test-Login $NEWPASS) {
  $headers = Auth $NEWPASS
} else {
  throw "No puedo autenticar como admin (ni 'admin/admin' ni 'admin/$NEWPASS')."
}

# New Code global = 90 dias
Sonar-Post '/api/new_code_periods/set' @{ type = 'NUMBER_OF_DAYS'; value = '90' } | Out-Null

# Quality gate "Curso Adecco": solo cobertura y duplicacion, sobre codigo overall
Sonar-Post '/api/qualitygates/create' @{ name = 'Curso Adecco' } | Out-Null
try {
  $cur = Invoke-RestMethod "$Sonar/api/qualitygates/show?name=Curso%20Adecco" -Headers $headers
  foreach ($c in $cur.conditions) { Sonar-Post '/api/qualitygates/delete_condition' @{ id = $c.id } | Out-Null }
} catch { Write-Host "  (aviso) no se pudieron ajustar las condiciones: $($_.Exception.Message)" }
Sonar-Post '/api/qualitygates/create_condition' @{ gateName = 'Curso Adecco'; metric = 'coverage';                 op = 'LT'; error = '50' } | Out-Null
Sonar-Post '/api/qualitygates/create_condition' @{ gateName = 'Curso Adecco'; metric = 'duplicated_lines_density'; op = 'GT'; error = '10' } | Out-Null
Sonar-Post '/api/qualitygates/set_as_default' @{ name = 'Curso Adecco' } | Out-Null

# Proyecto + token de analisis
Sonar-Post '/api/projects/create' @{ project = $ProjectKey; name = $ProjectKey } | Out-Null
Sonar-Post '/api/user_tokens/revoke' @{ name = $ProjectKey } | Out-Null
$tok = Sonar-Post '/api/user_tokens/generate' @{ name = $ProjectKey }
if (-not $tok -or -not $tok.token) { throw "No se pudo generar el token." }
$tok.token | Set-Content -Path 'sonar\token.txt' -NoNewline -Encoding ascii

Write-Host ""
Write-Host "Listo. Login: admin / $NEWPASS  |  proyecto: $ProjectKey  |  token en sonar\token.txt"
Write-Host "Siguiente:  .\sonar\analyze.ps1"
