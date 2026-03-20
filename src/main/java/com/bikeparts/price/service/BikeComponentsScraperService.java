package com.bikeparts.price.service;

import com.bikeparts.price.ScrapingUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bikeparts.price.ScrapingConstants;
import com.bikeparts.price.enums.FetchMethod;
import com.bikeparts.price.entity.ProductOffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Scraper-Service für den Online-Shop <a href="https://www.bike-components.de">bike-components.de</a>.
 *
 * <h2>Technischer Hintergrund</h2>
 * <p>bike-components.de verwendet Server-Side Rendering (SSR) mit Vue.js und Inertia.js.
 * Alle Produktdaten der Suchergebnisseite sind als JSON im HTML-Attribut
 * {@code data-props} des Elements {@code <div data-component="ProductCatalog">} enthalten.
 * Ein JavaScript-Rendering (z. B. Selenium) ist daher <strong>nicht erforderlich</strong> -
 * Jsoup ist für den HTTP-Request und das DOM-Parsing ausreichend.</p>
 *
 * <h2>robots.txt</h2>
 * <p>Die robots.txt enthält keine Disallow-Regeln.
 * Die Suchseite {@code /de/s/?keywords=} ist explizit erlaubt.</p>
 *
 * <h2>Caching</h2>
 * <p>Suchergebnisse werden mit {@code @Cacheable} gecacht, um wiederholte
 * identische Anfragen zu vermeiden und Rate-Limiting des Shops zu reduzieren.</p>
 *
 * <h2>Paginierung</h2>
 * <p>Pro Seite liefert der Shop standardmäßig 24 Produkte. Das Feld
 * {@code initialData.total} enthält die Gesamtanzahl der Treffer.</p>
 *
 * @see BikeComponentsShippingCostScraperService
 * @see ProductOffer
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BikeComponentsScraperService {

    /**
     * Jackson {@link ObjectMapper} zum Deserialisieren des JSON aus dem
     * {@code data-props}-Attribut. Wird per Constructor Injection bereitgestellt.
     */
    private final ObjectMapper objectMapper;

    /**
     * Schnelltest-Einstiegspunkt zum manuellen Ausführen des Scrapers
     * außerhalb des Spring-Kontexts (z. B. in der IDE direkt starten).
     *
     * @param args Kommandozeilenargumente (werden nicht ausgewertet).
     */
    public static void main(String[] args) {
        ObjectMapper objectMapper1 = new ObjectMapper();
        BikeComponentsScraperService bikeComponentsScraperService = new BikeComponentsScraperService(objectMapper1);
        ScrapingResult result = bikeComponentsScraperService.search(ScrapingConstants.BikeComponents.SEARCH_URL + "shimano fahrradkette slx");
    }

    /**
     * Führt eine Produktsuche durch und gibt die ersten
     * {@link ScrapingConstants.Common#MAX_NUMBER_PRODUCT_OFFERS} Treffer zurück.
     *
     * <p>Der Ablauf:</p>
     * <ol>
     *   <li>Suchbegriff URL-kodieren und an die Such-URL anhängen</li>
     *   <li>HTTP-GET via Jsoup (10 s Timeout)</li>
     *   <li>HTML-Dokument an {@link #parseDocument(Document, String)} delegieren</li>
     *   <li>Bei Fehler (IOException, Timeout etc.): Fehlerstatus zurückgeben</li>
     * </ol>
     *
     * <p>Das Ergebnis wird pro Query gecacht ({@code @Cacheable}).
     * Ein erneuter Aufruf mit demselben Suchbegriff trifft daher den Cache
     * und löst keinen neuen HTTP-Request aus.</p>
     *
     * @param searchQuery Suchbegriff, z. B. {@code "shimano kette"}.
     *              Leerzeichen werden als {@code +} kodiert (URL-Encoding).
     * @return {@link ScrapingResult} mit gefundenen {@link ProductOffer}s, oder Fehlerstatus.
     */
    @Cacheable(value = "bikeComponentsSearch", key = "#searchQuery")
    public ScrapingResult search(String searchQuery) {
        String url = ScrapingConstants.BikeComponents.SEARCH_URL + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
        log.debug("Scraping bike-components.de: {}", url);
        log.debug("searchQuery: {}", searchQuery);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(ScrapingConstants.Common.USER_AGENT)
                    .timeout(10_000)
                    .get();
            return parseDocument(doc, searchQuery);
        } catch (Exception e) {
            log.error("Fehler beim Scraping von bike-components.de für Query '{}': {}", searchQuery, e.getMessage());
            return ScrapingResult.error(e.getMessage());
        }
    }

    /**
     * Parst ein bereits geladenes Jsoup-{@link Document} und extrahiert
     * alle Produkte aus dem {@code data-props}-JSON-Attribut.
     *
     * <p>Diese Methode ist <strong>package-private</strong>, um sie in Unit-Tests
     * direkt mit einem aus einer Testdatei geparsten Dokument aufrufen zu können -
     * ohne einen echten HTTP-Request durchzuführen.</p>
     *
     * <p>Ablauf:</p>
     * <ol>
     *   <li>CSS-Selektor {@code [data-component='ProductCatalog']} findet das
     *       Vue-Einstiegselement im HTML</li>
     *   <li>Das Attribut {@code data-props} enthält das vollständige JSON
     *       der Suchergebnisse</li>
     *   <li>Pfad im JSON: {@code initialData -> products -> [n] -> data}</li>
     *   <li>Jedes Produkt wird über {@link #mapToDto(JsonNode, String)} in ein
     *       {@link ProductOffer} überführt</li>
     * </ol>
     *
     * @param doc         Das von Jsoup geparste HTML-Dokument der Suchergebnisseite.
     * @param searchQuery Suchbegriff, der für Filterung und Mapping weitergereicht wird.
     * @return {@link ScrapingResult} mit extrahierten {@link ProductOffer}s,
     *         oder Fehlerstatus wenn das {@code ProductCatalog}-Element fehlt
     *         oder ein JSON-Fehler auftritt.
     */
    ScrapingResult parseDocument(Document doc, String searchQuery) {
        Element catalog = doc.selectFirst("[data-component='ProductCatalog']");
        if (catalog == null) {
            log.warn("ProductCatalog-Element nicht gefunden");
            return ScrapingResult.error("ProductCatalog-Element nicht gefunden");
        }
        try {
            JsonNode root = objectMapper.readTree(catalog.attr("data-props"));
            JsonNode initialData = root.path("initialData");
            if (initialData.isMissingNode()) {
                log.warn("API-Struktur geändert? Knoten 'initialData' fehlt im data-props-JSON");
                return ScrapingResult.error("API-Struktur geändert: 'initialData' fehlt");
            }
            JsonNode products = initialData.path("products");
            if (products.isMissingNode() || !products.isArray()) {
                log.warn("API-Struktur geändert? Knoten 'initialData.products' fehlt oder ist kein Array");
                return ScrapingResult.error("API-Struktur geändert: 'initialData.products' fehlt oder kein Array");
            }
            if (products.isEmpty()) {
                log.warn("bike-components.de: Keine Produkte gefunden für diese Suchanfrage");
                return ScrapingResult.noResults();
            }

            List<ProductOffer> result = new ArrayList<>();
            for (JsonNode product : products) {

                // Maximale Anzahl passender Treffer erreicht - Schleife abbrechen
                if (result.size() >= ScrapingConstants.Common.MAX_NUMBER_PRODUCT_OFFERS) break;

                String productName = product.path("data").path("productName").asText();

                // Nur Produkte uebernehmen, deren Name alle Suchbegriffe enthaelt
                if (ScrapingUtils.containsAllTerms(searchQuery, productName)) {
                    result.add(mapToDto(product.path("data"), searchQuery));
                } else {
                    log.debug("Produkt herausgefiltert (nicht alle Suchbegriffe enthalten): {}", productName);
                }
            }

            int total = root.path("initialData").path("total").asInt();
            log.debug("bike-components.de: {} Produkte gefunden (Gesamt: {}), gespeichert: {}",
                    products.size(), total, ScrapingConstants.Common.MAX_NUMBER_PRODUCT_OFFERS);
            result.forEach(offer -> log.debug("ProductOffer: {}", offer));
            return ScrapingResult.success(result);

        } catch (Exception e) {
            log.error("Fehler beim Parsen des HTML-Dokuments: {}", e.getMessage());
            return ScrapingResult.error(e.getMessage());
        }
    }


    /**
     * Überführt einen einzelnen JSON-Knoten ({@code data}-Objekt eines Produkts)
     * in ein {@link ProductOffer}.
     *
     * <p>Mapping-Regeln:</p>
     * <ul>
     *   <li>{@code productName}  <- {@code data.productName}</li>
     *   <li>{@code price}        <- {@code data.priceRaw} als {@link BigDecimal};
     *       {@code null} wenn {@code priceRaw <= 0}</li>
     *   <li>{@code productUrl}   <- {@link ScrapingConstants.BikeComponents#BASE_URL} + {@code data.link}</li>
     *   <li>{@code inStock}      <- {@code !isSoldOut && isBuyable}</li>
     *   <li>{@code shopName}     <- {@link ScrapingConstants.BikeComponents#SHOP_NAME}</li>
     *   <li>{@code shopId}       <- {@link ScrapingConstants.BikeComponents#SHOP_ID}</li>
     *   <li>{@code source}       <- immer {@link FetchMethod#WEB_SCRAPING}</li>
     *   <li>{@code fetchedAt}    <- {@link LocalDateTime#now()} zum Zeitpunkt des Mappings</li>
     * </ul>
     *
     * @param data        JSON-Knoten mit den Produktdaten eines einzelnen Eintrags.
     * @param searchQuery Suchbegriff, der dem {@link ProductOffer} zugeordnet wird.
     * @return Befülltes {@link ProductOffer}.
     */
    private ProductOffer mapToDto(JsonNode data, String searchQuery) {
        double priceRaw = data.path("priceRaw").asDouble();
        String productName = data.path("productName").asText();
        boolean isBuyable = data.path("isBuyable").asBoolean();
        String productId = data.path("productId").asText("?");

        if (productName.isBlank()) {
            log.warn("API-Struktur geändert? Feld 'productName' fehlt oder leer (productId={})", productId);
        }
        if (isBuyable && priceRaw <= 0) {
            log.warn("API-Struktur geändert? Feld 'priceRaw' fehlt oder 0 bei kaufbarem Produkt (productId={})", productId);
        }

        return ProductOffer.builder()
                .productName(productName)
                .price(priceRaw > 0 ? BigDecimal.valueOf(priceRaw) : null)
                .productUrl(ScrapingConstants.BikeComponents.BASE_URL + data.path("link").asText())
                .inStock(!data.path("isSoldOut").asBoolean() && isBuyable)
                .shopName(ScrapingConstants.BikeComponents.SHOP_NAME)
                .source(FetchMethod.WEB_SCRAPING)
                .fetchedAt(LocalDateTime.now())
                .searchQuery(searchQuery)
                .build();
    }
}
