# Pflichtenheft Preisermittlung – Quellen-Analyse für BikePartsFinder

**Stand:** 16.03.2026  
**Zweck:** Bewertung aller untersuchten externen Preisquellen für Phase 2 der Hybridsuche

---

## Ergebnis-Übersicht

| Quelle | Typ | Verfügbar? | Empfehlung |
|---|---|---|---|
| Amazon PA-API | Offizielle API | ❌ Deprecated, keine Neuregistrierung | Nicht verwenden |
| Amazon Creators API | Offizielle API | ❌ Affiliate-Nachweis erforderlich | Nicht verwenden |
| Amazon Scraping | Web Scraping | ❌ ToS + robots.txt verboten, CAPTCHA-Blocking | Nicht verwenden |
| idealo API (PWS 2.0) | Händler-API | ❌ Falsche API (Händler, kein Preisabruf) | Nicht verwenden |
| geizhals.de | Keine public API | ❌ Nur inoffizielle Bastel-Projekte | Nicht verwenden |
| billiger.de API | Händler-Partnermodell | ❌ Kooperationsvertrag + Unternehmen nötig | Nicht verwenden |
| Brave Search API | Web-Suche API | ❌ Liefert URLs, keine Preise; kostenpflichtig ab ~1.000 Anfragen | Nicht verwenden |
| eBay Browse API | Offizielle Developer-API | ⚠️ Sandbox frei; Production nur via EPN; überwiegend Gebrauchtteile → für Neuteile wenig relevant | Nur Sandbox / optional |
| **bike-components.de** | Web Scraping | ✅ robots.txt erlaubt, bereits implementiert | **Phase 1 — verwenden** |
| **bike-discount.de** | Web Scraping | ✅ robots.txt erlaubt, Such-URL verifiziert | **Phase 2 — verwenden** |
| bike-discount.de Store-API | Shopware 6 REST-API | ❌ sw-access-key nicht öffentlich exponiert | Nicht verwenden |
| rosebikes.de | Web Scraping | ⚠️ robots.txt sperrt Suche, nur privat/lokal | Nur lokal |
| fahrrad-xxl.de | Web Scraping | ❌ Schlechtes Sortiment (kein 10-fach), `Disallow: /suche/` | Nicht verwenden |
| raddiscount.de | Web Scraping | ❌ CGI-Suche, kein Sortiment, keine Ergebnisse | Nicht verwenden |
| fahrrad.de | Web Scraping | ❌ Shopify: `Disallow: /search` | Nicht verwenden |
| fahrrad-teile.shop | Web Scraping | ❌ Shopify: `Disallow: /search` | Nicht verwenden |
| bike24.de | Web Scraping | ❌ `Disallow: /search?*` | Nicht verwenden |
| rad24.de | Web Scraping | ❌ Kein SSL | Nicht verwenden |

---

## 1. Amazon PA-API (Product Advertising API)

**Ergebnis: ❌ Nicht verwendbar**

- PA-API wird am **30.04.2026 deprecated**. Amazon akzeptiert **keine neuen PA-API-Kunden mehr**.
- Die Nachfolge-API heißt **Creators API** und erfordert ein aktives Amazon-Affiliate-Konto mit mindestens **10 qualifizierenden Verkäufen innerhalb der letzten 30 Tage**.
- Ein **Affiliate** ist jemand, der Produkte auf einer eigenen Website mit Provisions-Links verlinkt und dadurch Verkäufe bei Amazon generiert. BikePartsFinder ist kein Affiliate-Portal → Voraussetzung nicht erfüllbar.
- Auch die alte PA-API ist für Neukunden gesperrt.

**Fazit:** Amazon-Integration entfällt vollständig für das Portfolio-Projekt.

---

## 2. Amazon Scraping

**Ergebnis: ❌ Nicht verwendbar**

- Amazon verbietet Web Scraping explizit in den Nutzungsbedingungen: keine Roboter, Spider oder automatisierten Mittel erlaubt.
- `robots.txt` setzt `Disallow: /` für alle User Agents außer privilegierten Crawlern (Googlebot, Bingbot). Kein Teil von Amazon ist für allgemeines Scraping freigegeben.
- Technisch: Amazon setzt CAPTCHA, IP-Blocking und Browser-Fingerprinting ein. Mit Jsoup nicht umsetzbar.
- Amazon klagte im November 2025 gegen Perplexity AI wegen Scraping – zeigt aktive Rechtsdurchsetzung.

**Fazit:** Rechtlich und technisch nicht vertretbar.

---

## 3. idealo API (PWS 2.0)

**Ergebnis: ❌ Falsche API für den Use Case**

- Die idealo PWS 2.0 ist eine **Angebotsdaten-Schnittstelle für Händler** – d.h. Shops können damit ihre eigenen Produkte auf idealo listen, aktualisieren und löschen.
- Es gibt **keine öffentliche Read-API**, mit der sich Preise fremder Shops abfragen lassen.
- Technology-Partner-Programm erfordert: registriertes Unternehmen, mindestens 1 aktiven Händler als Nutzer der Lösung, Listung von idealo auf der eigenen Website.

**Fazit:** Falsche API. Kein Preisabruf möglich. Nicht im Scope.

---

## 4. geizhals.de

**Ergebnis: ❌ Keine nutzbare API**

- Es gibt keine offizielle, öffentliche Preisabfrage-API für Entwickler.
- Einziges bekanntes Projekt ist ein inoffizielles GitHub-Projekt, das nur die Top-10-meistgeklickten Produkte liefert – kein stabiler, produktionsreifer Zugang.
- Scraping wäre technisch denkbar, aber rechtlich unklar (AGB prüfen).

**Fazit:** Keine brauchbare Lösung für das Projekt.

---

## 5. billiger.de API

**Ergebnis: ❌ Nicht zugänglich für private Entwickler**

- API-Zugang nur nach **Unterzeichnung eines Kooperationsvertrags** möglich.
- Teilnahme am Partnerprogramm nur für **registrierte Unternehmen oder eingetragene Kleinunternehmer**.
- Kein Self-Service-Zugang. Kontaktaufnahme über `partner@solute.de` erforderlich.

**Fazit:** Für ein Portfolio-Projekt nicht erreichbar.

---

## 6. Brave Search API

**Ergebnis: ❌ Falscher Ansatz für Preisermittlung**

- Die Brave Search API ist eine **Web-Suche-API** – sie liefert URLs und Snippets von Webseiten, keine strukturierten Produktpreise.
- Theoretischer Workflow: Brave API → Shop-URLs → jede URL einzeln mit Jsoup scrapen → Preis parsen. Sehr hoher Aufwand, keine Garantie welche Shops zurückkommen.
- **Kostenmodell (Stand: Feb. 2026):** Kein kostenloser Tier mehr für neue Nutzer. Neues Startguthaben: $5 (~1.000 Anfragen), danach $5 pro 1.000 Anfragen kostenpflichtig.
- Früher (bis Aug. 2025): 5.000 kostenlose Anfragen/Monat. Inzwischen abgeschafft.

**Fazit:** Falscher Ansatz für strukturierte Preisermittlung. Zu aufwändig, zu teuer, kein Mehrwert gegenüber direktem Scraping.

---

## 7. eBay Browse API ⚠️

**Ergebnis: ⚠️ Für diesen Use Case nur bedingt geeignet**

- Nachfolger der deprecated **eBay Finding API** (abgeschaltet Feb. 2025).
- Registrierung unter `developer.ebay.com` — Sandbox sofort kostenlos nutzbar.
- Production-Zugang erfordert Antrag beim **eBay Partner Network (EPN)**, Vertragsunterzeichnung und Genehmigung durch eBay (~10 Werktage, keine Garantie). Kein Studenten-Zugang vorhanden.
- **Kernproblem für BikePartsFinder:** eBay ist primär ein Gebrauchtmarkt und Marktplatz für Privatverkäufer. Für neue Fahrradteile (Shimano-Kette, Bremsbeläge etc.) ist das Sortiment deutlich schwächer als bei spezialisierten Fahrradshops. Suchergebnisse sind unstrukturierter (Auktionen, Privatverkäufer, unterschiedliche Zustände).
- Da BikePartsFinder **ausschließlich Neuteile** sucht, ist eBay keine sinnvolle primäre Quelle.

**Fazit:** Sandbox für Portfolio-Demo nutzbar. Für den persönlichen Betrieb mit echten Neuteile-Preisen nicht empfohlen — spezialisierte Bikeshops per Scraping sind relevanter.

---

## 8. bike-components.de (Scraping) ✅

**Ergebnis: ✅ Bereits implementiert, Phase 1 MVP**

- `robots.txt`: Keine Disallow-Regeln → Scraping explizit unproblematisch.
- Rendering: Vue.js + Inertia.js (SSR) → JSON in `data-props`-Attribut, kein Selenium nötig.
- Such-URL: `https://www.bike-components.de/de/s/?keywords={query}`
- Parsing-Aufwand: Gering. Preis als Float direkt nutzbar (`priceRaw: 19.99`).

---

## 9. bike-discount.de (Scraping) ✅

**Ergebnis: ✅ Empfohlen für Phase 2**

- `robots.txt` (Shopware): `Allow: /` — keine Sperre der Suchseite. Nur technische/interne Pfade gesperrt (checkout, account, wishlist etc.).
- Such-URL verifiziert (16.03.2026): `https://www.bike-discount.de/de/search?search={query}` liefert Ergebnisse.
- Testsuche „shimano slx kette 10-fach" erfolgreich.
- Großes Sortiment, spezialisierter Fahrradteile-Shop.
- Plattform: Shopware 6 (SSR) → Jsoup ausreichend, kein Selenium nötig.
- Parsing-Aufwand: Mittel (Shopware-spezifische HTML-Struktur, Preis als String, Regex-Parsing nötig).

**Fazit:** Bester verfügbarer zweiter Scraping-Kandidat nach bike-components.de.

### Scraping-Code (Shopware 6 SSR)

```java
// Such-URL: https://www.bike-discount.de/de/search?search={query}
Document doc = Jsoup.connect(
    "https://www.bike-discount.de/de/search?search=" + URLEncoder.encode(query, "UTF-8")
)
.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
.timeout(10_000)
.get();

// Shopware 6 Standard-Selektoren (per F12 → Elements im Browser verifizieren):
Elements products = doc.select(".product-box");

for (Element product : products) {
    String name     = product.select(".product-name").text();
    String priceStr = product.select(".product-price-info .price-unit-price").text();
    String url      = "https://www.bike-discount.de"
                    + product.select("a.product-image-link").attr("href");
    boolean inStock = !product.select(".delivery-status-indicator.is-available").isEmpty();
    BigDecimal price = parsePrice(priceStr); // "18,99 €" → BigDecimal
}
```

**Preis-Parsing** (identisch zu rosebikes.de):
```java
private BigDecimal parsePrice(String priceStr) {
    String cleaned = priceStr
        .replace("€", "").replace(".", "").replace(",", ".").trim();
    return new BigDecimal(cleaned);
}
```

> **Hinweis:** Die genauen CSS-Klassen können je nach aktivem Shopware-Theme leicht variieren. Einmalige Verifikation per F12 → Elements empfohlen.

---

## 9a. bike-discount.de Store-API (Shopware 6 REST) ❌

**Ergebnis: ❌ Nicht verwendbar — sw-access-key nicht zugänglich**

### Hintergrund

Shopware 6 stellt eine öffentliche **Store-API** (`/store-api/search`) bereit, die strukturierte JSON-Produktdaten liefern würde. Für die Nutzung sind zwei Parameter erforderlich:

| Parameter | Bedeutung |
|---|---|
| `salesChannelId` | Identifiziert den Shop-Kanal (im HTML-Quelltext auffindbar) |
| `sw-access-key` | Auth-Token für API-Anfragen (im Request-Header zwingend) |

### Analyse (16.03.2026)

- Die `salesChannelId` von bike-discount.de ist im Quelltext auffindbar: `018c7eaddde773d4ae02eb5b8686cc6a`
- Der `sw-access-key` ist jedoch **nicht im Browser exponiert**: bike-discount.de verwendet reines serverseitiges Rendering (Shopware 6 SSR mit Twig/PHP). Der Browser stellt ausschließlich Standard-GET-Requests an die Storefront — es werden **keine Store-API-Calls vom Browser aus** gemacht.
- Im Netzwerk-Tab (F12) sind bei einer Suche nur normale Storefront-HTML-Anfragen sichtbar, keine `/store-api/`-Requests.
- Der `sw-access-key` verbleibt damit serverseitig in der Shopware-Konfiguration und ist für externe Zugriffe nicht verfügbar.

### Warum salesChannelId allein nicht reicht

Die `salesChannelId` identifiziert nur *welcher* Shop-Kanal angesprochen werden soll. Der `sw-access-key` ist das eigentliche Auth-Token — ohne ihn antwortet die Store-API mit `401 Unauthorized`. Beide Parameter sind **zwingend gemeinsam** erforderlich:

```http
POST https://www.bike-discount.de/store-api/search
sw-access-key: SWSC...    ← Pflicht, nicht verfügbar
Content-Type: application/json

{
  "query": "shimano xt kette",
  "limit": 24
}
```

**Fazit:** Store-API-Zugriff nicht möglich. Der `sw-access-key` wird serverseitig gehalten und ist nicht öffentlich zugänglich. Jsoup HTML-Scraping (Abschnitt 9) ist der praktikable Weg.

---

## 10. rosebikes.de (Scraping) ⚠️

**Ergebnis: ⚠️ Nur für privaten/lokalen Betrieb**

- `robots.txt`: `Disallow: /search?*` → Suche explizit gesperrt.
- Technisch scrapbar mit Jsoup (SSR, kein Selenium nötig), aber rechtlich nicht für öffentlichen Betrieb geeignet.
- Nur für lokale Demo / Portfolio-Präsentation vertretbar.

---

## 11. Geprüfte Shops — Ausgeschlossen

| Shop | robots.txt Befund | Praxis-Test | Grund Ausschluss |
|---|---|---|---|
| fahrrad.de | `Disallow: /search` (Shopify-Standard) | — | robots.txt gesperrt |
| fahrrad-teile.shop | `Disallow: /search` (Shopify-Standard) | — | robots.txt gesperrt |
| bike24.de | `Disallow: /search?*`, `/suche?*` | — | robots.txt gesperrt |
| fahrrad-xxl.de | `Disallow: /suche/` | Such-URL `/suche/?q=...` funktioniert, aber Sortiment unzureichend (kein 10-fach-Material) | Schlechtes Sortiment |
| raddiscount.de | `Disallow: /cgi-bin/search/` | Such-URL läuft über `/cgi-bin/search/search.php` → CGI aus den 2000ern, keine Ergebnisse | Kein Sortiment, veraltete Technik |
| rad24.de | — | Kein SSL | Unsichere Verbindung, Jsoup nicht verwendbar |
| bikester.de | — | Domain existiert nicht mehr | — |
| 2rad-kreis.de | — | Domain existiert nicht mehr | — |

**Hinweis Shopify-Muster:** Alle Shopify-Shops sperren `/search` standardmäßig in der robots.txt. Das betrifft fahrrad.de, fahrrad-teile.shop und viele weitere. Eine manuelle Prüfung lohnt sich nur bei Shops auf anderen Plattformen (Shopware, WooCommerce, Magento, eigene Systeme).

---

## Finale Entscheidung für BikePartsFinder

| Phase | Quelle | Such-URL | Status |
|---|---|---|---|
| Phase 1 MVP | bike-components.de | `https://www.bike-components.de/de/s/?keywords={query}` | ✅ Fertig implementiert |
| Phase 2 | bike-discount.de | `https://www.bike-discount.de/de/search?search={query}` | ⭐ Nächster Kandidat |
| Phase 2 lokal | rosebikes.de | `https://www.rosebikes.de/search?q={query}` | ⚠️ Nur privat/lokal |
| Optional | eBay Browse API | Sandbox: sofort; Production: EPN-Antrag nötig | ⚠️ Nur wenn Gebrauchtteile gewünscht |
| Alle Phasen | Amazon, idealo, geizhals, billiger.de, Brave, bike24, fahrrad.de, raddiscount | — | ❌ Nicht verwenden |
