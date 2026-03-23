package com.bikeparts.price.service;

import com.bikeparts.price.ScrapingConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bikeparts.price.entity.ProductOffer;
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
 * Unit-Tests für {@link BikeComponentsScraperService}.
 *
 * <h2>Teststrategie</h2>
 * <p>Getestet wird ausschließlich die package-private Methode
 * {@link BikeComponentsScraperService#parseDocument(Document, String)}, die die gesamte
 * Parsing-Logik enthält. Der HTTP-Request ({@code Jsoup.connect(...)}) wird
 * bewusst <strong>nicht</strong> ausgeführt – stattdessen wird ein reales
 * HTML-Dokument aus {@code src/test/resources/} per Classpath geladen und direkt
 * als {@link Document} übergeben.</p>
 *
 * <p>Vorteile dieses Ansatzes:</p>
 * <ul>
 *   <li>Kein Netzwerkzugriff → Tests laufen offline und deterministisch</li>
 *   <li>Keine statischen Mocks nötig (kein {@code mockStatic(Jsoup.class)})</li>
 *   <li>Tests basieren auf echter Shop-Antwort → realistische Abdeckung</li>
 * </ul>
 *
 * <h2>Testdaten</h2>
 * <ul>
 *   <li>{@code response_Shimano_Kette_SLX.xml} – echte HTTP-Antwort
 *       der Suchseite für „shimano fahrradkette slx"; enthält 66 Treffer,
 *       24 Produkte auf Seite 1</li>
 *   <li>{@code emptyDoc} – minimales HTML ohne {@code ProductCatalog}-Element,
 *       simuliert eine leere oder fehlerhafte Seite</li>
 * </ul>
 *
 * @see BikeComponentsScraperService
 * @see ProductOffer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BikeComponentsScraperService")
class BikeComponentsScraperServiceTest {

    /** Die zu testende Service-Instanz; wird in {@link #setUp()} neu erzeugt. */
    private BikeComponentsScraperService service;

    /** Suchbegriff, der in allen Tests als {@code searchQuery}-Parameter verwendet wird. */
    private static final String TEST_QUERY = "Shimano Kette SLX";

    /**
     * Geparste echte HTTP-Antwort von bike-components.de für die Suche
     * „shimano fahrradkette slx". Wird einmalig in {@link #loadDocuments()}
     * aus {@code src/test/resources/} geladen und für alle Tests wiederverwendet.
     */
    private static Document realDoc;

    /**
     * Minimales HTML-Dokument ohne {@code ProductCatalog}-Element.
     * Simuliert eine Seite ohne Suchergebnisse oder eine unerwartete Antwortstruktur.
     */
    private static Document emptyDoc;

    /**
     * Lädt alle für die Tests benötigten Dokumente einmalig vor dem ersten Test.
     *
     * <p>Die Testressource {@code response_Shimano_Kette_SLX.xml}
     * wird über den Classpath-Mechanismus von JUnit geladen, sodass der Pfad
     * unabhängig vom Working Directory des Build-Tools ist.</p>
     *
     * @throws Exception wenn die Testressource nicht gefunden oder nicht geparst
     *                   werden kann.
     */
    @BeforeAll
    static void loadDocuments() throws Exception {
        URL resource = BikeComponentsScraperServiceTest.class.getClassLoader()
                .getResource("bikeComponents/response_Shimano_Kette_SLX.xml");
        realDoc  = Jsoup.parse(new File(resource.toURI()), "UTF-8");
        emptyDoc = Jsoup.parse("<html><body><p>Keine Ergebnisse</p></body></html>");
    }

    /**
     * Erzeugt vor jedem Test eine frische Service-Instanz mit einem echten
     * {@link ObjectMapper}. Da {@code parseDocument()} keinen Spring-Kontext
     * benötigt, wird der Service direkt per {@code new} instanziiert.
     */
    @BeforeEach
    void setUp() {
        service = new BikeComponentsScraperService(new ObjectMapper());
    }

    // -------------------------------------------------------------------------

    /**
     * Tests für den Normalfall: Das HTML-Dokument enthält ein gültiges
     * {@code ProductCatalog}-Element mit vollständigen Produktdaten.
     */
    @Nested
    @DisplayName("parseDocument(Document) - happy path")
    class ParseDocument {

        /**
         * Verifiziert, dass die Ergebnisliste auf {@link ScrapingConstants.Common#MAX_NUMBER_PRODUCT_OFFERS}
         * begrenzt wird. bike-components.de liefert standardmäßig 24 Produkte pro Seite,
         * der Service gibt jedoch nur die ersten {@code MAX_NUMBER_PRODUCT_OFFERS} zurück.
         * (Gesamttreffer laut Testdatei: 66).
         */
        @Test
        @DisplayName("returns at most MAX_NUMBER_PRODUCT_OFFERS products (total=66, 24 per page)")
        void returns8Products_limitedByMaxNumberProductOffers() {
            List<ProductOffer> result = service.parseDocument(realDoc, TEST_QUERY).offers();
            Long id = 10L;
            for (int i = 0; i < result.size(); i++) {
                result.get(i).setId(id--);
                System.out.println(result.get(i).toStringForLlama());
            }
            assertEquals(ScrapingConstants.Common.MAX_NUMBER_PRODUCT_OFFERS, result.size());
        }

        /**
         * Verifiziert, dass der {@code productName} des ersten Produkts korrekt
         * aus dem JSON-Feld {@code data.productName} gemappt wird.
         * Erwartet wird ein Name, der „Shimano XT" enthält (case-insensitiv).
         */
        @Test
        @DisplayName("maps the first product's name from JSON correctly")
        void mapsFirstProduct_name() {
            ProductOffer first = service.parseDocument(realDoc, TEST_QUERY).offers().get(0);
            assertTrue(first.getProductName().toLowerCase().contains("shimano xt"));
        }

        /**
         * Verifiziert, dass der Preis des ersten Produkts als positiver,
         * nicht-null {@link java.math.BigDecimal} gemappt wird.
         * Das JSON-Feld {@code data.priceRaw} enthält einen Double-Wert > 0.
         */
        @Test
        @DisplayName("maps the first product's price as non-null positive BigDecimal")
        void mapsFirstProduct_price() {
            ProductOffer first = service.parseDocument(realDoc, TEST_QUERY).offers().get(0);
            assertNotNull(first.getPrice());
            assertTrue(first.getPrice().doubleValue() > 0);
        }

        /**
         * Verifiziert, dass {@code inStock} für das erste Produkt {@code true} ist,
         * da laut Testdatei {@code isBuyable=true} und {@code isSoldOut=false} gelten.
         */
        @Test
        @DisplayName("sets inStock=true for the first product (isBuyable=true, isSoldOut=false)")
        void mapsFirstProduct_inStock() {
            assertTrue(service.parseDocument(realDoc, TEST_QUERY).offers().get(0).isInStock());
        }

        /**
         * Verifiziert, dass die {@code productUrl} jedes Produkts mit der
         * Basis-URL {@code https://www.bike-components.de/} beginnt.
         * Der Service setzt die URL aus der Konstante {@code BASE_URL} und dem
         * relativen Pfad aus {@code data.link} zusammen.
         */
        @Test
        @DisplayName("sets productUrl with bike-components.de base URL for every product")
        void setsProductUrl_withBaseUrl() {
            service.parseDocument(realDoc, TEST_QUERY).offers().forEach(dto ->
                    assertTrue(dto.getProductUrl().startsWith("https://www.bike-components.de/")));
        }

        /**
         * Verifiziert, dass {@code shopName} und {@code shopId} für jedes Produkt
         * korrekt auf die Konstanten des Service gesetzt werden:
         * {@code "bike-components.de"} bzw. {@code 1L}.
         */
        @Test
        @DisplayName("sets shopName='bike-components.de' and shopId=1 on every product")
        void setsShopMetadata() {
            service.parseDocument(realDoc, TEST_QUERY).offers().forEach(dto -> {
                assertEquals("bike-components.de", dto.getShopName());
            });
        }

        /**
         * Verifiziert, dass {@code fetchedAt} für jedes Produkt gesetzt ist.
         * Der Wert wird beim Mapping mit {@link java.time.LocalDateTime#now()}
         * befüllt und darf nicht {@code null} sein.
         */
        @Test
        @DisplayName("sets non-null fetchedAt on every product")
        void setsFetchedAt_nonNull() {
            service.parseDocument(realDoc, TEST_QUERY).offers().forEach(dto ->
                    assertNotNull(dto.getFetchedAt()));
        }

        /**
         * Verifiziert, dass {@code searchQuery} für jedes Produkt auf den
         * übergebenen Suchbegriff gesetzt wird.
         */
        @Test
        @DisplayName("sets searchQuery on every product from the passed query parameter")
        void setsSearchQuery_onEveryProduct() {
            service.parseDocument(realDoc, TEST_QUERY).offers().forEach(dto ->
                    assertEquals(TEST_QUERY, dto.getSearchQuery()));
        }
    }

    // -------------------------------------------------------------------------

    /**
     * Tests für Grenzfälle und Fehlersituationen beim Parsen des HTML-Dokuments.
     */
    @Nested
    @DisplayName("parseDocument(Document) - edge cases")
    class ParseDocumentEdgeCases {

        /**
         * Verifiziert, dass eine leere Liste zurückgegeben wird, wenn das
         * HTML-Dokument kein Element mit {@code data-component='ProductCatalog'}
         * enthält. Dies tritt z. B. bei Fehlerseiten oder geänderten
         * Shop-Strukturen auf.
         */
        @Test
        @DisplayName("returns ERROR status when ProductCatalog element is absent from HTML")
        void returnsError_whenNoCatalogElement() {
            ScrapingResult result = service.parseDocument(emptyDoc, TEST_QUERY);
            assertEquals(ScrapingResult.ScrapingStatus.ERROR, result.status());
            assertTrue(result.offers().isEmpty());
        }
    }
}
