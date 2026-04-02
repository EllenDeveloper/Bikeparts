# BikePartsFinder

**Pflichtenheft**

| | |
|---|---|
| **Projekt:** | BikePartsFinder - KI-gestütztes Recherche-Tool für Fahrrad-Ersatzteile |
| **Version:** | 1.0 |
| **Datum:** | 12.03.2026 |
| **Auftraggeber:** | - |
| **Auftragnehmer:** | - |

---

# Historie

| Nr. | Datum | Version | Geänderte Kapitel | Beschreibung der Änderung | Autor | Zustand |
|-----|-------|---------|-------------------|--------------------------|-------|---------|
| 1 | 03.03.2026 | 1.0 | Alle | Initiale Erstellung | - | In Bearbeitung |
| 4 | 03.03.2026 | 1.0 | Produktinformationen | -> nicht mit KI aktualisiert!<br> Technische Anforderungen Software  | - | /F25/ Account bearbeiten + Security hinzugefügt |
| 6 | 16.03.2026 | 1.0 | 10.3.2 | Amazon PA-API deprecated (30.04.2026), keine Neuregistrierung möglich; Creators API erfordert Affiliate-Verkaufsnachweis → nicht im Scope; eBay Finding API durch Browse API ersetzt | - | Amazon-API-Status aktualisiert |


---

# Inhaltsverzeichnis

1. [Zielbestimmung](#1-zielbestimmung)
2. [Produkteinsatz](#2-produkteinsatz)
3. [Produktübersicht](#3-produktübersicht)
4. [Produktfunktionen](#4-produktfunktionen)
5. [Produktdaten](#5-produktdaten)
6. [Produktleistungen](#6-produktleistungen)
7. [Qualitätsanforderungen](#7-qualitätsanforderungen)
8. [Benutzungsoberfläche](#8-benutzungsoberfläche)
9. [Nichtfunktionale Anforderungen](#9-nichtfunktionale-anforderungen)
10. [Technische Produktumgebung](#10-technische-produktumgebung)
11. [Spezielle Anforderungen an die Entwicklungsumgebung](#11-spezielle-anforderungen-an-die-entwicklungsumgebung)
12. [Gliederung in Teilprodukte](#12-gliederung-in-teilprodukte)
13. [Testszenarien](#13-testszenarien)
14. [Ergänzungen](#14-ergänzungen)
15. [Glossar](#15-glossar)

---

# 1. Zielbestimmung

BikePartsFinder ist eine KI-gestützte Webanwendung (monolithische Spring Boot Architektur), mit der Fahrradbegeisterte benötigte Ersatzteile komfortabel recherchieren und die Preise vergleichen können. Der Nutzer verwaltet seine Fahrräder und deren Verschleißteile, stellt einen Warenkorb zusammen und lässt anschließend per KI-gestützter Hybridsuche die günstigsten Angebote aus verschiedenen Online-Shops ermitteln.

## 1.1 Musskriterien
**Konzentration auf die wesentlichen Technischen Bestandteile!**

Deshalb wird der User seine Fahrradteile und Fahrräder nicht selbst eintragen können. Es erfolgt stattdessen der Import der Daten per SQL-Script.
- Minimale UI nur für die Demo. Benutzer können lediglich die Daten anzeigen. (Kein CRUD)
- Benutzer können Fahrradteile in einen Warenkorb legen
- Das System sucht automatisch in mehreren Online-Quellen nach Preisen für alle Artikel im Warenkorb
- Ollama (KI) optimiert die Suchanfragen und bewertet die gefundenen Angebote
- Pro Artikel werden bis zu 5 günstigste Angebote (Shop, Preis, URL) tabellarisch ausgegeben, sortiert nach Preis aufsteigend
- Authentifizierung und Autorisierung mit den Rollen USER und ADMIN
- Logging über Aspektorientierte Programmierung (AOP)
- ExceptionHandling über Aspektorientierte Programmierung (AOP)

## 1.2 Wunschkriterien
- Benutzer können eigene Fahrräder mit kompatiblen Ersatzteilen anlegen, bearbeiten und löschen = CRUD
- Warenkorb-Optimierung: Ollama analysiert alle Artikel gemeinsam und empfiehlt Shops, bei denen mehrere Artikel gebündelt bestellt werden können (Versandkosten sparen)
- Priorisierung nach Benutzerkriterien: günstigster Gesamtpreis, Versandkosten, Zahlungsart (EU), Lieferzeit
- Benutzer kann Suchergebnisse unter einem Namen und Datum speichern
- Benutzer kann Favoriten-Shops und Blacklist-Shops pflegen
- Admin-Dashboard mit Statistiken
- E-Mail-Versand des Warenkorbs an den Benutzer (JavaMailSender)
- Idealo-API Integration (falls Studenten-Zugang verfügbar)
- Mehrsprachigkeit (i18n: DE/EN)
- Actuator-Monitoring
- Admin kann Benutzer sperren und aktivieren sowie verifizierte Partner-Shops verwalten

## 1.3 Abgrenzungskriterien

- Kein eigener Online-Shop - die Anwendung vermittelt nur zu externen Shops
- Kein direkter Kaufabschluss innerhalb der Anwendung
- Keine mobile App - reine Web-Applikation (SSR mit Thymeleaf)
- Keine Echtzeitpreisüberwachung oder automatischer Preisalarm (Phase 1)

---

# 2. Produkteinsatz

BikePartsFinder unterstützt Fahrradbesitzer dabei, günstige Ersatzteile im Internet zu finden. Die Anwendung aggregiert Preise aus verschiedenen Quellen und bewertet diese mithilfe von KI, sodass der Benutzer den günstigsten Gesamtanbieter schnell identifizieren kann.

## 2.1 Anwendungsbereiche

Privater Anwendungsbereich: Wartung und Reparatur von Fahrrädern durch Privatpersonen. Die Anwendung eignet sich insbesondere für Personen, die mehrere Fahrräder besitzen und regelmäßig Verschleißteile nachbestellen.

## 2.2 Zielgruppen

Benutzer der Anwendung gliedern sich in:

- **Registrierte Benutzer (USER):** Verwalten eigene Fahrräder und Teile, erstellen Warenkörbe, starten Preisrecherchen
- **Administratoren (ADMIN):** Verwalten Benutzerkonten, pflegen verifizierte Partner-Shops, verwalten API-Keys, haben Zugriff auf Statistiken und das Admin-Dashboard (spätere Versionen)

---

# 3. Produktübersicht

Die Anwendung besteht aus folgenden zentralen Modulen:

**Benutzerverwaltung:** Registrierung, Login (Spring Security), Rollenverwaltung (USER/ADMIN), (Spätere Version: Profilverwaltung).

**Fahrradverwaltung:** CRUD-Operationen für Fahrräder und zugehörige Verschleißteile. Jedem Fahrrad können mehrere Teile zugeordnet werden (Kette, Kassette, Bremsbeläge, Reifen, Schaltung usw.).

**Warenkorb:** Benutzer markieren Fahrradteile und legen sie in den Warenkorb. Ein aktiver Warenkorb pro Benutzer; Spätere Versionen: Warenkörbe können benannt und gespeichert werden.

**Hybridsuche (KI-gestützt):**
- Ollama generiert optimierte Suchanfragen (z. B. "Fahrradkette MTB" → "Shimano XT CN-HG95 10-fach Kette")
- Das Backend ruft parallel mehrere Quellen ab:
  - **Phase 1 (MVP):** bike-components.de via Jsoup (Web-Scraping, Vue.js + Inertia.js SSR - JSON direkt aus HTML-Attribut)
  - **Phase 2:** rosebikes.de via Jsoup (Web-Scraping, Custom SSR) + Amazon Product Advertising API + eBay Finding API
- Ollama bewertet und filtert die gesammelten Ergebnisse
- Ausgabe: Tabelle mit den 5-10 günstigsten Angeboten je Artikel (Produkt | Shop | URL | Preis | Versand)

**Admin-Bereich:** Spätere Versionen: Benutzerverwaltung, Partner-Shop-Verwaltung, Statistiken, API-Key-Verwaltung.

---

# 4. Produktfunktionen

## 4.1 Geschäftsprozesse

| **(ID) Geschäftsprozess** | /F10/ Fahrrad und Teile anzeigen |
|---|---|
| **Kategorie** | Primär |
| **Vorbedingung** | Benutzer ist eingeloggt |
| **Nachbedingung Erfolg** | Fahrrad bzw. Fahrradteil ist gespeichert / aktualisiert / gelöscht |
| **Nachbedingung Fehlschlag** | Validierungsfehler, Datenbankfehler |
| **Akteure** | USER |
| **Auslösendes Ereignis** | Benutzer navigiert zur Fahrradverwaltung |
| **Beschreibung** | * Admin importiert die Daten für den User in die Datenbank (-> fertig)<br>* User kann eine Liste mit Fahrrädern und deren Fahrradteilen anzeigen.  (-> fertig)<br> * Validation Groups verwenden. + ControllerAdvice für bessere Error-Messages (GlobalExceptionHandler) Siehe MVC und  Thymeleaf.  (-> fertig)<br> * Detailierte Fehlermeldungen, RestExceptionHandler für RestController  (-> fertig)<br> * Styling der Seiten  (-> fertig, könnte noch verbessert werden)|
| **Erweiterung** | UI in späterer Version: <br>1. Fahrrad anlegen (Typ, Name, Marke, Modell, eBike-Flag)<br>2. Fahrradteile zum Fahrrad hinzufügen (Typ, Marke, Qualität, Spezifikation)<br>3. Teile bearbeiten oder löschen <br>4. Kaufdatum und Kaufpreis des letzten Kaufs können gespeichert werden |
| **Alternativen** | Admin importiert Daten initial in die Datenbank |

---

| **(ID) Geschäftsprozess** | /F20/ Warenkorb zusammenstellen |
|---|---|
| **Kategorie** | Primär |
| **Vorbedingung** | Benutzer ist eingeloggt, mindestens ein Fahrradteil ist vorhanden |
| **Nachbedingung Erfolg** | Ausgewählte Teile befinden sich im aktiven Warenkorb |
| **Nachbedingung Fehlschlag** | Teil konnte nicht in den Warenkorb gelegt werden |
| **Akteure** | USER |
| **Auslösendes Ereignis** | Benutzer markiert Fahrradteile und klickt „In den Warenkorb" |
| **Beschreibung** | 1. Liste eigener Fahrräder und Teile anzeigen (-> fertig)<br>2. Teile auswählen / markieren (-> fertig)<br>3. Teile in den aktiven Warenkorb legen (-> fertig)<br>4. Menge angeben (-> fertig) <br>5. Warenkorb anzeigen <br>6. Button auf Warenkorb von jeder Seite anzeigen (-> fertig)|
| **Erweiterung** | - Gesamtsumme für die einzelnen Shops anzeigen <br> |
| **Alternativen** | Artikel ohne Zuordnung zu eigenem Fahrrad manuell als Freitext eingeben |

---

| **(ID) Geschäftsprozess** | /F25/ Login + Account bearbeiten + Security |
|---|---|
| **Kategorie** | Primär |
| **Vorbedingung** | - |
| **Nachbedingung Erfolg** | Benutzer und Admin loggen sich ein. Es wird unterschieden in Admin und Benutzer |
| **Nachbedingung Fehlschlag** | |
| **Akteure** | USER, Admin |
| **Auslösendes Ereignis** | Benutzer loggt sich ein.|
| **Beschreibung** | - Login für Benutzer (-> fertig)<br>- Security-Prozess ist implementiert (-> fertig)<br>- Speichern des Beans: Account in der Session (-> fertig)<br>- Passwort verschlüsseln (-> fertig)|
| **Erweiterung** |  - Benutzer bearbeiten<br> - Verschiedene Rechte. <br>- Login für Admin<br>- Ändern der Accountdaten. <br>|
| **Alternativen** |  |

---

| **(ID) Geschäftsprozess** | /F30/ Preise + Versandkosten per scraping herausfinden und speichern |
|---|---|
| **Kategorie** | Primär |
| **Vorbedingung** | Benutzer ist eingeloggt, Warenkorb enthält mindestens einen Artikel |
| **Nachbedingung Erfolg** | In der Datenbank sind die Ergebnisdaten gespeichert |
| **Nachbedingung Fehlschlag** | Info wenn gewünschter shop ausfällt|
| **Akteure** | USER |
| **Auslösendes Ereignis** | Benutzer klickt „Günstigsten Shop finden" |
| **Beschreibung** | 1. Für jeden Artikel im Warenkorb: <br>2. Backend baut Such-URL und ruft Quellen parallel ab:<br>   **Phase 1 (MVP):** per Scraping bike-components.de: <br> - Preise scrapen → `https://www.bike-components.de/de/s/?keywords={query}`(-> fertig)<br> - Versandkosten scrapen und speichern. (-> fertig)<br> - bei API-Änderungen/Fehler loggen. (-> fertig)<br> - Info an den User ob beim scraping ein Fehler aufgetreten ist. Es wird ein SearchResult vom service zurückgegeben und auf Webseite angezeigt (-> fertig)<br> - Die Suchbegriffe müssen auf deutsch sein. (-> fertig)<br>  <br>3. Die gescrapten Preise werden in der Datenbank gespeichert. 1x pro tag / beim ersten Zugriff auf die Suche die Preise mit dem Shop vergleichen (Scraping) und speichern. <br>4. Ausgabe als Tabelle: Produkt \| Shop (URL) \| Preis \| (-> fertig)
| **Erweiterung** |  **Phase 2:** per Scraping: rosebikes.de <br> - Preise scrapen →   `https://www.rosebikes.de/search?q={query}` ACHTUNG: laut robots.txt ist search verboten: `Disallow: /search?*`<br> <br> Parallel-Processing aller Artikel |
| **Alternativen** | |

**Notizen:**

* **Sicherheit:**  Query-Manipulation verhindern.
(Search Injection).
Da Bikepart-Felder direkt als Suchbegriff an externe Shops gesendet werden, muss verhindert werden, dass in diesen Feldern JavaScript-Code steht.
Beim Speichern des Entities werden über Validierung HTML-Tags und nicht erlaubte Sonderzeichen erst garn nicht gespeichert. 
*  **Proxy:**  Es wurde getestet, ob der Proxy Squid eingesetzt werden konnte.  Um die Server der Bikeshops zu entlasten. Detail-Ausführungen siehe HTTP_Proxy_Squid.md. Das resultat war: er wird nicht eingesetzt, da er für HTTPS nicht verwendet werden kann. => code wurde 24.3.2026 wieder entfernt


* **Testdaten für die Entwicklung:** Aktuell werden die Daten in der Datenbank h2 gespeichert, dass nicht bei jedem Hochfahren die Preise gescraped werden. 
TODO: gescrapede Preise mit den Preisen in der DB vergleichen bzw. überschreiben 

---

| **(ID) Geschäftsprozess** | /F35/ Günstigsten Shop finden (Hybridsuche) |
|---|---|
| **Kategorie** | Primär |
| **Vorbedingung** | Benutzer ist eingeloggt, Warenkorb enthält mindestens einen Artikel |
| **Nachbedingung Erfolg** | Tabelle mit bis zu 10 günstigste Angeboten pro Artikel wird angezeigt |
| **Nachbedingung Fehlschlag** | Keine Ergebnisse gefunden, Fehlermeldung; Fallback wenn eine Quelle ausfällt |
| **Akteure** | USER |
| **Auslösendes Ereignis** | Benutzer klickt „Günstigsten Shop finden" |
| **Beschreibung** | 1. Für jeden Artikel im Warenkorb: An die KI Llama.cpp werden die productOffers aus den Preisrecherchen gegeben. Diese bewertet dei Ergebnisse und gibt 1 optimales Produkt zurück. <br> TODO Beschreibung fertigmachen <br> 2. Ergebnisse (Preis, Shop, URL, Versand) werden gesammelt. Es wird eine Liste von für einen Bikepart als ProductOffers aus allen shops an llama.cpp übergeben. (-> fertig)<br>3. Ollama bewertet und filtert: Top 5-10 Angebote je Artikel (-> fertig)<br>4. Ausgabe als Tabelle: Produkt \| Shop (URL) \| Preis \|
| **Erweiterung** | Caching von Ergebnissen (1-2 h, für Entwicklung 14 Tage)  (-> fertig) <br>Loading-Indicator während der Suche <br> Parallel-Processing aller Artikel |
| **Alternativen** | Wenn eine Scraping-Quelle nicht verfügbar ist, werden verbleibende Quellen verwendet (Fallback-Strategie) |

#### Notizen
Evtl Suche mit serchNG. Das wäre wie google-Suche. Hier müsste ich per JNI die c++-Bibliothek anprogrammieren.

Die Ollama/Llama.cpp kann auf 3 Methoden anprogrammiert werden:
1. per spring-ai-starter-model-ollama
2. über JNI kann die ollama.cpp anprogrammiert werden. Hier muss das passende Modell dann mitgeliefert werden. 
3. Realisiert: Anprogrammieren des llama.cpp Servers per HttpClient. Ich habe extra java.net.http.HttpClient, da man die Klassen auch mit Java SE nutzen kann. Mit den Klassen von SpringBoot wären die REST-Requests deutlich schneller. Diese Geschwindigkeit brauche ich aber für den Webshop nicht.

Für die Variante 3 muss man llama.cpp von der Webseite herunterladen und auspacken. Der Server muss entweder per Hand oder wird automatisch
von der Applikation gestartet. Siehe Readme.md  -> Diese Variante habe ich implementiert (-> fertig)

Analyse der Preis-Suche siehe 
[Pflichtenheft_Preisermittlung_Amazon_Idealo_BraveSearch_ebay_Scraping_v3.md](docs/Pflichtenheft_Preisermittlung_Amazon_Idealo_BraveSearch_ebay_Scraping_v3.md)



---

| **(ID) Geschäftsprozess** | /F40/ Benutzer- und Rollenverwaltung (Admin) -> Offen. Nicht im MVP |
|---|---|
| **Kategorie** | Sekundär |
| **Vorbedingung** | Benutzer ist als ADMIN eingeloggt |
| **Nachbedingung Erfolg** | Benutzer wurde gesperrt / aktiviert / gelöscht |
| **Nachbedingung Fehlschlag** | Aktion konnte nicht durchgeführt werden |
| **Akteure** | ADMIN |
| **Auslösendes Ereignis** | Admin navigiert zum Admin-Dashboard |
| **Beschreibung** | 1. Benutzerliste anzeigen<br>2. Benutzer sperren oder aktivieren<br>3. Benutzer löschen |
| **Erweiterung** | Statistiken und Suchlogs einsehen |
| **Alternativen** | - |

---

| **(ID) Geschäftsprozess** | /F50/ Shop-Favoriten und Blacklist verwalten -> Offen. Nicht im MVP|
|---|---|
| **Kategorie** | Sekundär (Version 2) |
| **Vorbedingung** | Benutzer ist eingeloggt |
| **Nachbedingung Erfolg** | Shop ist als Favorit oder auf der Blacklist gespeichert |
| **Nachbedingung Fehlschlag** | - |
| **Akteure** | USER |
| **Auslösendes Ereignis** | Benutzer markiert einen Shop aus den Suchergebnissen |
| **Beschreibung** | 1. Shop als Favorit markieren (wird bei Suche bevorzugt)<br>2. Shop auf Blacklist setzen (wird bei Suche ausgeschlossen)<br>3. Name, URL, Notizen zum Shop speichern |
| **Erweiterung** | - |
| **Alternativen** | - |

---

# 5. Produktdaten
Siehe [Datenmodell_Entities_v4.md](docs/Datenmodell_Entities_v4.md)
Das sind Nummerierungen wie /D10/,....

# 6. Produktleistungen

**Alle Produktleistungen werden nach dem MVP implementiert**

/L10/ Die Suchanfrage (/F30/) darf bei bis zu 3 Artikeln im Warenkorb nicht länger als 40 Sekunden dauern. Durch Parallel-Processing ist eine Zielzeit von unter 20 Sekunden anzustreben.

/L20/ Alle sonstigen Benutzeraktionen (Navigieren, Speichern, Laden) müssen unter 2 Sekunden reagieren.

/L30/ Suchergebnisse sollen für 1-24 Stunden gecacht werden, um wiederholte Scraping-Anfragen und API-Calls zu vermeiden.

/L40/ Während der Suche wird dem Benutzer ein Loading-Indicator angezeigt.

/L50/ Fällt eine Datenquelle (Scraping-Shop, Amazon, eBay) aus, wird die Suche mit den verbleibenden Quellen fortgesetzt (Fallback-Strategie).

---

# 7. Qualitätsanforderungen

| **Produktqualität** | **sehr gut** | **gut** | **normal** | **nicht relevant** |
|---|:---:|:---:|:---:|:---:|
| **Funktionalität** | | | | |
| Angemessenheit | | X | | |
| Richtigkeit | | X | | |
| Interoperabilität | | X | | |
| Sicherheit | X | | | |
| **Zuverlässigkeit** | | | | |
| Fehlertoleranz | | X | | |
| Wiederherstellbarkeit | | | X | |
| **Benutzbarkeit** | | | | |
| Verständlichkeit | | X | | |
| Erlernbarkeit | | X | | |
| Bedienbarkeit | X | | | |
| **Effizienz** | | | | |
| Zeitverhalten | | X | | |
| **Wartbarkeit** | | | | |
| Analysierbarkeit | | X | | |
| Änderbarkeit | | X | | |

---

# 8. Benutzungsoberfläche

/B10/ Die Oberfläche wird als Server-Side-Rendering (SSR) mit Thymeleaf umgesetzt. Sie ist für Desktop-Browser optimiert. 

/B20/ Die Bedienung ist auf Mausbedienung ausgelegt; Formulare sind intuitiv gestaltet.

/B30/ Folgende Rollen und ihre Rechte sind zu unterscheiden:

| **Rolle** | **Rechte** |
|---|---|
| USER | /F10/, /F20/, /F30/, /F50/ - Fahrräder & Teile verwalten, Warenkorb, Suche, Favoriten/Blacklist |
| ADMIN | /F40/ + alle USER-Rechte - Benutzerverwaltung, Partner-Shops, API-Keys, Statistiken |

/B40/ Das Admin-Dashboard ist über einen separaten gesicherten Bereich erreichbar.

/B50/ Fehlermeldungen werden dem Benutzer verständlich in der Oberfläche angezeigt (kein Stack-Trace).

---

# 9. Nichtfunktionale Anforderungen
/N00/ **Profile**:  Profile für verschiedene Umgebungen nutzen. z.B. die H2 Datenbank zur Entwicklungszeit. Konfiguration mit application-h2.properties. (-> fertig)

Profil für prod mit Einstellungen für den Linux-Server (-> fertig)

/N10/ **Sicherheit:** Spring Security wird für Authentifizierung und Autorisierung eingesetzt. Passwörter werden verschlüsselt gespeichert. (-> fertig) Ggf Security mit AOP verwenden um einzelne Methoden zu schützen. (-> fertig. Verwendet für das h2 Profil)

API-Keys für externe Dienste werden ebenfalls verschlüsselt abgelegt. 

/N20/ **Logging + Performance:** Logging wird mit SLF4J eingerichtet. AOP wird für einheitliches Exception-Handling und Logging eingesetzt (`@Aspect`, `@Around`)  (-> fertig). <br> Für Performance Messungen kann `@Around` verwendet werden. Es soll nicht bei allen Methoden die Performance gemessen werden. Es wird mit der selbst definierten `@Timed`  Annotation gesteuert, welche Methoden mit PerformanceAspect gemessen werden sollen. Bei der Verwendung von @Timed wird konfiguriert, dass das nur im Profil h2/dev (`@Profile("h2") und @Timed`) verwendet werden soll, Nicht in Prod, da dies die Performance verschlechtert.  (-> fertig)

/N30/ **Spring Actuator:** Monitoring mit Spring Boot Actuator aufbauen.

/N40/ **Caching:** Suchergebnisse werden gecacht (1-24 h), um die Last auf Scraping-Zielen und externen APIs zu reduzieren.  (-> fertig)

/N50/ **Datenschutz:** Benutzerdaten dürfen nicht an Dritte weitergegeben werden. Externe API-Aufrufe und Scraping-Anfragen enthalten keine personenbezogenen Daten des Benutzers.  (-> fertig)

/N60/ **Erweiterbarkeit:** Die Hybrid-Suchmechanik ist so gestaltet, dass neue Datenquellen (weitere Scraping-Shops) ohne Änderungen an bestehenden Quellen ergänzt werden können. Jede Quelle implementiert ein einheitliches `ScraperShopInterface`.  (-> fertig)

/N70/ **Native image:** Verwendung von GraalVM um ein natives Binary zu erzeugen. Dadurch benötigt man keine Java VM, um das Programm auszuführen. -> fertig 27.3.2026


/N80/ **Deploy und Ausführung unter Linux** Auf github bauen und Bikeparts auf einem Linux Server per wget holen und ausführen. Die dafür notwendigen Scripte schreiben.  -> fertig 28.3.2026

---

# 10. Technische Produktumgebung

Das Produkt ist als monolithische Spring Boot Webanwendung konzipiert und über einen Standard-Webbrowser nutzbar.

## 10.1 Software

| Komponente | Technologie |
|---|---|
| Backend-Framework | Spring Boot (Java) |
| Persistenz | Spring Data JPA, H2 (Entwicklung) / PostgreSQL oder MySQL (Produktion) |
| Template-Engine | Thymeleaf (SSR) + Spring MVC |
| KI-Integration | spring-ai-starter-model-ollama (Ollama) |
| Sicherheit | Spring Security |
| Web-Scraping | Jsoup - bike-components.de (Phase 1 MVP), rosebikes.de (Phase 2) |
| Externe APIs | Amazon Product Advertising API, eBay Finding API (beide Phase 2) |
| Testdaten | Java Faker |
| Logging | SLF4J mit AOP |
| Build-Tool | Maven |

### 10.1.1 Technische Anforderungen Software
Die Anwendung sollte die folgenden Anforderungen erfüllen:
1. Spring Data JPA zur Verwaltung der Datenpersistenz verwenden.
2. Alle wichtigen Annotationen wie @Entity, @Repository, @Service, @Controller usw. einsetzen.
3. CDI-Konfiguration für Dependency Injection (z.B. @Autowired, @Component) nutzen.
4. Logging mit Hilfe von SLF4J oder einer ähnlichen Logging-Bibliothek einrichten mit AOP (Aspektorientierte Programmierung).
5. Spring Web MVC für das Routing und die Controller-Logik einsetzen.
6. Thymeleaf als Template-Engine zur Erstellung der HTML-Seiten verwenden.
7. Die Java Faker-Bibliothek nutzen, um Testdaten für Fahrräder und Fahrradteile zu generieren.
8. Session Scope wird für die Verwaltung der Benutzersitzung (Spring Security) genutzt. Persistente Daten werden via Spring Data JPA in der Datenbank gespeichert.
9. Spring Data JPA zur Anbindung an eine relationale Datenbank (z.B. H2, MySQL, PostgreSQL) verwenden.
10. Es sollen Profile für verschiedene Umgebungen genutzt werden. z.B. die H2 Datenbank zur Entwicklungszeit. Konfiguration mit application-h2.properties.
11. AccountId und Cart sollen in der Session gespeichert werden.
12. Verwendung von GraalVM um ein native image / Natives Binary zu erzeugen. Hiermit benötigt man keine Java VM um das Programm auszuführen.

## 10.2 Hardware

Server: PC oder Cloud-Instanz mit ausreichend RAM für Ollama-Modell (mind. 8 GB RAM empfohlen).

Client: Browserfähiges Gerät (Desktop/Laptop), Bildschirmauflösung ab 1280 × 768.

Hinweis: Bei schwacher GPU kann die Ollama-Verarbeitung 5-10 Sekunden pro Artikel benötigen. Parallel-Processing reduziert die Gesamtwartezeit.

## 10.3 Produktschnittstellen

### 10.3.1 Web-Scraping via Jsoup (Phase 1 MVP & Phase 2)

Beide Shops wurden auf Scraping-Tauglichkeit analysiert (03.03.2026). Ergebnis: Beide
verwenden Server-Side Rendering (SSR) - alle Produktdaten sind im statischen
HTML-Response enthalten. Kein JavaScript-Rendering nötig. Jsoup ist ausreichend.

| Merkmal | bike-components.de | rosebikes.de |
|---|---|---|
| **robots.txt - Suchseite** | Kein Disallow | `Disallow: /search?*` |
| **Rendering** | Vue.js + Inertia.js (SSR) | Custom SSR |
| **Datenformat** | JSON in `data-props`-Attribut | GTM DataLayer JSON + CSS-Selektoren |
| **Preis** | `priceRaw: 19.99` (Float, direkt nutzbar) | String `"22,95 €"` (Regex-Parsing nötig) |
| **Verfügbarkeit** | Boolean-Flags: `isBuyable`, `isSoldOut` | CSS-Klassen |
| **Parsing-Aufwand** | Gering | Mittel |
| **Selenium nötig?** | Nein | Nein |
| **Implementierungsreihenfolge** | **Phase 1 - MVP** | **Phase 2 - nur für Privatnutzung** |

Technische Details, Parsing-Code, JSON-Felder und robots.txt-Bewertung:
siehe [Pflichtenheft_Zusatz_Scraping_Shop_Analyse_v1.md](docs/Pflichtenheft_Zusatz_Scraping_Shop_Analyse_v1.md).


### 10.3.2 Externe APIs (Phase 2)

- **Amazon – NICHT MEHR VERFÜGBAR (Stand: 16.03.2026):** PA-API wird am 30.04.2026 deprecated. Amazon akzeptiert keine neuen PA-API-Kunden mehr. Die Nachfolge-API heißt **Creators API**, erfordert jedoch ein aktives Amazon-Affiliate-Konto mit mindestens 10 qualifizierenden Verkäufen innerhalb der letzten 30 Tage. Da BikePartsFinder kein Affiliate-Portal ist, ist dieser Zugang im Scope dieses Projekts nicht erreichbar. **Amazon-Integration entfällt für Phase 2.**
- **eBay Browse API** (kostenlos nach Registrierung; Nachfolger der deprecated Finding API): Suche nach Neu- und Gebrauchtteilen; Registrierung unter https://developer.ebay.com — API-Keys innerhalb von 1–2 Werktagen verfügbar, kein Umsatznachweis erforderlich.

### 10.3.3 KI-Integration

- **Ollama (lokal):** KI-Modell für Suchanfragen-Optimierung und Ergebnis-Bewertung; Integration über `spring-ai-starter-model-ollama`

---

# 11. Spezielle Anforderungen an die Entwicklungsumgebung

- Java-Entwicklungsumgebung (JDK 21+)
- Ollama muss lokal installiert und ein passendes Sprachmodell geladen sein (alternativ: JNI-Integration von ollama.cpp - kein separates Ollama-Install nötig; offene Entscheidung)
- Zugang zu Amazon PAAPI und eBay Finding API erst ab Phase 2 erforderlich (Registrierung erforderlich)
- H2 für lokale Entwicklung; PostgreSQL / MySQL für Staging und Produktion

---

# 12. Gliederung in Teilprodukte

## 12.1 Phase 1 - MVP

| ID | Funktion |
|---|---|
| /F10/ | Fahrrad- und Teile-Verwaltung (kein CRUD, nur Anzeige; Import der Daten per SQL-Script). Minimale UI nur für die Demo |
| - | Code: klare Schichtentrennung (Controller → Service → Repository), aussagekräftige Tests, gutes README mit Architektur-Beschreibung |
| /F20/ | Warenkorb zusammenstellen |
| /F30/ | Hybridsuche: **bike-components.de** (Jsoup + JSON-Parsing aus `data-props`-Attribut) + Ollama (Suchanfrage-Optimierung + Ergebnis-Priorisierung) |
| /F40/ | Benutzer-Login, Registrierung, Rollen (USER/ADMIN) |
| - | Ausgabe: Tabelle pro Artikel mit Top-5-Angeboten (Produkt, Shop, URL, Preis, Versand) |
| - | Datenbank: H2 (Entwicklung), Testdaten via Java Faker |
| /N20/ | Logging mit SLF4J + AOP |
| /N20/ | Einheitliches Exception-Handling (`@ExceptionHandler`) + AOP. Stichwort @ControllerAdvice und GlobalExceptionHandler |

## 12.2 Phase 2 - Erweitert & APIs

| ID | Funktion |
|---|---|
| /N30/ | Spring Actuator für Monitoring |
| - | Production-Datenbank: PostgreSQL / MySQL |
| /F30+/ | **rosebikes.de** als zweite Scraping-Quelle ergänzen (Jsoup + GTM DataLayer JSON / CSS-Selektoren-Fallback) |
| /F30+/ | Amazon Product Advertising API integrieren -> deprecated |
| /F30+/ | eBay Finding API integrieren |
| /F20+/ | Warenkorb-Optimierung: Ollama analysiert alle Artikel gemeinsam, Empfehlung nach Shop-Bündelung |
| - | Suchprioritäten des Benutzers (Gesamtpreis, Versand, Lieferzeit, Zahlungsart) |

## 12.3 Phase 3 - CRUD UI für Fahrradteile & Admin & Favoriten

| ID | Funktion |
|---|---|
| /F10/ | Fahrrad- und Teile-Verwaltung (CRUD) |
| /F50/ | Als User: Favoriten-Shops + Blacklist von Shops speichern |
| /F40+/ | Admin-Dashboard mit Statistiken |
| /F40+/ | Vollständiges User-Management (sperren, aktivieren, löschen) |
| /F60/ | Verifizierte Partner-Shops pflegen (Admin) |
| /F61/ | Shop-API-Keys verwalten (Admin) |
| /F70/ | Suchergebnis speichern (Name + Datum) |

## 12.4 Phase 4 - Benachrichtigung, Security REST

| ID | Funktion |
|---|---|
| - | JavaMailSender: Warenkorb per E-Mail an Benutzer versenden |
| - | Mehrsprachigkeit (i18n: DE/EN) |
| - | Aktuell ist der REST-Controller ungeschützt. Hierfür muss eine separate Authentifizierung eingebaut werden. (SessionScope funktioniert hier nicht) Authentifizierung mit JWT einbauen. Man bekommt ein Token. |
| - | Registrierung eines neuen Users |

---

# 13. Testszenarien

Während und nach der Produktentwicklung werden folgende Tests durchgeführt:

- Login und Logout; Zugriffskontrolle nach Rolle (USER darf nicht in Admin-Bereich)
- CRUD-Tests für Fahrräder und Fahrradteile
- Warenkorb: Hinzufügen, Entfernen, Mengenänderung
- Suche (Phase 1): Aufruf der Hybridsuche mit Mock-HTML für bike-components.de; Prüfung des Jsoup-JSON-Parsings und der Ollama-Suchanfrage-Optimierung
- Scraping-Robustheit: Fallback von JSON-Parsing (`data-props`) auf CSS-Selektoren bei HTML-Struktur-Änderungen
- Suche (Phase 2): Integration von rosebikes.de, Amazon PAAPI und eBay API; Mock-Daten für API-Aufrufe
- Fallback-Test: Simulation eines Scraping-Ausfalls (Shop antwortet nicht oder liefert geändertes HTML → Fallback auf andere Quellen)
- Performance-Test: Suche mit 3 Artikeln im Warenkorb; Ziel < 40 Sekunden Gesamtdauer
- Datenbank: Cascade-Delete prüfen (Löschen eines Accounts löscht zugehörige Bikes, Parts, Carts)
- Sicherheit: Passwörter werden nur verschlüsselt gespeichert; API-Keys nicht im Klartext
- Admin: Benutzer sperren/aktivieren; Partner-Shop anlegen und löschen (Phase 3)

---

# 14. Ergänzungen

**Performance-Problem und Lösung:**
Bei 3 Artikeln im Warenkorb entstehen in Phase 1 bis zu 3 Scraping-Requests (bike-components.de), in Phase 2 bis zu 15+ API-Calls + Web-Requests. Bei schwacher GPU kann Ollama 5-10 Sekunden pro Artikel benötigen. Gegenmaßnahmen: Parallel-Processing aller Artikel (`CompletableFuture`), Caching von Produktdaten (1-24 h), Loading-Indicator für den Benutzer.

**Web-Scraping Machbarkeit - Analyse vom 03.03.2026:**

Beide Ziel-Shops wurden auf Scraping-Tauglichkeit mit Jsoup geprüft. Ergebnis: Beide sind ohne JavaScript-Rendering scrapbar.

| Eigenschaft | bike-components.de | rosebikes.de |
|---|---|---|
| Rendering | Vue.js + Inertia.js (SSR) | Custom SSR |
| Datenformat | JSON in `data-props`-Attribut | GTM DataLayer JSON + CSS-Selektoren |
| Preis | Float direkt (`priceRaw: 19.99`) | String "22,95 €" (Regex-Parsing nötig) |
| Verfügbarkeit | Boolean-Flags (`isSoldOut`, `isBuyable`) | CSS-Klassen |
| Bewertungen | `reviewStars` + `reviewsCount` | Im HTML vorhanden |
| Paginierung | `total` + 24 Produkte pro Seite | Vorhanden |
| Parsing-Aufwand | Gering | Mittel |
| Selenium nötig? | Nein | Nein |

**Risiken Web-Scraping und Mitigation:**

| Risiko | Mitigation |
|---|---|
| HTML-Struktur ändert sich durch Shop-Update | Robuste Selektoren + JSON-Fallback auf CSS-Selektoren; Fehler per AOP-Logging erfassen; Monitoring via Spring Actuator |
| Rate Limiting durch Shop | `@Cacheable` (1-24 h), Request-Delays zwischen Anfragen, `User-Agent: BikePartsFinder/1.0` setzen |
| Rechtliches | `robots.txt` beider Shops vor Go-Live prüfen; keine personenbezogenen Daten in Scraping-Anfragen |

**KI-Integration (Ollama) - zwei mögliche Ansätze:**
- **Variante A:** `spring-ai-starter-model-ollama` - einfache Integration, Ollama muss auf dem Server installiert sein
- **Variante B:** JNI-Integration von `ollama.cpp` - kein separates Ollama-Install nötig, ggf. bessere Performance auf schwacher GPU; höherer Implementierungsaufwand

**Domain-Vorschläge:**
- bikeparts-bestprice.de
- cheapest-bikeparts.de
- bikeparts-finder.de

**Offene Punkte (TODO):**
- `robots.txt` von bike-components.de und rosebikes.de vor Go-Live prüfen
- Actuator-Konfiguration für Monitoring festlegen
- Ollama-Variante entscheiden: spring-ai-starter vs. JNI/ollama.cpp

---

# 15. Glossar

| Begriff | Erläuterung |
|---|---|
| Ollama | Lokales KI-Sprachmodell, das für Suchanfrage-Optimierung und Ergebnisbewertung eingesetzt wird |
| Hybridsuche | Kombination aus Web-Scraping (Phase 1: bike-components.de; Phase 2: + rosebikes.de) sowie Amazon API und eBay API (Phase 2) zur Preisrecherche |
| Web-Scraping | Automatisiertes Auslesen von Webseiteninhalten; hier via Jsoup-Bibliothek |
| SSR | Server-Side Rendering - bei den Scraping-Zielen bedeutet dies: alle Produktdaten sind im initialen HTML-Response enthalten, kein Selenium nötig |
| Inertia.js | Framework, das Vue.js-Komponenten serverseitig vorrendert und alle Daten als JSON in `data-props`-Attributen im HTML einbettet (genutzt von bike-components.de) |
| data-props | HTML-Attribut des `ProductCatalog`-Elements auf bike-components.de, das ein vollständiges JSON-Objekt mit allen Produktdaten der Ergebnisseite enthält |
| priceRaw | Float-Preis direkt aus dem bike-components.de JSON, z. B. `19.99` — kann direkt als BigDecimal verwendet werden, kein String-Parsing nötig |
| GTM DataLayer | Google Tag Manager DataLayer - `<script>`-Block mit strukturierten Produktdaten, den rosebikes.de für Analytics einsetzt und der als primäre Scraping-Quelle dient |
| SearchSource | Geplantes Interface, das jede Suchquelle (Scraping-Shop, Amazon, eBay) implementiert, um neue Quellen ohne Änderung bestehenden Codes ergänzen zu können |
| BIKEPART | Fahrradteil, das einem Fahrrad (BIKE) zugeordnet ist (z. B. Kette, Kassette, Bremsbeläge) |
| CART | Warenkorb, der die zu suchenden Artikel enthält |
| PRODUCT_OFFER | Ein einzelnes Angebot eines Shops für einen Artikel aus dem Warenkorb |
| MTB | Mountain Bike |
| MTB_FULLY | Fully-Gefedertes Mountain Bike |
| GEAR_SHIFT | Schaltung (Fahrradteil-Typ) |
| AOP | Aspect-Oriented Programming - wird für Logging und Exception-Handling eingesetzt |
| JPA | Java Persistence API - Standard für Datenbankzugriff in Java |
| PAAPI | Amazon Product Advertising API |
| i18n | Internationalisierung (Mehrsprachigkeit) |
