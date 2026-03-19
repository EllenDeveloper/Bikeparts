package com.bikeparts.price;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fuer {@link ScrapingUtils#containsAllTerms(String, String)}.
 *
 * <p>Die Testdaten basieren auf realen Scraping-Ergebnissen von bike-components.de
 * fuer die Suchanfrage "Shimano XT Kette 10-fach".</p>
 */
@DisplayName("ScrapingUtils")
class ScrapingUtilsTest {

    @Nested
    @DisplayName("containsAllTerms - all terms match")
    class AllTermsMatch {

        @Test
        @DisplayName("returns true when all terms are present as exact tokens")
        void allTermsPresent() {
            assertTrue(ScrapingUtils.containsAllTerms(
                    "Shimano XT Kette 10-fach",
                    "Shimano XT / XTR / SLX CN-HG95 10-fach Kette"));
        }

        @Test
        @DisplayName("returns true when product name contains terms separated by plus sign")
        void termsSeparatedByPlus() {
            assertTrue(ScrapingUtils.containsAllTerms(
                    "Shimano XT Kette 10-fach",
                    "Shimano XT Kassette CS-M771-10 + Kette CN-HG95 10-fach Verschleissset"));
        }

        @Test
        @DisplayName("returns true when search query is case-insensitive")
        void caseInsensitive() {
            assertTrue(ScrapingUtils.containsAllTerms(
                    "shimano xt kette 10-fach",
                    "Shimano XT / XTR / SLX CN-HG95 10-fach Kette"));
        }

        @Test
        @DisplayName("returns true for single term query")
        void singleTerm() {
            assertTrue(ScrapingUtils.containsAllTerms(
                    "Shimano",
                    "Shimano XT CN-HG95 10-fach Kette"));
        }
    }

    @Nested
    @DisplayName("containsAllTerms - term missing")
    class TermMissing {

        @Test
        @DisplayName("returns false when speed specification does not match")
        void wrongSpeedSpecification() {
            assertFalse(ScrapingUtils.containsAllTerms(
                    "Shimano XT Kette 10-fach",
                    "Shimano Ultegra / XT / E-Bike Quick-Link Kette CN-HG701-11 11-fach"));
        }

        @Test
        @DisplayName("returns false when product type does not match")
        void wrongProductType() {
            assertFalse(ScrapingUtils.containsAllTerms(
                    "Shimano XT Kette 10-fach",
                    "Shimano XT Kassette CS-M771-10 10-fach"));
        }

        @Test
        @DisplayName("returns false when both product type and speed are missing")
        void multipleTermsMissing() {
            assertFalse(ScrapingUtils.containsAllTerms(
                    "Shimano XT Kette 10-fach",
                    "Shimano XT Kassette CS-M8000 11-fach"));
        }
    }

    @Nested
    @DisplayName("containsAllTerms - partial word match prevention")
    class PartialWordMatchPrevention {

        @Test
        @DisplayName("returns false when term matches only as substring of a longer word")
        void ketteDoesNotMatchKettenblatt() {
            assertFalse(ScrapingUtils.containsAllTerms(
                    "Shimano XT Kette 10-fach",
                    "Shimano XT Kettenblatt FC-M780 / FC-T780 / FC-T781 10-fach"));
        }

        @Test
        @DisplayName("returns false when term matches only as substring of a compound token")
        void xtDoesNotMatchXtRearDerailleur() {
            assertFalse(ScrapingUtils.containsAllTerms(
                    "Shimano XT Kette 10-fach",
                    "Shimano XTR Kassette CN-HG95 10-fach Kette"));
        }
    }

    @Nested
    @DisplayName("containsAllTerms - separator handling")
    class SeparatorHandling {

        @Test
        @DisplayName("returns true when terms are separated by slash in product name")
        void slashSeparator() {
            assertTrue(ScrapingUtils.containsAllTerms(
                    "XT XTR SLX",
                    "Shimano XT / XTR / SLX CN-HG95"));
        }

        @Test
        @DisplayName("returns true when terms are separated by comma in product name")
        void commaSeparator() {
            assertTrue(ScrapingUtils.containsAllTerms(
                    "Kassette Kette",
                    "Shimano XT Kassette,Kette 10-fach"));
        }
    }
}
