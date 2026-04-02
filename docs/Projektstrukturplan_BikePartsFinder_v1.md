# Projektstrukturplan

**Projekt:** BikePartsFinder - KI-gestütztes Recherche-Tool für Fahrrad-Ersatzteile

**_Basis: Pflichtenheft_BikePartsFinder_v3.md_**

| | |
|---|---|
| **Auftraggeber:** | - |
| **Auftragnehmer:** | - |
| **Beginn:** | - |
| **Abschluss:** | - |

---

## Vorbereitung

| Lfd. Nr. | ID | Vorgang | Geplant (AT) | Tatsächlich (AT) | Von | Bis |
|---:|---|---|:---:|:---:|:---:|:---:|
| 1 | | Projektplanung | 3 | | | |
| 2 | MS1 | Abschluss Projektplanung | - | | | |
| 3 | TA1 | Erstellung Pflichtenheft | 2 | | | |
| 4 | MS2 | Abgabe Pflichtenheft | - | | | |
| 5 | TA2 | Analyse & Feinentwurf | 3 | | | |
| 6 | AP2.1 | Datenmodell & Entities | 1 | | | |
| 7 | AP2.2 | Architektur-Beschreibung (README) | 1 | | | |
| | | **Summe** | **10** | | | |

---

## Phase 1 - MVP

| Lfd. Nr. | ID | Vorgang | Geplant (AT) | Tatsächlich (AT) | Von | Bis |
|---:|---|---|:---:|:---:|:---:|:---:|
| 8 | TA3 | Umsetzung Phase 1 - MVP | - | | | |
| 9 | AP3.1 | Projektsetup (Spring Boot, H2, Maven) | 1 | | | |
| 10 | AP3.2 | Datenbankmodellierung & JPA-Entities | 3 | | | |
| 11 | AP3.3 | SQL-Import-Script (Bikes, Bikeparts) | 1 | | | |
| 12 | AP3.4 | Spring Security (Login, Registrierung, Rollen USER/ADMIN) | 3 | | | |
| 13 | AP3.5 | Fahrradverwaltung - Anzeige (Controller, Service, Repository) | 2 | | | |
| 14 | AP3.6 | Warenkorb (CART + CART_ITEM, Add/Remove) | 2 | | | |
| 15 | AP3.7 | Ollama-Integration (QueryExpander + ResultRanker) | 3 | | | |
| 16 | AP3.8 | Web-Scraping bike-components.de (Jsoup + JSON-Parsing) | 3 | | | |
| 17 | AP3.9 | Hybridsuche-Orchestrierung + Ergebnisausgabe (Tabelle) | 2 | | | |
| 18 | AP3.10 | AOP Logging (SLF4J + @Aspect) | 1 | | | |
| 19 | AP3.11 | AOP Exception-Handling (@ExceptionHandler) | 1 | | | |
| 20 | AP3.12 | Thymeleaf UI (Minimalansicht für Demo) | 2 | | | |
| 21 | AP3.13 | Testdaten via Java Faker | 1 | | | |
| 22 | TA4 | Test Phase 1 | - | | | |
| 23 | AP4.1 | Testszenarien entwerfen | 1 | | | |
| 24 | AP4.2 | Tests durchführen & Auswertung (Testprotokolle) | 2 | | | |
| 25 | MS3 | Abschluss Phase 1 - MVP | - | | | |
| | | **Summe** | **28** | | | |

---

## Phase 2 - Erweitert & APIs

| Lfd. Nr. | ID | Vorgang | Geplant (AT) | Tatsächlich (AT) | Von | Bis |
|---:|---|---|:---:|:---:|:---:|:---:|
| 26 | TA5 | Umsetzung Phase 2 - Erweitert & APIs | - | | | |
| 27 | AP5.1 | Spring Actuator (Monitoring) | 1 | | | |
| 28 | AP5.2 | Web-Scraping rosebikes.de (GTM DataLayer + CSS-Fallback) | 3 | | | |
| 29 | AP5.3 | Amazon Product Advertising API | 3 | | | |
| 30 | AP5.4 | eBay Finding API | 2 | | | |
| 31 | AP5.5 | Warenkorb-Optimierung (Ollama Shop-Bündelung) | 2 | | | |
| 32 | AP5.6 | Suchprioritäten des Benutzers | 2 | | | |
| 33 | AP5.7 | Produktionsdatenbank (PostgreSQL / MySQL) | 1 | | | |
| 34 | TA6 | Test Phase 2 | - | | | |
| 35 | AP6.1 | Integrationstests (rosebikes.de, Amazon, eBay) | 2 | | | |
| 36 | AP6.2 | Performance-Test (3 Artikel, Ziel < 40 Sek.) | 1 | | | |
| 37 | AP6.3 | Fallback-Test (Scraping-Ausfall simulieren) | 1 | | | |
| 38 | MS4 | Abschluss Phase 2 | - | | | |
| | | **Summe** | **18** | | | |

---

## Phase 3 - CRUD & Admin

| Lfd. Nr. | ID | Vorgang | Geplant (AT) | Tatsächlich (AT) | Von | Bis |
|---:|---|---|:---:|:---:|:---:|:---:|
| 39 | TA7 | Umsetzung Phase 3 - CRUD & Admin | - | | | |
| 40 | AP7.1 | CRUD Fahrradverwaltung (Fahrräder + Teile) | 4 | | | |
| 41 | AP7.2 | Favoriten-Shops & Blacklist (User) | 2 | | | |
| 42 | AP7.3 | Admin-Dashboard (Statistiken, Suchlogs) | 3 | | | |
| 43 | AP7.4 | User-Management Admin (sperren, aktivieren, löschen) | 2 | | | |
| 44 | AP7.5 | Partner-Shops & API-Keys verwalten (Admin) | 2 | | | |
| 45 | AP7.6 | Suchergebnis speichern (Name + Datum) | 1 | | | |
| 46 | MS5 | Abschluss Phase 3 | - | | | |
| | | **Summe** | **14** | | | |

---

## Phase 4 - Benachrichtigung

| Lfd. Nr. | ID | Vorgang | Geplant (AT) | Tatsächlich (AT) | Von | Bis |
|---:|---|---|:---:|:---:|:---:|:---:|
| 47 | TA8 | Umsetzung Phase 4 - Benachrichtigung | - | | | |
| 48 | AP8.1 | JavaMailSender (Warenkorb per E-Mail) | 2 | | | |
| 49 | AP8.2 | Mehrsprachigkeit i18n (DE/EN) | 2 | | | |
| | | **Summe** | **4** | | | |

---

## Projektabschluss

| Lfd. Nr. | ID | Vorgang | Geplant (AT) | Tatsächlich (AT) | Von | Bis |
|---:|---|---|:---:|:---:|:---:|:---:|
| 50 | TA9 | Projektabschluss | - | | | |
| 51 | AP9.1 | Abschlussdokumentation & README | 1 | | | |
| 52 | MS6 | Auslieferung / Abgabe | - | | | |
| 53 | MS7 | Projektende | - | | | |
| | | **Summe** | **1** | | | |

---

## Gesamtsumme

| | Geplant (AT) | Tatsächlich (AT) |
|---|:---:|:---:|
| Vorbereitung | 10 | |
| Phase 1 - MVP | 28 | |
| Phase 2 - Erweitert & APIs | 18 | |
| Phase 3 - CRUD & Admin | 14 | |
| Phase 4 - Benachrichtigung | 4 | |
| Projektabschluss | 1 | |
| **Gesamt** | **75** | |

---

## Legende

| Kürzel | Bedeutung |
|---|---|
| MS | Meilenstein (kein Zeitaufwand) |
| TA | Teilaufgabe / Arbeitspaket-Gruppe |
| AP | Arbeitspaket |
| AT | Arbeitstage |
| - | Kein Zeitaufwand (Meilenstein) oder noch nicht geschätzt |
