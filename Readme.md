# BikePartsFinder

KI-gestütztes Recherche-Tool für Fahrrad-Ersatzteile.

Der Nutzer verwaltet seine Fahrräder und Verschleißteile, legt einen Warenkorb an und
startet eine Scraping-Web-Suche nach Preisen in Online-Shops. Die Ergebnisse werden
über eine lokale KI (llama.cpp) bewertet und dem Nutzer gerankt angezeigt.

---

## Tech-Stack

- Spring Boot 3.5, Java 21
- Architektur: Controller -> Service -> Repository
- AOP, Caching, JPA, Thymeleaf, Spring Security
- Web-Scraping (Jsoup)
- Profile: `h2` (Entwicklung), `prod` (Produktion)
- KI: llama.cpp mit Qwen-Modell (lokal, sehr klein und passt in den Hauptspeicher, keine Cloud) - LLM Engineering
- GraalVM Native Image (kein JVM auf dem Server nötig)
- Proxy: Squid (via Docker) - wurde wieder entfernt, da HTTPS-Seiten nicht gecacht
  werden können. Stattdessen wird Caching in der Anwendung selbst verwendet (`@Cacheable`).

## Pflichtenheft

Siehe [Pflichtenheft_BikePartsFinder_v6.md](docs/Pflichtenheft_BikePartsFinder_v6.md) <br>
**Das Projekt wurde als Lernprojekt der IBB als Referenzprojekt innerhalb von einem Monat realisiert. Unter Benutzung von Argentic coding (Claude code).**

---
## Projektstrukturplan

Siehe [Projektstrukturplan_BikePartsFinder_v1.md](docs/Projektstrukturplan_BikePartsFinder_v1.md)

---

## Installation unter Linux

Siehe [Installation_Linux.md](docs/Installation_Linux.md)

---

## Windows - Entwicklung und Build

### Voraussetzungen

- Amazon Corretto 21 (oder anderes JDK 21)
- Maven
- llama.cpp für Windows herunterladen:
  https://github.com/ggml-org/llama.cpp/releases/download/b8308/llama-b8308-bin-win-cpu-x64.zip
- KI-Modell herunterladen 
  - empfohlen: Q4_K_M:
    https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF. (Dateiname: qwen2.5-1.5b-instruct-q4_k_m.gguf)
  - Alternativ ist auch das etwas größere Model möglich (empfohlen: Q3_K_M): https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF (Dateiname: qwen2.5-3b-instruct-q3_k_m.gguf)
 
### Konfiguration

`src/main/resources/application.properties` anpassen:

```properties
llama.server.exe=C:/dev/.../llama-b8308-bin-win-cpu-x64/llama-server.exe
llama.server.model=C:/dev/.../ki_models/qwen2.5-1.5b-instruct-q4_k_m.gguf
```

### llama-server manuell starten (optional)

```cmd
cd C:\dev\...\llama-b8308-bin-win-cpu-x64
llama-server.exe -m "C:\dev\...\ki_models\qwen2.5-1.5b-instruct-q4_k_m.gguf" --port 8099 -c 2048 --threads 4
```

Alternativ startet die Anwendung den llama-server automatisch beim Hochfahren
(`llama.server.lifecycle.auto-start=true` in application.properties).

### Konfiguration der Anwendung für HTTPS mit Aufruf in der Powershell
*Erstellen eines PKCS12 keystore mit keytool*

In jedem Java JDK ist innerhalb des bin Verzeichnisses, das Komandozeilentool keytool zu finden. Mit diesem Tool 
kann ein Keystore im Format PKCS#12 oder JKS angelegt werden. Es kann lesend und schreibend zugegriffen werden.
Ich habe lokal den keystore mit dem Kommando erstellt:
```cmd
keytool -genkeypair -alias BikePartsFinder -keyalg RSA -keysize 4096 -sigalg SHA256withRSA -dname „cn=example.com,ou=example,dc=example,dc=com“ -startdate "2026/06/19 00:00:00" -validity 365 -storetype PKCS12 -storepass  changeit -keystore C:\dev\keytool_Zertifikate_HTTPS_License\BikePartsFinder.p12
```

### Security: HTTPS mit SSL und JWT (JSON Web Token) konfigurieren

Sensible Werte (Keystore-Pfad, Passwörter, JWT-Secret) dürfen nicht eingecheckt werden.
Die Konfiguration unterscheidet sich je nach Umgebung:

**Lokale Entwicklung:**
Kopiere `src/main/resources/application-local.properties.example` nach `application-local.properties`
(Datei ist in `.gitignore`) und trage die echten Werte ein:
```properties
app.jwt.secret=<mindestens-32-zeichen-langer-zufallskey>
server.ssl.key-store=file:<pfad>/BikePartsFinder.p12
server.ssl.key-store-password=<passwort>
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=BikePartsFinder
server.port=8443
server.http2.enabled=true
```
Starten mit: `mvn spring-boot:run "-Dspring-boot.run.profiles=dev,local"`
In der IDE muss ebenfalls das Profil `local` aktiviert sein.

**Produktion:**
In `application-prod.properties` stehen Platzhalter mit Env-Variablen.
Auf dem Server eine `.env`-Datei (in `.gitignore`) anlegen:
```
JWT_SECRET_KEY=<mindestens-32-zeichen-langer-zufallskey>
SSL_KEYSTORE_PATH=<pfad>/BikePartsFinder.p12
SSL_KEYSTORE_PASSWORD=<passwort>
SSL_KEYSTORE_TYPE=PKCS12
```

### Anwendung starten

**Lokale Entwicklung** (H2 + HTTPS + JWT):
```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=dev,local"
```

**Windows Prod-Test** (Prod-Konfiguration + Windows llama-Pfade, liest `.env`):
```bash
scripts/win/start.ps1
```
Info: Das script liesst die environment variablen aus der datei .env und startet automatisch das 
maven kommando:  _mvn spring-boot:run --% -Dspring-boot.run.profiles=dev_

( --% bewirkt, dass von der powershell alles danach an maven weitergegeben wird)

### Anwendung aufrufen
Erreichbar mit HTTPS unter: https://localhost:8443

Browser-Warnung: Da es sich um ein selbstsigniertes Zertifikat handelt, warnt der Browser möglicherweise vor der Verbindung. Dies ist normal. Bestätigen Sie die Ausnahme 
("Erweitert" -> "Weiter zur Seite (nicht sicher)"), um die Anwendung zu nutzen.

### curl-Befehl um z.B. bike mit der Id 1 anzuzeigen
```bash
 curl -X GET https://localhost:8443/api/accounts/bike/1 -u "demo@bikeparts.de:admin123" --insecure
```

Info ohne HTTPS:
Erreichbar unter: http://localhost:8080  

### Natives Binary bauen (GraalVM)

GraalVM 21 ggf. herunterladen und installieren:
https://www.oracle.com/downloads/graalvm-downloads.html

`JAVA_HOME` auf das GraalVM-Verzeichnis setzen.
Visual Studio mit C++ Community Edition installieren. (das zugehörige programm vcvarsall.bat ist dann im Verzeichnis: C:\Program Files\Microsoft Visual Studio\18\Community\VC\Auxiliary\Build)

PowerShell im Projektverzeichnis Bikeparts öffnen und ausführen:

```powershell
"C:\Program Files\Microsoft Visual Studio\18\Community\VC\Auxiliary\Build\vcvarsall.bat" x64
mvn -Pnative package
```

Das Binary liegt danach unter `target/bikeparts.exe`.

**Hinweis Virenscanner:** Norton (und andere) blockieren ggf. den Build-Prozess.
Folgende Pfade als Ausnahme eintragen:

```
C:\Program Files\Java_JDK\graalvm-jdk-21.0.10+8.1\
C:\Users\<USER>\AppData\Local\Temp\SVM-*
C:\<PATH>\Bikeparts\target\
```
