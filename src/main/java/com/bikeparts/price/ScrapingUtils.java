package com.bikeparts.price;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ScrapingUtils {

    /**
     * Prueft ob alle Suchbegriffe der {@code searchQuery} als exakte Token
     * im {@code productName} enthalten sind.
     *
     * <p>Beide Strings werden in Kleinbuchstaben umgewandelt und an den Zeichen
     * Leerzeichen, {@code /}, {@code +} und {@code ,} in Token zerlegt.
     * Ein Suchbegriff muss als <em>exaktes</em> Token vorkommen -
     * Teilstring-Matches werden vermieden (z. B. "Kette" matcht nicht "Kettenblatt").</p>
     *
     * <p>Beispiel:
     * <pre>
     *   searchQuery  = "Shimano XT Kette 10-fach"
     *   productName  = "Shimano XT / XTR / SLX CN-HG95 10-fach Kette"
     *   productTokens = [shimano, xt, xtr, slx, cn-hg95, 10-fach, kette]
     *
     *   contains("shimano") -> true
     *   contains("xt")      -> true
     *   contains("kette")   -> true
     *   contains("10-fach") -> true
     *   -> true (alle Begriffe gefunden)
     * </pre>
     * </p>
     *
     * @param searchQuery Suchbegriff, z. B. {@code "Shimano XT Kette 10-fach"}
     * @param productName Produktname aus dem Scraping-Ergebnis
     * @return {@code true} wenn alle Suchbegriffe als Token im Produktnamen vorkommen
     */
    public static boolean containsAllTerms(String searchQuery, String productName) {
        // Produktnamen in Token-Set zerlegen (Trennzeichen: Leerzeichen, /, +, ,)
        Set<String> productTokens = Arrays.stream(
                        productName.toLowerCase().split("[\\s/+,]+"))
                .collect(Collectors.toSet());

        // true wenn jeder Suchbegriff als exaktes Token im Produktnamen vorkommt
        // allMatch bricht beim ersten fehlenden Begriff sofort ab (Short-Circuit)
        return Arrays.stream(searchQuery.toLowerCase().split("\\s+"))
                .allMatch(productTokens::contains);
    }
}
