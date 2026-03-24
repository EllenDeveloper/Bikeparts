package com.bikeparts.price;

import com.bikeparts.config.ProxyConfig;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ScrapingUtils {


    private final ProxyConfig proxyConfig;

    public ScrapingUtils(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    public static boolean checkTerms(String searchQuery, String productName) {
        return containsAllTerms(searchQuery, productName)
                || containsSomeTerms(searchQuery, productName);
    }

    /**
     * Prueft, ob alle Suchbegriffe der {@code searchQuery} als exakte Token
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

    public static boolean containsShimanoAbbreviation(String searchQuery, String productName) {
        // Produktnamen in Token-Set zerlegen (Trennzeichen: Leerzeichen, /, +, ,)
        Set<String> productTokens = Arrays.stream(
                        productName.toLowerCase().split("[\\s/+,]+"))
                .collect(Collectors.toSet());

        // die CS- bezeichnung von shimano herausfiltern: CS-M771-10
        Set<String> searchQuerySet = Arrays.stream(
                        searchQuery.toLowerCase().split("[\\s/+,]+"))
                .collect(Collectors.toSet());

        //  TODO: Shimano Abkürzungen in application.properties
        Optional<String> shimanoAbbreviations = searchQuerySet.stream()
                .filter(p -> p.contains("cs-")).filter(p -> p.contains("cn-"))
                .filter(p -> p.contains("st-")).filter(p -> p.contains("sl-"))
                .findFirst();

        return shimanoAbbreviations.filter(productTokens::contains).isPresent();
    }

    public static boolean containsSomeTerms(String searchQuery, String productName) {
        // Produktnamen in Token-Set zerlegen (Trennzeichen: Leerzeichen, /, +, ,)
        Set<String> productTokens = Arrays.stream(
                        productName.toLowerCase().split("[\\s/+,]+"))
                .collect(Collectors.toSet());

        // true wenn ein Suchbegriff als exaktes Token im Produktnamen vorkommt
        // TODO: Suche verbessern
        return Arrays.stream(searchQuery.toLowerCase().split("\\s+"))
                .anyMatch(productTokens::contains);
    }

    public Connection buildConnection(String url) {
        Connection conn = Jsoup.connect(url)
                .userAgent(ScrapingConstants.Common.USER_AGENT)
                .timeout(20_000);
        if (proxyConfig.isEnabled()) {
            conn.proxy(proxyConfig.getHost(), proxyConfig.getPort());
        }
        return conn;
    }
}
