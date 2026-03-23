package com.bikeparts.price.service;

import com.bikeparts.price.ScrapingConstants;
import com.bikeparts.price.ScrapingUtils;
import com.bikeparts.price.entity.ProductOffer;
import com.bikeparts.price.enums.FetchMethod;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scraper-Service für den Online-Shop <a href="https://www.bike-discount.de">bike-discount.de</a>.
 *
 * <h2>Technischer Hintergrund</h2>
 * <p>bike-discount.de basiert auf <strong>Shopware 6</strong> und liefert vollständig
 * server-seitig gerendertes HTML. Die Produktdaten sind direkt im DOM enthalten -
 * kein {@code data-props}-JSON, kein JavaScript-Rendering erforderlich.
 * Jsoup ist für HTTP-Request und DOM-Parsing ausreichend.</p>
 *
 * <h2>Seitenstruktur (Suchergebnisseite)</h2>
 * <pre>
 * div.row.cms-listing-row[data-aria-live-text="Es werden N Produkte angezeigt."]
 *   div.cms-listing-col[role="listitem"]           (ein Eintrag pro Produkt)
 *     div.card.product-box
 *       div.product-title
 *         a[href="https://..."]                    (absoluter Produkt-Link)
 *           b   Marke (z. B. "Shimano")
 *           br/
 *               Modell (z. B. "SLX CN-M7100 12-fach Kette")
 *       span.product-price                         (ownText = "24,99 €")
 *         span.list-price > span.list-price-price  (UVP, optional)
 *       div.product-action
 *         form.buy-widget[data-add-to-cart="true"]   (direkt kaufbar = auf Lager)
 *         ODER a.product-button-detail               (Varianten-Produkt = ebenfalls auf Lager)
 * </pre>
 *
 * <h2>Preisparser</h2>
 * <p>{@code "89,99 €"} -> {@link #parsePrice(String)} -> {@code new BigDecimal("89.99")}.</p>
 * <p>{@link Element#ownText()} auf {@code span.product-price} liefert nur den
 * eigentlichen Preis, nicht den verschachtelten UVP-Text.</p>
 *
 * <h2>robots.txt</h2>
 * <p>Die robots.txt von bike-discount.de enthält keine Disallow-Regeln für die
 * Suchseite {@code /de/search?search=}. Scraping ist damit zulässig.</p>
 *
 * <h2>Caching</h2>
 * <p>Suchergebnisse werden mit {@code @Cacheable("bikeDiscountSearch")} gecacht,
 * um wiederholte identische Anfragen zu vermeiden.</p>
 *
 * @see BikeDiscountShippingCostScraperService
 * @see ProductOffer
 */
@Slf4j
@Service
public class BikeDiscountScraperService implements ScraperShopInterface {

    /** Regex zum Extrahieren der Trefferanzahl aus {@code data-aria-live-text}. */
    private static final Pattern TOTAL_PATTERN = Pattern.compile("\\d+");

    /**
     * Schnelltest-Einstiegspunkt zum manuellen Ausführen des Scrapers
     * außerhalb des Spring-Kontexts (z. B. in der IDE direkt starten).
     *
     * @param args Kommandozeilenargumente (werden nicht ausgewertet).
     */
    public static void main(String[] args) {
        BikeDiscountScraperService service = new BikeDiscountScraperService();
        ScrapingResult result = service.search("shimano slx kette 10-fach");
        result.offers().forEach(System.out::println);
    }

    /**
     * Führt eine Produktsuche auf bike-discount.de durch und gibt die ersten
     * {@link ScrapingConstants.Common#MAX_NUMBER_PRODUCT_OFFERS} passenden Treffer zurück.
     *
     * <p>Ablauf:</p>
     * <ol>
     *   <li>Suchbegriff URL-kodieren und an {@link ScrapingConstants.BikeDiscount#SEARCH_URL}
     *       anhängen</li>
     *   <li>HTTP-GET via Jsoup mit {@link ScrapingConstants.Common#USER_AGENT} und 10 s
     *       Timeout</li>
     *   <li>HTML-Dokument an {@link #parseDocument(Document, String)} delegieren</li>
     *   <li>Bei Fehler: {@link ScrapingResult#error(String)} zurückgeben</li>
     * </ol>
     *
     * <p>Das Ergebnis wird pro Query gecacht. Ein erneuter Aufruf mit demselben
     * Suchbegriff löst keinen neuen HTTP-Request aus.</p>
     *
     * @param searchQuery Suchbegriff, z. B. {@code "shimano slx kette 10-fach"}.
     * @return {@link ScrapingResult} mit gefundenen {@link ProductOffer}s oder Fehlerstatus.
     */
    @Cacheable(value = "bikeDiscountSearch", key = "#searchQuery")
    public ScrapingResult search(String searchQuery) {
        String url = ScrapingConstants.BikeDiscount.SEARCH_URL
                + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
        log.debug("Scraping bike-discount.de: {}", url);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(ScrapingConstants.Common.USER_AGENT)
                    .timeout(10_000)
                    .get();
            return parseDocument(doc, searchQuery);
        } catch (Exception e) {
            log.error("Fehler beim Scraping von bike-discount.de für Query '{}': {}",
                    searchQuery, e.getMessage());
            return ScrapingResult.error(e.getMessage(), ScrapingConstants.BikeDiscount.SHOP_NAME);
        }
    }

    /**
     * Parst ein bereits geladenes Jsoup-{@link Document} und extrahiert
     * Produkte aus den {@code div.cms-listing-col[role=listitem]}-Elementen.
     *
     * <p>Diese Methode ist <strong>package-private</strong>, um sie in Unit-Tests
     * direkt mit einem aus einer Testdatei geparsten Dokument aufrufen zu können -
     * ohne einen echten HTTP-Request durchzuführen.</p>
     *
     * <p>Ablauf:</p>
     * <ol>
     *   <li>{@code div.row.cms-listing-row} selektieren; fehlt das Element, wird
     *       {@link ScrapingResult#error(String)} zurückgegeben</li>
     *   <li>Alle {@code div.cms-listing-col[role=listitem]} iterieren</li>
     *   <li>Produktname aus {@code div.product-title a} lesen;
     *       Produkte, die nicht alle Suchbegriffe enthalten, werden übersprungen
     *       ({@link ScrapingUtils#containsAllTerms(String, String)})</li>
     *   <li>Jedes passende Produkt via {@link #mapToDto(Element, String)} in ein
     *       {@link ProductOffer} überführen</li>
     *   <li>Abbruch nach {@link ScrapingConstants.Common#MAX_NUMBER_PRODUCT_OFFERS}
     *       gesammelten Treffern</li>
     * </ol>
     *
     * @param doc         Das von Jsoup geparste HTML-Dokument der Suchergebnisseite.
     * @param searchQuery Suchbegriff für Filterung und {@link ProductOffer#getSearchQuery()}.
     * @return {@link ScrapingResult} mit extrahierten {@link ProductOffer}s oder Fehlerstatus.
     */
    ScrapingResult parseDocument(Document doc, String searchQuery) {
        Element listingRow = doc.selectFirst("div.row.cms-listing-row");
        if (listingRow == null) {
            log.warn("Seitenstruktur geändert? Element 'div.row.cms-listing-row' nicht gefunden");
            return ScrapingResult.error("Listing-Container nicht gefunden", ScrapingConstants.BikeDiscount.SHOP_NAME);
        }

        int total = parseTotal(listingRow.attr("data-aria-live-text"));

        Elements listItems = listingRow.select("div.cms-listing-col[role=listitem]");
        if (listItems.isEmpty()) {
            log.warn("bike-discount.de: Keine Produkte gefunden für Query '{}'", searchQuery);
            return ScrapingResult.noResults(ScrapingConstants.BikeDiscount.SHOP_NAME);
        }

        List<ProductOffer> result = new ArrayList<>();
        for (Element item : listItems) {
            if (result.size() >= ScrapingConstants.Common.MAX_NUMBER_PRODUCT_OFFERS) break;

            String productName = item.select("div.product-title a").text();
            if (ScrapingUtils.containsAllTerms(searchQuery, productName)) {
                result.add(mapToDto(item, searchQuery));
            } else {
                log.debug("Produkt herausgefiltert (nicht alle Suchbegriffe enthalten): {}",
                        productName);
            }
        }

        log.debug("bike-discount.de: {} Treffer gesamt, {} übernommen (max {})",
                total, result.size(), ScrapingConstants.Common.MAX_NUMBER_PRODUCT_OFFERS);
        result.forEach(offer -> log.debug("ProductOffer: {}", offer));
        return ScrapingResult.success(result, ScrapingConstants.BikeDiscount.SHOP_NAME);
    }

    /**
     * Überführt ein einzelnes Produkt-DOM-Element ({@code div.cms-listing-col})
     * in ein {@link ProductOffer}.
     *
     * <p>Mapping-Regeln:</p>
     * <ul>
     *   <li>{@code productName} <- {@code div.product-title a} (.text() liefert
     *       "Marke Modell", z. B. {@code "Shimano SLX CN-M7100 12-fach Kette"})</li>
     *   <li>{@code productUrl}  <- {@code div.product-title a[href]} (absolute URL)</li>
     *   <li>{@code price}       <- {@code span.product-price} (.ownText(), z. B. {@code "89,99 €"});
     *       {@code null} bei leerem oder nicht parsebarem Preistext</li>
     *   <li>{@code inStock}     <- {@code form.buy-widget} vorhanden (direkt kaufbar)
     *       ODER {@code a.product-button-detail} vorhanden (Varianten-Produkt, ebenfalls verfügbar)</li>
     *   <li>{@code shopName}    <- {@link ScrapingConstants.BikeDiscount#SHOP_NAME}</li>
     *   <li>{@code shopId}      <- {@link ScrapingConstants.BikeDiscount#SHOP_ID}</li>
     *   <li>{@code source}      <- immer {@link FetchMethod#WEB_SCRAPING}</li>
     *   <li>{@code fetchedAt}   <- {@link LocalDateTime#now()}</li>
     * </ul>
     *
     * @param item        Jsoup-Element {@code div.cms-listing-col[role=listitem]}.
     * @param searchQuery Suchbegriff, der dem {@link ProductOffer} zugeordnet wird.
     * @return Befülltes {@link ProductOffer}.
     */
    private ProductOffer mapToDto(Element item, String searchQuery) {
        String productName = item.select("div.product-title a").text();
        String productUrl  = item.select("div.product-title a").attr("href");

        Element priceEl = item.selectFirst("span.product-price");
        BigDecimal price = null;
        if (priceEl != null) {
            String priceText = priceEl.ownText().trim();
            if (!priceText.isEmpty()) {
                try {
                    price = parsePrice(priceText);
                } catch (NumberFormatException e) {
                    log.warn("Preis konnte nicht geparst werden: '{}' (Produkt: {})",
                            priceText, productName);
                }
            }
        }

        boolean inStock = item.selectFirst("form.buy-widget") != null
                || item.selectFirst("a.product-button-detail") != null;

        if (productName.isBlank()) {
            log.warn("Seitenstruktur geändert? Produktname leer (URL: {})", productUrl);
        }
        if (inStock && price == null) {
            log.warn("Seitenstruktur geändert? Kein Preis bei kaufbarem Produkt: {}", productName);
        }

        return ProductOffer.builder()
                .productName(productName)
                .price(price)
                .productUrl(productUrl)
                .inStock(inStock)
                .shopName(ScrapingConstants.BikeDiscount.SHOP_NAME)
                .source(FetchMethod.WEB_SCRAPING)
                .fetchedAt(LocalDateTime.now())
                .searchQuery(searchQuery)
                .build();
    }

    /**
     * Extrahiert die Gesamtanzahl der Treffer aus dem {@code data-aria-live-text}-Attribut.
     *
     * <p>Beispiel: {@code "Es werden 48 Produkte angezeigt."} -> {@code 48}</p>
     *
     * @param ariaText Inhalt des {@code data-aria-live-text}-Attributs.
     * @return Erste gefundene Zahl, oder {@code 0} wenn kein Match.
     */
    private int parseTotal(String ariaText) {
        Matcher m = TOTAL_PATTERN.matcher(ariaText);
        return m.find() ? Integer.parseInt(m.group()) : 0;
    }

    /**
     * Konvertiert einen lokalisierten Preisstring in einen {@link BigDecimal}.
     *
     * <p>Transformationsschritte:</p>
     * <ol>
     *   <li>{@code "€"} und Whitespace entfernen</li>
     *   <li>Tausenderpunkt {@code "."} entfernen (z. B. {@code "1.000"})</li>
     *   <li>Dezimalkomma {@code ","} durch {@code "."} ersetzen</li>
     *   <li>String als {@link BigDecimal} parsen</li>
     * </ol>
     *
     * <p>Beispiel: {@code "89,99 €"} -> {@code new BigDecimal("89.99")}</p>
     *
     * @param priceText Preisstring im deutschen Format, z. B. {@code "89,99 €"}.
     * @return Exakter Dezimalwert als {@link BigDecimal}.
     * @throws NumberFormatException wenn der bereinigte String kein gültiges Dezimalformat hat.
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
