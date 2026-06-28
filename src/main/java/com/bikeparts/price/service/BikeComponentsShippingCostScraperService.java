package com.bikeparts.price.service;

import com.bikeparts.price.ScrapingConstants;
import com.bikeparts.price.enums.FetchMethod;
import com.bikeparts.price.entity.ShopInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Scraper-Service zum Abrufen der Standard-Versandkosten für Deutschland
 * von <a href="https://www.bike-components.de/de/service/versand/">bike-components.de/service/versand</a>.
 *
 * <p>Das Ergebnis wird als vollständiges {@link ShopInfo}-Objekt zurückgegeben,
 * das neben dem Preis auch Shop-Metadaten, Quelle und Abfragezeitpunkt enthält.
 * {@link ShopInfo#getFreeShippingOnOrdersOver()} wird von diesem Service nicht befüllt
 * (bleibt {@code null}), da die Seite keinen entsprechenden Wert in Tabellenform
 * enthält.</p>
 *
 * <h2>Seitenstruktur</h2>
 * <p>Die Versandkostenseite enthält eine HTML-Tabelle mit folgenden Spalten:</p>
 * <pre>
 * | Land          | Standard | Sperrgut¹ | Spedition² |
 * |---------------|----------|-----------|------------|
 * | Deutschland   | 4,99€    | 9,99€     | 49,99€     |
 * | Österreich    | ...      | ...       | ...        |
 * </pre>
 * <p>Dieser Service liest gezielt den <strong>Standard-Versandpreis</strong> für
 * die Zeile „Deutschland" (Spaltenindex 1).</p>
 *
 * <h2>Caching</h2>
 * <p>Das Ergebnis wird dauerhaft gecacht ({@code @Cacheable("shippingCostsBikeComponents")}),
 * da sich Versandkosten selten ändern. Der Cache wird erst bei einem
 * Neustart der Anwendung geleert.</p>
 *
 * <h2>Fehlerverhalten</h2>
 * <p>Bei einem HTTP-Fehler, Timeout oder fehlendem Tabellenelement gibt die Methode
 * {@code null} zurück und loggt den Fehler. Die aufrufende Schicht muss
 * {@code null} entsprechend behandeln.</p>
 *
 * @see BikeComponentsScraperService
 * @see ShopInfo
 */
@Slf4j
@Service
public class BikeComponentsShippingCostScraperService {

    /**
     * Schnelltest-Einstiegspunkt zum manuellen Ausführen des Scrapers
     * außerhalb des Spring-Kontexts (z. B. in der IDE direkt starten).
     *
     * @param args Kommandozeilenargumente (werden nicht ausgewertet).
     */
    public static void main(String[] args) {
        ObjectMapper objectMapper1 = new ObjectMapper();
        BikeComponentsShippingCostScraperService bikeComponentsShippingCostScraperService
                = new BikeComponentsShippingCostScraperService();
        bikeComponentsShippingCostScraperService.getStandardShippingCostForGermany();
    }

    /**
     * Ruft den Standard-Versandpreis für Deutschland von der Versandkostenseite ab
     * und gibt ihn als {@link ShopInfo} zurück.
     *
     * <p>Ablauf:</p>
     * <ol>
     *   <li>HTTP-GET via Jsoup auf die Versandkostenseite (10 s Timeout)</li>
     *   <li>Das geladene HTML-Dokument wird an {@link #parseDocument(Document)} delegiert</li>
     *   <li>Bei Fehler: {@code null} zurückgeben und Fehler loggen</li>
     * </ol>
     *
     * <p>Das Ergebnis wird gecacht. Wiederholte Aufrufe liefern den zwischengespeicherten
     * Wert, ohne einen erneuten HTTP-Request auszulösen.</p>
     *
     * @return {@link ShopInfo} mit Versandkosten und Shop-Metadaten,
     *         oder {@code null} bei Fehler.
     */
    @Cacheable("shippingCostsBikeComponents")
    public ShopInfo getStandardShippingCostForGermany() {
        log.info("Scraping Versandkosten von: {}", ScrapingConstants.BikeComponents.SHIPPING_URL);
        try {
            Document doc = Jsoup.connect(ScrapingConstants.BikeComponents.SHIPPING_URL)
                    .userAgent(ScrapingConstants.Common.USER_AGENT)
                    .timeout(10_000)
                    .get();

            return parseDocument(doc);
        } catch (Exception e) {
            log.error("Fehler beim Scraping der Versandkosten: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parst ein bereits geladenes Jsoup-{@link Document}, sucht die Deutschland-Zeile
     * in der Versandkostentabelle und gibt ein befülltes {@link ShopInfo}-Objekt zurück.
     *
     * <p>Diese Methode ist <strong>package-private</strong>, um sie in Unit-Tests
     * direkt mit einem aus einer Testdatei geparsten Dokument aufrufen zu können -
     * ohne einen echten HTTP-Request durchzuführen.</p>
     *
     * <p>Ablauf:</p>
     * <ol>
     *   <li>Alle {@code <tr>}-Elemente der Tabelle selektieren</li>
     *   <li>Zeilen ohne {@code <td>}-Zellen überspringen (z. B. Header-Zeilen mit {@code <th>})</li>
     *   <li>Erste Zelle (Index 0) auf {@link ScrapingConstants.Common#COUNTRY_GERMANY} prüfen</li>
     *   <li>Zweite Zelle (Index 1) enthält den Standard-Versandpreis -> {@link #parsePrice(String)}</li>
     *   <li>Ergebnis in ein {@link ShopInfo}-Objekt verpacken</li>
     * </ol>
     *
     * <p>{@link ShopInfo#getFreeShippingOnOrdersOver()} wird nicht gesetzt und bleibt
     * {@code null}, da dieser Wert nicht aus der Tabellenstruktur herausgelesen wird.</p>
     *
     * @param doc Das von Jsoup geparste HTML-Dokument der Versandkostenseite.
     * @return {@link ShopInfo} mit gescrapten Versandkosten und Metadaten,
     *         oder {@code null} wenn keine passende Tabellenzeile gefunden wurde.
     */
    ShopInfo parseDocument(Document doc) {
        Elements rows = doc.select("table tr");

        for (Element row : rows) {
            Elements cells = row.select("td");
            if (cells.isEmpty()) {
                continue;
            }
            String country = cells.get(0).text().trim();
            if (country.contains(ScrapingConstants.Common.COUNTRY_GERMANY) && cells.size() >= 2) {
                String priceText = cells.get(1).text().trim();
                BigDecimal shippingCost = parsePrice(priceText);
                log.info("Versandkosten Deutschland Standard: {}", shippingCost);
                return ShopInfo.builder()
                        .shopName(ScrapingConstants.BikeComponents.SHOP_NAME)
                        .shippingCostUrl(ScrapingConstants.BikeComponents.SHIPPING_URL)
                        .shippingCost(shippingCost)
                        .source(FetchMethod.WEB_SCRAPING)
                        .fetchedAt(LocalDateTime.now())
                        .build();
            }
        }

        log.warn("Keine Versandkosten für '{}' gefunden", ScrapingConstants.Common.COUNTRY_GERMANY);
        return null;
    }

    /**
     * Konvertiert einen lokalisierten Preisstring in einen {@link BigDecimal}.
     *
     * <p>Transformationsschritte:</p>
     * <ol>
     *   <li>{@code "€"} entfernen</li>
     *   <li>Tausenderpunkt {@code "."} entfernen (z. B. {@code "1.000"})</li>
     *   <li>Dezimalkomma {@code ","} durch Punkt {@code "."} ersetzen</li>
     *   <li>Whitespace trimmen</li>
     *   <li>String als {@link BigDecimal} parsen</li>
     * </ol>
     *
     * <p>Beispiel: {@code "4,99€"} -> {@code new BigDecimal("4.99")}</p>
     *
     * @param priceText Preisstring im deutschen Format, z. B. {@code "4,99€"}.
     * @return Exakter Dezimalwert als {@link BigDecimal}.
     * @throws NumberFormatException wenn der bereinigte String kein gültiges
     *                               Dezimalformat hat.
     */
    private BigDecimal parsePrice(String priceText) {
        String cleaned = priceText
                .replace("€", "")
                .replace(".", "")
                .replace(",", ".")
                .trim();
        return new BigDecimal(cleaned);
    }
}
