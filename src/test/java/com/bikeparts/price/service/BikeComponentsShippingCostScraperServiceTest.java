package com.bikeparts.price.service;

import com.bikeparts.price.enums.FetchMethod;
import com.bikeparts.price.entity.ShopInfo;
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
 * Unit-Tests für {@link BikeComponentsShippingCostScraperService}.
 *
 * <h2>Teststrategie</h2>
 * <p>Getestet wird ausschließlich die package-private Methode
 * {@link BikeComponentsShippingCostScraperService#parseDocument(Document)},
 * die die gesamte Parsing-Logik enthält. Der HTTP-Request
 * ({@code Jsoup.connect(...)}) wird bewusst <strong>nicht</strong> ausgeführt –
 * stattdessen wird ein reales HTML-Dokument aus {@code src/test/resources/}
 * per Classpath geladen und direkt als {@link Document} übergeben.</p>
 *
 * <p>Vorteile dieses Ansatzes:</p>
 * <ul>
 *   <li>Kein Netzwerkzugriff → Tests laufen offline und deterministisch</li>
 *   <li>Keine statischen Mocks nötig</li>
 *   <li>Tests basieren auf echter Shop-Antwort → realistische Abdeckung</li>
 * </ul>
 *
 * <h2>Testdaten</h2>
 * <ul>
 *   <li>{@code response_versandkosten.xml} - echte HTTP-Antwort
 *       der Versandkostenseite; enthält die Versandkostentabelle mit Deutschland-Zeile
 *       ({@code Standard: 4,99€})</li>
 *   <li>{@code emptyDoc} - minimales HTML ohne Tabelle, simuliert eine fehlerhafte
 *       oder unerwartete Seite</li>
 *   <li>{@code noGermanyDoc} - HTML mit Tabelle, aber ohne Deutschland-Zeile,
 *       simuliert eine Seite mit geändertem Tabelleninhalt</li>
 * </ul>
 *
 * <p>Hinweis: {@link ShopInfo#getFreeShippingOnOrdersOver()} wird vom Service nicht
 * befüllt und ist daher in den Ergebnissen immer {@code null}. Dieses Feld wird
 * hier nicht separat getestet.</p>
 *
 * @see BikeComponentsShippingCostScraperService
 * @see ShopInfo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BikeComponentsShippingCostScraperService")
class BikeComponentsShippingCostScraperServiceTest {

    /** Die zu testende Service-Instanz; wird in {@link #setUp()} neu erzeugt. */
    private BikeComponentsShippingCostScraperService service;

    /**
     * Geparste echte HTTP-Antwort der Versandkostenseite von bike-components.de.
     * Wird einmalig in {@link #loadDocuments()} aus {@code src/test/resources/}
     * geladen und für alle Tests wiederverwendet.
     */
    private static Document realDoc;

    /**
     * Minimales HTML-Dokument ohne {@code <table>}-Element.
     * Simuliert eine Seite, die keine Versandkostentabelle enthält,
     * z. B. bei einer geänderten Shop-Struktur oder einem HTTP-Fehler.
     */
    private static Document emptyDoc;

    /**
     * HTML-Dokument mit einer Versandkostentabelle, die keine Zeile für
     * „Deutschland" enthält. Simuliert den Fall, dass die Tabelle vorhanden
     * ist, aber der gesuchte Ländername fehlt (z. B. nach einer Umstrukturierung
     * der Seite).
     */
    private static Document noGermanyDoc;

    /**
     * Lädt alle für die Tests benötigten Dokumente einmalig vor dem ersten Test.
     *
     * <p>{@code response_versandkosten.xml} wird über den
     * Classpath-Mechanismus von JUnit geladen, sodass der Pfad unabhängig
     * vom Working Directory des Build-Tools ist.</p>
     *
     * <p>{@code noGermanyDoc} wird direkt aus einem HTML-String geparst und
     * enthält nur Einträge für Österreich und Schweiz – nicht für Deutschland.</p>
     *
     * @throws Exception wenn die Testressource nicht gefunden oder nicht geparst
     *                   werden kann.
     */
    @BeforeAll
    static void loadDocuments() throws Exception {
        URL resource = BikeComponentsShippingCostScraperServiceTest.class.getClassLoader()
                .getResource("bikeComponents/response_versandkosten.xml");
        realDoc = Jsoup.parse(new File(resource.toURI()), "UTF-8");

        emptyDoc = Jsoup.parse("<html><body><p>Keine Tabelle</p></body></html>");

        noGermanyDoc = Jsoup.parse("""
                <html><body>
                  <table>
                    <thead><tr><th>Land</th><th>Standard</th></tr></thead>
                    <tbody>
                      <tr><td>Österreich</td><td>5,99€</td></tr>
                      <tr><td>Schweiz</td><td>16,99€</td></tr>
                    </tbody>
                  </table>
                </body></html>
                """);
    }

    /**
     * Erzeugt vor jedem Test eine frische Service-Instanz. Da
     * {@code parseDocument()} keinen Spring-Kontext benötigt und
     * {@link BikeComponentsShippingCostScraperService} keine Constructor-Argumente
     * hat, wird der Service direkt per {@code new} instanziiert.
     */
    @BeforeEach
    void setUp() {
        service = new BikeComponentsShippingCostScraperService();
    }

    // -------------------------------------------------------------------------

    /**
     * Tests für den Normalfall: Das HTML-Dokument enthält eine gültige
     * Versandkostentabelle mit einer Zeile für „Deutschland".
     */
    @Nested
    @DisplayName("parseDocument(Document) - happy path")
    class ParseDocument {

        /**
         * Verifiziert, dass das zurückgegebene {@link ShopInfo}-Objekt nicht
         * {@code null} ist, wenn die Deutschland-Zeile in der Tabelle gefunden wurde.
         */
        @Test
        @DisplayName("returns a non-null ShopInfo for Germany standard shipping")
        void returnsNonNull_forGermany() {
            assertNotNull(service.parseDocument(realDoc));
        }

        /**
         * Verifiziert, dass {@code shippingCost} exakt {@code 4.99} beträgt.
         * Der Rohwert in der Tabelle lautet {@code "4,99€"}; der Service muss
         * das deutsche Zahlenformat korrekt in einen {@link BigDecimal} umwandeln.
         */
        @Test
        @DisplayName("sets shippingCost=4.99 for Germany standard shipping")
        void setsCorrectShippingCost() {
            ShopInfo result = service.parseDocument(realDoc);
            assertEquals(new BigDecimal("4.99"), result.getShippingCost());
        }

        /**
         * Verifiziert, dass {@code shopName} auf {@code "bike-components.de"} gesetzt wird.
         */
        @Test
        @DisplayName("sets shopName='bike-components.de'")
        void setsShopName() {
            assertEquals("bike-components.de", service.parseDocument(realDoc).getShopName());
        }

        /**
         * Verifiziert, dass {@code shippingCostUrl} auf die korrekte Versandkostenseite
         * von bike-components.de gesetzt wird.
         */
        @Test
        @DisplayName("sets shippingCostUrl to the versand page URL")
        void setsShippingCostUrl() {
            assertEquals(
                    "https://www.bike-components.de/de/service/versand/",
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
         * HTML-Dokument gar keine {@code <table>}-Elemente enthält.
         * Der Service iteriert über {@code table tr} – findet er keine Zeilen,
         * wird {@code null} zurückgegeben und ein Warning geloggt.
         */
        @Test
        @DisplayName("returns null when HTML contains no table")
        void returnsNull_whenNoTablePresent() {
            assertNull(service.parseDocument(emptyDoc));
        }

        /**
         * Verifiziert, dass {@code null} zurückgegeben wird, wenn die Tabelle
         * vorhanden ist, aber keine Zeile mit „Deutschland" in der ersten Spalte
         * enthält. Dies deckt den Fall ab, dass die Seitenstruktur geändert wurde
         * oder Deutschland aus der Tabelle entfernt wurde.
         */
        @Test
        @DisplayName("returns null when table contains no Deutschland row")
        void returnsNull_whenNoDeutschlandRow() {
            assertNull(service.parseDocument(noGermanyDoc));
        }
    }
}
