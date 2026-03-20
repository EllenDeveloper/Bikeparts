package com.bikeparts.price.service;

import com.bikeparts.price.ScrapingConstants;
import com.bikeparts.price.entity.ProductOffer;
import com.bikeparts.price.enums.FetchMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für {@link BikeDiscountScraperService}.
 *
 * <h2>Teststrategie</h2>
 * <p>Getestet wird ausschließlich die package-private Methode
 * {@link BikeDiscountScraperService#parseDocument(Document, String)},
 * die die gesamte Parsing-Logik enthält. Der HTTP-Request
 * ({@code Jsoup.connect(...)}) wird bewusst <strong>nicht</strong> ausgeführt -
 * stattdessen wird ein reales HTML-Dokument aus {@code src/test/resources/}
 * per Classpath geladen und direkt als {@link Document} übergeben.</p>
 *
 * <p>Vorteile dieses Ansatzes:</p>
 * <ul>
 *   <li>Kein Netzwerkzugriff -> Tests laufen offline und deterministisch</li>
 *   <li>Keine statischen Mocks nötig</li>
 *   <li>Tests basieren auf echter Shop-Antwort -> realistische Abdeckung</li>
 * </ul>
 *
 * <h2>Testdaten</h2>
 * <ul>
 *   <li>{@code bikeDiscount/response_Shimano_Kette_SLX.xml} - echte HTTP-Antwort
 *       der Suchseite für „shimano slx kette 10-fach"; enthält 48 Treffer auf Seite 1</li>
 *   <li>{@code emptyDoc} - minimales HTML ohne {@code div.row.cms-listing-row},
 *       simuliert eine leere oder fehlerhafte Seite</li>
 *   <li>{@code noProductsDoc} - HTML mit Listing-Container, aber ohne Produkt-Einträge,
 *       simuliert eine Seite ohne Treffer</li>
 * </ul>
 *
 * <h2>Test-Query</h2>
 * <p>{@link #TEST_QUERY} = {@code "shimano slx"} liefert aus der Testdatei exakt
 * {@link ScrapingConstants.Common#MAX_NUMBER_PRODUCT_OFFERS} passende Treffer
 * (alle SLX-Produkte im Response).</p>
 *
 * @see BikeDiscountScraperService
 * @see ProductOffer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BikeDiscountScraperService")
class BikeDiscountScraperServiceTest {

    /** Die zu testende Service-Instanz; wird in {@link #setUp()} neu erzeugt. */
    private BikeDiscountScraperService service;

    /**
     * Suchbegriff für alle Tests. Liefert aus der Testdatei exakt
     * {@link ScrapingConstants.Common#MAX_NUMBER_PRODUCT_OFFERS} Treffer.
     */
    private static final String TEST_QUERY = "shimano slx";

    /**
     * Geparste echte HTTP-Antwort von bike-discount.de für die Suche
     * „shimano slx kette 10-fach". Wird einmalig in {@link #loadDocuments()}
     * aus {@code src/test/resources/} geladen und für alle Tests wiederverwendet.
     */
    private static Document realDoc;

    /**
     * Minimales HTML-Dokument ohne {@code div.row.cms-listing-row}-Element.
     * Simuliert eine Seite ohne Suchergebnis-Container, z. B. bei einer
     * geänderten Shop-Struktur oder einem HTTP-Fehler.
     */
    private static Document emptyDoc;

    /**
     * HTML-Dokument mit einem leeren Listing-Container (kein Produkt-Eintrag).
     * Simuliert eine Suchergebnisseite ohne Treffer.
     */
    private static Document noProductsDoc;

    /**
     * Lädt alle für die Tests benötigten Dokumente einmalig vor dem ersten Test.
     *
     * @throws Exception wenn die Testressource nicht gefunden oder nicht geparst
     *                   werden kann.
     */
    @BeforeAll
    static void loadDocuments() throws Exception {
        URL resource = BikeDiscountScraperServiceTest.class.getClassLoader()
                .getResource("bikeDiscount/response_Shimano_Kette_SLX.xml");
        realDoc = Jsoup.parse(new File(resource.toURI()), "UTF-8");

        emptyDoc = Jsoup.parse("<html><body><p>Keine Ergebnisse</p></body></html>");

        noProductsDoc = Jsoup.parse("""
                <html><body>
                  <div class="row cms-listing-row" data-aria-live-text="Es werden 0 Produkte angezeigt." role="list">
                  </div>
                </body></html>
                """);
    }

    /**
     * Erzeugt vor jedem Test eine frische Service-Instanz.
     */
    @BeforeEach
    void setUp() {
        service = new BikeDiscountScraperService();
    }

    // -------------------------------------------------------------------------

    /**
     * Tests für den Normalfall: Das HTML-Dokument enthält gültige Suchergebnisse.
     */
    @Nested
    @DisplayName("parseDocument(Document, String) - happy path")
    class ParseDocument {

        /**
         * Verifiziert, dass der Status {@link ScrapingResult.ScrapingStatus#SUCCESS}
         * zurückgegeben wird, wenn passende Produkte gefunden wurden.
         */
        @Test
        @DisplayName("returns SUCCESS status when matching products are found")
        void returnsSuccess_whenProductsFound() {
            assertEquals(ScrapingResult.ScrapingStatus.SUCCESS,
                    service.parseDocument(realDoc, TEST_QUERY).status());
        }

        /**
         * Verifiziert, dass die Ergebnisliste auf
         * {@link ScrapingConstants.Common#MAX_NUMBER_PRODUCT_OFFERS} begrenzt wird.
         * Die Testdatei enthält 48 Produkte, davon mehr als 6 SLX-Treffer.
         */
        @Test
        @DisplayName("returns at most MAX_NUMBER_PRODUCT_OFFERS matching products")
        void limitsResultsToMaxProductOffers() {
            List<ProductOffer> result = service.parseDocument(realDoc, TEST_QUERY).offers();
            assertEquals(ScrapingConstants.Common.MAX_NUMBER_PRODUCT_OFFERS, result.size());
        }

        /**
         * Verifiziert, dass der {@code productName} des ersten Treffers
         * sowohl „shimano" als auch „slx" enthält (case-insensitiv),
         * d. h. der Filter {@link com.bikeparts.price.ScrapingUtils#containsAllTerms(String, String)}
         * korrekt angewendet wurde.
         */
        @Test
        @DisplayName("first product name contains all search terms (shimano, slx)")
        void firstProduct_nameContainsAllSearchTerms() {
            String name = service.parseDocument(realDoc, TEST_QUERY).offers().get(0)
                    .getProductName().toLowerCase();
            assertTrue(name.contains("shimano"));
            assertTrue(name.contains("slx"));
        }

        /**
         * Verifiziert, dass der Preis des ersten Produkts als positiver,
         * nicht-null {@link java.math.BigDecimal} gemappt wird.
         */
        @Test
        @DisplayName("first product has non-null positive price")
        void firstProduct_hasPositivePrice() {
            ProductOffer first = service.parseDocument(realDoc, TEST_QUERY).offers().get(0);
            assertNotNull(first.getPrice());
            assertTrue(first.getPrice().doubleValue() > 0);
        }

        /**
         * Verifiziert, dass {@code inStock} für das erste Produkt {@code true} ist.
         * Das erste SLX-Produkt in der Testdatei besitzt ein {@code form.buy-widget}.
         */
        @Test
        @DisplayName("first product is inStock=true (has buy-widget)")
        void firstProduct_isInStock() {
            assertTrue(service.parseDocument(realDoc, TEST_QUERY).offers().get(0).isInStock());
        }

        /**
         * Verifiziert, dass {@code productUrl} jedes Produkts mit der
         * Basis-URL {@code https://www.bike-discount.de/} beginnt.
         */
        @Test
        @DisplayName("every product has productUrl starting with bike-discount.de base URL")
        void everyProduct_hasCorrectBaseUrl() {
            service.parseDocument(realDoc, TEST_QUERY).offers().forEach(offer ->
                    assertTrue(offer.getProductUrl().startsWith(
                            "https://www.bike-discount.de/")));
        }

        /**
         * Verifiziert, dass {@code shopName} für jedes Produkt auf
         * {@code "bike-discount.de"} gesetzt ist.
         */
        @Test
        @DisplayName("every product has shopName='bike-discount.de'")
        void everyProduct_hasCorrectShopName() {
            service.parseDocument(realDoc, TEST_QUERY).offers().forEach(offer ->
                    assertEquals("bike-discount.de", offer.getShopName()));
        }

        /**
         * Verifiziert, dass {@code source} für jedes Produkt auf
         * {@link FetchMethod#WEB_SCRAPING} gesetzt ist.
         */
        @Test
        @DisplayName("every product has source=WEB_SCRAPING")
        void everyProduct_hasSourceWebScraping() {
            service.parseDocument(realDoc, TEST_QUERY).offers().forEach(offer ->
                    assertEquals(FetchMethod.WEB_SCRAPING, offer.getSource()));
        }

        /**
         * Verifiziert, dass {@code fetchedAt} für jedes Produkt nicht {@code null} ist.
         */
        @Test
        @DisplayName("every product has non-null fetchedAt")
        void everyProduct_hasFetchedAt() {
            service.parseDocument(realDoc, TEST_QUERY).offers().forEach(offer ->
                    assertNotNull(offer.getFetchedAt()));
        }

        /**
         * Verifiziert, dass {@code searchQuery} für jedes Produkt auf den
         * übergebenen Suchbegriff gesetzt ist.
         */
        @Test
        @DisplayName("every product has searchQuery set to the passed query")
        void everyProduct_hasCorrectSearchQuery() {
            service.parseDocument(realDoc, TEST_QUERY).offers().forEach(offer ->
                    assertEquals(TEST_QUERY, offer.getSearchQuery()));
        }
    }

    // -------------------------------------------------------------------------

    /**
     * Tests für Grenzfälle und Fehlersituationen beim Parsen des HTML-Dokuments.
     */
    @Nested
    @DisplayName("parseDocument(Document, String) - edge cases")
    class ParseDocumentEdgeCases {

        /**
         * Verifiziert, dass {@link ScrapingResult.ScrapingStatus#ERROR} zurückgegeben
         * wird, wenn das HTML-Dokument kein {@code div.row.cms-listing-row}-Element enthält.
         */
        @Test
        @DisplayName("returns ERROR status when listing container is absent")
        void returnsError_whenNoListingContainer() {
            ScrapingResult result = service.parseDocument(emptyDoc, TEST_QUERY);
            assertEquals(ScrapingResult.ScrapingStatus.ERROR, result.status());
        }

        /**
         * Verifiziert, dass {@link ScrapingResult.ScrapingStatus#NO_RESULTS} zurückgegeben
         * wird, wenn der Listing-Container vorhanden ist, aber keine Produkt-Einträge enthält.
         */
        @Test
        @DisplayName("returns NO_RESULTS status when listing container has no product items")
        void returnsNoResults_whenListingContainerIsEmpty() {
            ScrapingResult result = service.parseDocument(noProductsDoc, TEST_QUERY);
            assertEquals(ScrapingResult.ScrapingStatus.NO_RESULTS, result.status());
        }
    }
}
