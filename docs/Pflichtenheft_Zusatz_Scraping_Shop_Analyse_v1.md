# Web-Scraping Shop-Analyse - BikePartsFinder

**Stand:** 03.03.2026

Beide Ziel-Shops verwenden Server-Side Rendering (SSR). Alle Produktdaten sind im
statischen HTML-Response enthalten. Kein JavaScript-Rendering (Selenium / Playwright)
nötig - Jsoup ist ausreichend.

---

## Übersicht

| Merkmal | bike-components.de | rosebikes.de |
|---|---|---|
| **robots.txt - Suchseite** | Kein Disallow | `Disallow: /search?*` |
| **Rendering** | Vue.js + Inertia.js (SSR) | Custom SSR |
| **Datenformat** | JSON in `data-props`-Attribut | GTM DataLayer JSON + CSS-Selektoren |
| **Preis** | `priceRaw: 19.99` (Float, direkt nutzbar) | String `"22,95 €"` (Regex-Parsing nötig) |
| **Streichpreis / UVP** | `strikeThroughPrice` vorhanden | Im HTML vorhanden |
| **Rabatt** | `percentageDiscount` (Integer, z. B. 49) | Im HTML vorhanden |
| **Verfügbarkeit** | Boolean-Flags: `isBuyable`, `isSoldOut`, `isQuicklyOutOfStock` | CSS-Klassen |
| **Bewertungen** | `reviewStars` (0-5), `reviewsCount` | Im HTML vorhanden |
| **Paginierung** | `total` (Gesamttreffer), 24 pro Seite | Vorhanden |
| **Parsing-Aufwand** | Gering | Mittel |
| **Selenium nötig?** | Nein | Nein |
| **Implementierungsreihenfolge** | **Phase 1 - MVP** | **Phase 2 - nur für Privatnutzung** |

---

## bike-components.de

### robots.txt-Bewertung

```
User-agent: *
(keine Disallow-Regeln)
```

Keine einzige Einschränkung. Die Suchseite `/de/s/?keywords=` ist explizit erlaubt.
**Fazit: Scraping unproblematisch.**

### Such-URL

```
https://www.bike-components.de/de/s/?keywords={urlEncodedQuery}
```

### Technische Details

Das vollständige Produkt-JSON befindet sich im `data-props`-Attribut des
`ProductCatalog`-Elements direkt im statischen HTML - kein JavaScript-Rendering nötig.

```java
Document doc = Jsoup.connect(
    "https://www.bike-components.de/de/s/?keywords=" + URLEncoder.encode(query, "UTF-8")
).userAgent("BikePartsFinder/1.0").get();

Element catalog = doc.selectFirst("[data-component='ProductCatalog']");
JsonNode root = objectMapper.readTree(catalog.attr("data-props"));
JsonNode products = root.path("initialData").path("products");

for (JsonNode product : products) {
    String name     = product.path("data").path("productName").asText();
    double price    = product.path("data").path("priceRaw").asDouble();
    String url      = "https://www.bike-components.de"
                      + product.path("data").path("link").asText();
    boolean inStock = !product.path("data").path("isSoldOut").asBoolean();
    double stars    = product.path("data").path("reviewStars").asDouble();
}
```

### Verfügbare JSON-Felder je Produkt

| Feld | Typ | Beispiel | Beschreibung |
|---|---|---|---|
| `productName` | String | "Shimano XT CN-HG95 10-fach" | Vollständiger Produktname |
| `priceRaw` | Double | 19.99 | Preis als Float → direkt als BigDecimal nutzbar |
| `strikeThroughPrice` | String | "91,95€" | UVP / Streichpreis |
| `percentageDiscount` | Integer | 49 | Rabatt in % |
| `link` | String | "/de/Shimano/.../p35948/" | Relativer Link → Basis-URL ergänzen |
| `reviewStars` | Double | 4.5 | Bewertung 0-5 |
| `reviewsCount` | Integer | 8 | Anzahl Bewertungen |
| `isBuyable` | Boolean | true | Bestellbar |
| `isSoldOut` | Boolean | false | Ausverkauft |
| `isOffer` | Boolean | true | Sonderangebot |
| `isNew` | Boolean | false | Neuheit |
| `isQuicklyOutOfStock` | Boolean | true | Schnell vergriffen |
| `total` (Root) | Integer | 124 | Gesamttreffer der Suche |

---

## rosebikes.de

### robots.txt-Bewertung

```
User-agent: *
Disallow: /search?*
```

Die Suchseite ist explizit gesperrt. Die robots.txt ist kein Gesetz,
aber ein klares Signal des Betreibers.

**Fazit:**
- Privater Einsatz (lokale Demo, Portfolio ohne öffentlichen Betrieb): vertretbar,
  da kein kommerzieller Schaden entsteht und keine Daten weitergegeben werden
- Öffentlicher / kommerzieller Betrieb: nicht empfohlen - Abmahnrisiko,
  Verstoß gegen Nutzungsbedingungen

> **Hinweis:** Für das Portfolio-Projekt bleibt rosebikes.de als interne Demo-Quelle
> nutzbar, solange die Anwendung nicht öffentlich betrieben oder kommerziell
> eingesetzt wird.

### Such-URL

```
https://www.rosebikes.de/search?q={urlEncodedQuery}
```

### Technische Details

Produktdaten primär als JSON im GTM DataLayer `<script>`-Block;
CSS-Selektoren als Fallback.

```java
Document doc = Jsoup.connect(
    "https://www.rosebikes.de/search?q=" + URLEncoder.encode(query, "UTF-8")
).userAgent("BikePartsFinder/1.0").get();

// Primär: JSON aus GTM DataLayer <script>-Block
Elements scripts = doc.select("script:containsData(dataLayer)");
// JSON parsen und Produktliste extrahieren

// Fallback: CSS-Selektoren
Elements products = doc.select(".product-tile");
for (Element product : products) {
    String name  = product.select(".product-tile__title").text();
    String price = product.select(".product-tile__price").text(); // "22,95 €" -> Regex
    String url   = "https://www.rosebikes.de" + product.select("a").attr("href");
}
```

### Preisstring parsen

Da rosebikes.de den Preis als String liefert (`"22,95 €"`), ist ein Parsing-Schritt nötig:

```java
private BigDecimal parsePrice(String priceString) {
    // "22,95 €" → "22.95" → BigDecimal
    String cleaned = priceString
        .replace("€", "")
        .replace(".", "")   // Tausenderpunkt entfernen
        .replace(",", ".")  // Dezimalkomma → Punkt
        .trim();
    return new BigDecimal(cleaned);
}
```

---

## Risiken & Mitigation (beide Shops)

| Risiko | Mitigation |
|---|---|
| HTML-Struktur ändert sich | JSON-Fallback auf CSS-Selektoren; Fehler per AOP-Logging erfassen |
| Rate Limiting | `@Cacheable` (1-24 h), Delays zwischen Requests, `User-Agent` setzen |
| rosebikes.de - robots.txt | Nur privat / lokal betreiben; nicht öffentlich deployen |
