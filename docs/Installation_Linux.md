# BikePartsFinder - Installation unter Linux

## Voraussetzungen

- Linux (Ubuntu x64)
- `wget` installiert
- Internetzugang

---

## Schritt 1 - Installationsscripte herunterladen

Siehe Script 1_initial_script_downloadBikepartScripts.sh
```bash
wget https://raw.githubusercontent.com/EllenDeveloper/Bikeparts/master/scripts/linux/2_downloadEnvironment
wget https://raw.githubusercontent.com/EllenDeveloper/Bikeparts/master/scripts/linux/3_start.sh
chmod +x 2_downloadEnvironment.sh 3_start.sh
```

---

## Schritt 2 - Umgebung herunterladen

```bash
./2_downloadEnvironment.sh
```

Laedt herunter:
- llama.cpp b8308 (KI-Inferenzserver)
- Qwen 2.5 3B Modell (ca. 1.5 GB)
- bikeparts Binary (neueste Version von GitHub Releases)

Ergebnis: Verzeichnis `bikepartsSoftware/` im aktuellen Verzeichnis.

---

## Schritt 3 - Verzeichnis umbenennen (optional)

```bash
mv bikepartsSoftware bikepartsSoftware_v1.0.1
```

---

## Schritt 4 - .env anlegen

Im Arbeitsverzeichnis eine `.env`-Datei anlegen (wird nicht eingecheckt):
```
JWT_SECRET_KEY=<mindestens-32-zeichen-langer-zufallskey>
SSL_KEYSTORE_PATH=<pfad>/BikePartsFinder.p12
SSL_KEYSTORE_PASSWORD=<passwort>
SSL_KEYSTORE_TYPE=PKCS12
```

---

## Schritt 5 - Anwendung starten

```bash
./3_start.sh
```

Startet:
- Liest `.env` und setzt Umgebungsvariablen
- `llama-server` auf Port 8099
- `bikeparts` mit Spring-Profil `prod`

Beide Prozesse laufen im Hintergrund. Die Anwendung ist danach erreichbar unter:
```
https://<server-ip>:8443
```

---

## Verzeichnisstruktur

```
bikepartsSoftware/
- bikeparts          <- Binary
- llama.cpp-server/  <- llama-server
- ki_models/         <- KI-Modell
- logs/              <- Logfiles
```

---

## Logs

```
bikepartsSoftware/logs/llama-server.log
bikepartsSoftware/logs/bikeparts.log
```

Log live verfolgen:
```bash
tail -f bikepartsSoftware/logs/bikeparts.log
```

---

## Prozesse stoppen

Die PIDs werden beim Start ausgegeben. Stoppen mit:
```bash
kill <PID>
```

Oder alle auf einmal:
```bash
pkill -f bikeparts
pkill -f llama-server
```
