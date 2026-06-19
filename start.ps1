# 1. .env Datei einlesen und Umgebungsvariablen setzen
if (Test-Path .env) {
    Get-Content .env | ForEach-Object {
        # Ignoriere leere Zeilen und Kommentare
        if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            # Setze die Variable für den aktuellen Prozess (wird an Kind-Prozesse vererbt)
            Set-Item -Path "env:$key" -Value $value
            Write-Host "Gesetzt: $key"
        }
    }
} else {
    Write-Warning ".env Datei nicht gefunden!"
}

# 2. Maven starten
# WICHTIG: Nutzen Sie '--%' auch hier im Skript, um PowerShell-Parsing zu verhindern
Write-Host "Starte Maven mit Profil h2..."
mvn spring-boot:run --% -Dspring-boot.run.profiles=h2