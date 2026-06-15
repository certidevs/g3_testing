# SonarQube en local (cobertura + quality gate)

Levanta un SonarQube Community en tu máquina para ver la cobertura del proyecto y el
quality gate, en `http://localhost:9000`.

## Requisitos
- Docker Desktop arrancado (~3-4 GB de RAM libres).
- Chrome instalado (los tests Selenium corren headless).
- Puerto 9000 libre.

## Uso (Windows / PowerShell) — desde la raíz del repo
```powershell
docker compose -f compose.sonar.yaml up -d   # 1) levanta SonarQube (la 1ª vez baja imágenes)
.\sonar\provision.ps1                         # 2) prepara gate + proyecto + token (una vez)
.\sonar\analyze.ps1                           # 3) corre tests + cobertura y sube el análisis
```
Login en http://localhost:9000 → **admin / Adecco.Sonar.2026**
Panel del proyecto: http://localhost:9000/dashboard?id=g3_testing

## Linux / macOS / WSL
```bash
docker compose -f compose.sonar.yaml up -d
./sonar/provision.sh
./sonar/analyze.sh
```

## Parar
```powershell
docker compose -f compose.sonar.yaml down      # conserva datos
docker compose -f compose.sonar.yaml down -v   # borra datos
```

## Problemas frecuentes
- **SonarQube no arranca:** suele ser `vm.max_map_count` (lo exige Elasticsearch). En
  Docker Desktop: `wsl -d docker-desktop sysctl -w vm.max_map_count=262144` y repite `up -d`.
- **Puerto 9000 ocupado:** cambia `"9000:9000"` por `"9001:9000"` en `compose.sonar.yaml`
  y usa `http://localhost:9001` en el navegador y en `analyze`.
