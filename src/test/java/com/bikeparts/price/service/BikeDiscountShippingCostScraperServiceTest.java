package com.bikeparts.price.service;

import com.bikeparts.price.entity.ShopInfo;
import com.bikeparts.price.enums.FetchMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für {@link BikeDiscountShippingCostScraperService}.
 *
 * <h2>Teststrategie</h2>
 * <p>Getestet wird ausschließlich die package-private Methode
 * {@link BikeDiscountShippingCostScraperService#parseDocument(Document)},
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
 *   <li>{@code bikeDiscount/response_versandkosten.xml} - echte HTTP-Antwort
 *       der Versandkostenseite von bike-discount.de; enthält den Deutschland-Block
 *       mit {@code Versandkosten: 4,49 €} und {@code Frei ab: 98,99 €}</li>
 *   <li>{@code emptyDoc} - minimales HTML ohne {@code div.shipping-costs-info__details},
 *       simuliert eine fehlerhafte oder geänderte Seite</li>
 * </ul>
 *
 * @see BikeDiscountShippingCostScraperService
 * @see ShopInfo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BikeDiscountShippingCostScraperService")
class BikeDiscountShippingCostScraperServiceTest {

    /** Die zu testende Service-Instanz; wird in {@link #setUp()} neu erzeugt. */
    private BikeDiscountShippingCostScraperService service;

    /**
     * Geparste echte HTTP-Antwort der Versandkostenseite von bike-discount.de.
     * Wird einmalig in {@link #loadDocuments()} aus {@code src/test/resources/}
     * geladen und für alle Tests wiederverwendet.
     */
    private static Document realDoc;

    /**
     * Minimales HTML-Dokument ohne {@code div.shipping-costs-info__details}-Element.
     * Simuliert eine Seite, die keine Versandkostenblöcke enthält,
     * z. B. bei einer geänderten Shop-Struktur oder einem HTTP-Fehler.
     */
    private static Document emptyDoc;

    /**
     * Lädt alle für die Tests benötigten Dokumente einmalig vor dem ersten Test.
     *
     * <p>{@code response_versandkosten.xml} wird über den
     * Classpath-Mechanismus von JUnit geladen, sodass der Pfad unabhängig
     * vom Working Directory des Build-Tools ist.</p>
     *
     * @throws Exception wenn die Testressource nicht gefunden oder nicht geparst
     *                   werden kann.
     */
    @BeforeAll
    static void loadDocuments() throws Exception {
        URL resource = BikeDiscountShippingCostScraperServiceTest.class.getClassLoader()
                .getResource("bikeDiscount/response_versandkosten.xml");
        realDoc = Jsoup.parse(new File(resource.toURI()), "UTF-8");

        emptyDoc = Jsoup.parse("<html><body><p>Keine Versandkostendaten</p></body></html>");
    }

    /**
     * Erzeugt vor jedem Test eine frische Service-Instanz. Da
     * {@code parseDocument()} keinen Spring-Kontext benötigt und
     * {@link BikeDiscountShippingCostScraperService} keine Constructor-Argumente
     * hat, wird der Service direkt per {@code new} instanziiert.
     */
    @BeforeEach
    void setUp() {
        service = new BikeDiscountShippingCostScraperService();
    }

    // -------------------------------------------------------------------------

    /**
     * Tests für den Normalfall: Das HTML-Dokument enthält einen gültigen
     * Deutschland-Block mit Versandkosten- und Frei-ab-Angabe.
     */
    @Nested
    @DisplayName("parseDocument(Document) - happy path")
    class ParseDocument {

        /**
         * Verifiziert, dass das zurückgegebene {@link ShopInfo}-Objekt nicht
         * {@code null} ist, wenn der Deutschland-Block gefunden wurde.
         */
        @Test
        @DisplayName("returns a non-null ShopInfo for Germany standard shipping")
        void returnsNonNull_forGermany() {
            assertNotNull(service.parseDocument(realDoc));
        }

        /**
         * Verifiziert, dass {@code shippingCost} exakt {@code 4.49} beträgt.
         * Der Rohwert in der Versandkostentabelle lautet {@code " 4,49 € "};
         * der Service muss das deutsche Zahlenformat korrekt in einen
         * {@link BigDecimal} umwandeln.
         */
        @Test
        @DisplayName("sets shippingCost=4.49 for Germany standard shipping")
        void setsCorrectShippingCost() {
            ShopInfo result = service.parseDocument(realDoc);
            assertEquals(new BigDecimal("4.49"), result.getShippingCost());
        }

        /**
         * Verifiziert, dass {@code freeShippingOnOrdersOver} exakt {@code 98.99}
         * beträgt. Der Rohwert lautet {@code "98,99 €"} und wird aus dem
         * {@code dt}-Element „Frei ab" gelesen.
         */
        @Test
        @DisplayName("sets freeShippingOnOrdersOver=98.99 from 'Frei ab' row")
        void setsCorrectFreeShippingThreshold() {
            ShopInfo result = service.parseDocument(realDoc);
            assertEquals(new BigDecimal("98.99"), result.getFreeShippingOnOrdersOver());
        }

        /**
         * Verifiziert, dass {@code shopName} auf {@code "bike-discount.de"} gesetzt wird.
         */
        @Test
        @DisplayName("sets shopName='bike-discount.de'")
        void setsShopName() {
            assertEquals("bike-discount.de", service.parseDocument(realDoc).getShopName());
        }

        /**
         * Verifiziert, dass {@code shippingCostUrl} auf die korrekte Versandkostenseite
         * von bike-discount.de gesetzt wird.
         */
        @Test
        @DisplayName("sets shippingCostUrl to the shippingcosts page URL")
        void setsShippingCostUrl() {
            assertEquals(
                    "https://www.bike-discount.de/de/shippingcosts",
                    service.parseDocument(realDoc).getShippingCostUrl()
            );
        }

        /**
         * Verifiziert, dass {@code source} auf {@link FetchMethod#WEB_SCRAPING} gesetzt wird,
         * da die Daten direkt per HTTP-Scraping bezogen werden.
         */
        @Test
        @DisplayName("sets source=WEB_SCRAPING")
        void setsSource() {
            assertEquals(FetchMethod.WEB_SCRAPING, service.parseDocument(realDoc).getSource());
        }

        /**
         * Verifiziert, dass {@code fetchedAt} nicht {@code null} ist.
         * Der Wert wird beim Mapping mit {@link java.time.LocalDateTime#now()} gesetzt.
         */
        @Test
        @DisplayName("sets non-null fetchedAt timestamp")
        void setsFetchedAt_nonNull() {
            assertNotNull(service.parseDocument(realDoc).getFetchedAt());
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
         * Verifiziert, dass {@code null} zurückgegeben wird, wenn das
         * HTML-Dokument gar keine {@code div.shipping-costs-info__details}-Elemente
         * enthält. Der Service iteriert über diese Elemente - findet er keine,
         * wird {@code null} zurückgegeben und ein Warning geloggt.
         */
        @Test
        @DisplayName("returns null when HTML contains no shipping-costs-info__details block")
        void returnsNull_whenNoDetailsBlockPresent() {
            assertNull(service.parseDocument(emptyDoc));
        }

    }
}
