package com.bikeparts.price.service;

import com.bikeparts.price.ScrapingConstants;
import com.bikeparts.price.entity.ShopInfo;
import com.bikeparts.price.enums.FetchMethod;
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
 * von <a href="https://www.bike-discount.de/de/shippingcosts">bike-discount.de/shippingcosts</a>.
 *
 * <p>Das Ergebnis wird als vollständiges {@link ShopInfo}-Objekt zurückgegeben,
 * das neben dem Preis auch Shop-Metadaten, Quelle und Abfragezeitpunkt enthält.
 * {@link ShopInfo#getFreeShippingOnOrdersOver()} wird aus dem Feld „Frei ab"
 * der Versandkostentabelle befüllt.</p>
 *
 * <h2>Seitenstruktur (Versandkostenseite)</h2>
 * <p>Die Versandkostenseite verwendet <strong>keine HTML-Tabelle</strong>, sondern
 * ein Dropdown mit Länder-Auswahl. Jedes Land ist in einem eigenen Block enthalten:</p>
 * <pre>
 * div.shipping-costs-info__details[style="display: block;"]   (Deutschland = sichtbar)
 *   h4   "Deutschland:"
 *   dl
 *     dt   "Versandkosten"   dd   "4,49 €"
 *     dt   "Frei ab "        dd   "98,99 €"
 *     dt   "pro Sperrgut"    dd   "14,99 €"
 *     ...
 * </pre>
 * <p>Gescrapte Werte für Deutschland (Stand 03/2026):</p>
 * <ul>
 *   <li>Versandkosten: {@code 4,49 €}</li>
 *   <li>Kostenloser Versand ab: {@code 98,99 €}</li>
 * </ul>
 *
 * <h2>Caching</h2>
 * <p>Das Ergebnis wird gecacht ({@code @Cacheable("shippingCostsBikeDiscount")}),
 * da sich Versandkosten selten ändern. Der Cache wird erst bei einem
 * Neustart der Anwendung geleert.</p>
 *
 * <h2>Fehlerverhalten</h2>
 * <p>Bei einem HTTP-Fehler, Timeout oder fehlendem DOM-Element gibt die Methode
 * {@code null} zurück und loggt den Fehler. Die aufrufende Schicht muss
 * {@code null} entsprechend behandeln.</p>
 *
 * @see BikeDiscountScraperService
 * @see ShopInfo
 */
@Slf4j
@Service
public class BikeDiscountShippingCostScraperService {

    /**
     * Schnelltest-Einstiegspunkt zum manuellen Ausführen des Scrapers
     * außerhalb des Spring-Kontexts (z. B. in der IDE direkt starten).
     *
     * @param args Kommandozeilenargumente (werden nicht ausgewertet).
     */
    public static void main(String[] args) {
        BikeDiscountShippingCostScraperService service = new BikeDiscountShippingCostScraperService();
        ShopInfo result = service.getStandardShippingCostForGermany();
        System.out.println(result);
    }

    /**
     * Ruft den Standard-Versandpreis und den Gratisvesand-Schwellwert für Deutschland
     * von der Versandkostenseite ab und gibt sie als {@link ShopInfo} zurück.
     *
     * <p>Ablauf:</p>
     * <ol>
     *   <li>HTTP-GET auf {@link ScrapingConstants.BikeDiscount#SHIPPING_URL} via Jsoup mit
     *       {@link ScrapingConstants.Common#USER_AGENT} und 10 s Timeout</li>
     *   <li>Das geladene HTML-Dokument wird an {@link #parseDocument(Document)}
     *       delegiert</li>
     *   <li>Bei Fehler: {@code null} zurückgeben und Fehler loggen</li>
     * </ol>
     *
     * <p>Das Ergebnis wird gecacht. Wiederholte Aufrufe liefern den zwischengespeicherten
     * Wert, ohne einen erneuten HTTP-Request auszulösen.</p>
     *
     * @return {@link ShopInfo} mit Versandkosten und Shop-Metadaten,
     *         oder {@code null} bei Fehler.
     */
    @Cacheable("shippingCostsBikeDiscount")
    public ShopInfo getStandardShippingCostForGermany() {
        log.info("Scraping Versandkosten von: {}", ScrapingConstants.BikeDiscount.SHIPPING_URL);
        try {
            Document doc = Jsoup.connect(ScrapingConstants.BikeDiscount.SHIPPING_URL)
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
     * Parst ein bereits geladenes Jsoup-{@link Document}, sucht den Deutschland-Block
     * in der Versandkosten-Übersicht und gibt ein befülltes {@link ShopInfo}-Objekt zurück.
     *
     * <p>Diese Methode ist <strong>package-private</strong>, um sie in Unit-Tests
     * direkt mit einem aus einer Testdatei geparsten Dokument aufrufen zu können -
     * ohne einen echten HTTP-Request durchzuführen.</p>
     *
     * <p>Ablauf:</p>
     * <ol>
     *   <li>Alle {@code div.shipping-costs-info__details} selektieren</li>
     *   <li>Den Block finden, dessen {@code h4} den Text
     *       {@link ScrapingConstants.Common#COUNTRY_GERMANY} enthält</li>
     *   <li>Im zugehörigen {@code dl} die {@code dt}-Elemente iterieren:
     *       <ul>
     *         <li>{@code dt} = "Versandkosten" -> nächstes {@code dd} = {@code shippingCost}</li>
     *         <li>{@code dt} starts with "Frei ab" -> nächstes {@code dd} =
     *             {@code freeShippingOnOrdersOver}</li>
     *       </ul>
     *   </li>
     *   <li>Ergebnis in ein {@link ShopInfo}-Objekt verpacken</li>
     * </ol>
     *
     * @param doc Das von Jsoup geparste HTML-Dokument der Versandkostenseite.
     * @return {@link ShopInfo} mit gescrapten Versandkosten und Metadaten,
     *         oder {@code null} wenn kein Deutschland-Block gefunden wurde.
     */
    ShopInfo parseDocument(Document doc) {
        for (Element details : doc.select("div.shipping-costs-info__details")) {
            Element h4 = details.selectFirst("h4");
            if (h4 == null || !h4.text().contains(ScrapingConstants.Common.COUNTRY_GERMANY)) {
                continue;
            }

            Element dl = details.selectFirst("dl");
            if (dl == null) {
                log.warn("Deutschland-Block gefunden, aber kein <dl>-Element vorhanden");
                return null;
            }

            BigDecimal shippingCost = null;
            BigDecimal freeShippingOnOrdersOver = null;

            for (Element dt : dl.select("dt")) {
                String label = dt.text().trim();
                Element dd = dt.nextElementSibling();
                if (dd == null || !dd.tagName().equals("dd")) continue;

                String value = dd.text().trim();
                if (label.equalsIgnoreCase("Versandkosten")) {
                    shippingCost = parsePrice(value);
                    log.info("Versandkosten Deutschland Standard: {}", shippingCost);
                } else if (label.startsWith("Frei ab")) {
                    freeShippingOnOrdersOver = parsePrice(value);
                    log.info("Kostenloser Versand ab: {}", freeShippingOnOrdersOver);
                }
            }

            return ShopInfo.builder()
                    .id(ScrapingConstants.BikeDiscount.SHOP_ID)
                    .shopName(ScrapingConstants.BikeDiscount.SHOP_NAME)
                    .shippingCostUrl(ScrapingConstants.BikeDiscount.SHIPPING_URL)
                    .shippingCost(shippingCost)
                    .freeShippingOnOrdersOver(freeShippingOnOrdersOver)
                    .source(FetchMethod.WEB_SCRAPING)
                    .fetchedAt(LocalDateTime.now())
                    .build();
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
     * <p>Beispiel: {@code "4,49 €"} -> {@code new BigDecimal("4.49")}</p>
     *
     * @param priceText Preisstring im deutschen Format, z. B. {@code "4,49 €"}.
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
