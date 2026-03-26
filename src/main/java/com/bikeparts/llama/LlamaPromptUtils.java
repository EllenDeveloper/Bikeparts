package com.bikeparts.llama;

import com.bikeparts.price.entity.ProductOffer;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LlamaPromptUtils {

    public static final String chatMLToken_UserPart = "Gib mir die Top-Ergebnisse NUR aus der obigen Liste.";


    public static @NonNull String getSystemPrompt(String searchQuery, List<ProductOffer> productOfferBySearchQuery) {
        // Zerlegt den String in Einzelwörter und fügt sie mit Anführungszeichen zusammen
        String mandatoryTerms = String.join(", ",
                Arrays.stream(searchQuery.split(" "))
                        .map(word -> "\"" + word + "\"")
                        .toList()
        );
        List<ProductOffer> filteredOffers = preFilterAndSort(searchQuery, productOfferBySearchQuery);

        String filteredOffersString = toStringForLlama(filteredOffers);
//        1. Der 'product_name' MUSS "SLX" enthalten. (Reine "XT" oder "105" Produkte ignorieren).
//        2. Die Kette MUSS exakt "10-fach" sein. Wenn "10-fach" in der Suchanfrage steht. (Ignoriere "11-fach", "12-fach" oder andere Zahlen).
//        3. Sortierung: Der niedrigste 'price' muss in der ersten Zeile stehen.
//
        String systemPrompt = """
                ### AUFGABE:
                Du bist ein Preis-Vergleichs-Agent. Deine Aufgabe ist es, aus den reduzierten Daten das beste Angebot zu finden.
                
                ### PARAMETER:
                SUCHANFRAGE: "%s"
                PFLICHT-BEGRIFFE: %s
                
                ### PRÜFUNG:
                1. Sortierung: Der niedrigste 'price' muss in der ersten Zeile stehen.
                2. Der 'product_name' MUSS die begriffe aus "PFLICHT-BEGRIFFE enthalten. 
                3. Die Kette MUSS exakt "10-fach" sein. Wenn "10-fach" in der SUCHANFRAGE steht. (Ignoriere "11-fach", "12-fach" oder andere Zahlen).

                ### AUSGABEFORMAT:
                1. Erstelle eine interne Liste der IDs, sortiert nach Preis. Niedrigster Preis zuerst.
                2. Gib NUR die finale Liste im Format id=[ID], productName=[product_name], price=[price] aus.
                Kein Text davor oder danach.
                3. Nimm EXKLUSIV nur die IDs, die unten in der Sektion 'DATEN ZUM FILTERN' aufgelistet sind. Erfinde niemals IDs dazu.
                
                ### DATEN ZUM FILTERN:
                %s
                """.formatted(searchQuery, mandatoryTerms, filteredOffersString);
        return systemPrompt;
    }


    /**
     * Formatiert ein einzelnes {@link ProductOffer} als String fuer den Llama-Prompt.
     *
     * @param offer das Angebot
     * @return formatierter String
     */
    public static String toStringForLlama(ProductOffer offer) {
        return "id=" + offer.getId()
             + ", productName=" + offer.getProductName()
             + ", price=" + String.format("%.2f", offer.getPrice())
             + " ";
    }

    /**
     * Konvertiert eine Liste von {@link ProductOffer}-Objekten in einen formatierten String fuer den Llama-Prompt.
     *
     * <p>Jedes Angebot wird per {@link #toStringForLlama(ProductOffer)} formatiert und
     * mit Zeilenumbruch verbunden.</p>
     *
     * @param productOffers Liste der Angebote
     * @return formatierter String fuer den Llama-Prompt
     */
    public static String toStringForLlama(List<ProductOffer> productOffers) {
        return productOffers.stream()
                .map(LlamaPromptUtils::toStringForLlama)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Filtert und sortiert eine Liste von {@link ProductOffer}-Objekten anhand einer Suchanfrage.
     *
     * <p>Folgende Schritte werden ausgefuehrt:</p>
     * <ul>
     *   <li>Nur Angebote mit {@code inStock=true} werden behalten.</li>
     *   <li>Nur Angebote, deren {@code productName} mindestens 70% der Woerter der Suchanfrage
     *       enthaelt, werden behalten. Rein numerische Woerter (z.B. "10") werden als Wortgrenze
     *       geprueft, damit "10-fach" nicht auf "110" matcht.</li>
     *   <li>Die verbleibenden Angebote werden nach Preis aufsteigend sortiert.
     *       Angebote ohne Preis werden ans Ende gestellt.</li>
     * </ul>
     *
     * @param searchQuery   die Suchanfrage
     * @param productOffers die zu filternde Liste
     * @return gefilterte und sortierte Liste
     */
    public static List<ProductOffer> preFilterAndSort(String searchQuery, List<ProductOffer> productOffers) {
        // Wir behalten "10-fach" (wegen Fahrradkette 10-fach) als ein Wort zusammen
        String[] keywordsOfSearchQuery = searchQuery.toLowerCase().split("\\s+");
        long mimumNumberOfMatches = (long) Math.ceil(keywordsOfSearchQuery.length * 0.7);

        return preFilter(searchQuery, productOffers).stream()
                // Sortierung nach Preis (aufsteigend), null-Preise ans Ende
                .sorted((a, b) -> {
                    if (a.getPrice() == null) return 1;
                    if (b.getPrice() == null) return -1;
                    return a.getPrice().compareTo(b.getPrice());
                })
// Höchster Preis zuerst
//                .sorted((a, b) -> {
//                    if (a.getPrice() == null) return 1;
//                    if (b.getPrice() == null) return -1;
//                    return b.getPrice().compareTo(a.getPrice());
//                })
                .collect(Collectors.toList());
    }

    /**
     * Filtert eine Liste von {@link ProductOffer}-Objekten anhand einer Suchanfrage.
     *
     * <p>Nur Angebote, deren {@code productName} mindestens 70% der Woerter der Suchanfrage
     * als exakten Substring enthaelt, werden zurueckgegeben.
     * Im Gegensatz zu {@link #preFilterAndSort} wird keine Sortierung vorgenommen.</p>
     *
     * @param searchQuery   die Suchanfrage
     * @param productOffers die zu filternde Liste
     * @return gefilterte Liste ohne Sortierung
     */
    public static List<ProductOffer> preFilter(String searchQuery, List<ProductOffer> productOffers) {
        // Wir behalten "10-fach" (wegen Fahrradkette 10-fach) als ein Wort zusammen
        String[] keywordsOfSearchQuery = searchQuery.toLowerCase().split("\\s+");
        long mimumNumberOfMatches = (long) Math.ceil(keywordsOfSearchQuery.length * 0.7);

        return productOffers.stream()
                .filter(offer -> offer.getProductName() != null)
                .filter(offer -> {
                    String lowerName = offer.getProductName().toLowerCase();
                    long matchCount = Arrays.stream(keywordsOfSearchQuery)
                            .filter(lowerName::contains)
                            .count();
                    return matchCount >= mimumNumberOfMatches;
                })
                .collect(Collectors.toList());
    }

    /**
     * Parst einen mehrzeiligen productOffers-String in eine Liste von {@link ProductOffer}-Objekten.
     *
     * <p>Jede nicht-leere Zeile wird per {@link #getProductOfferFromString(String)} geparst.</p>
     *
     * @param productOffersString der vollstaendige mehrzeilige Angebotsstring
     * @return Liste der geparsten {@link ProductOffer}-Objekte
     */
    public static List<ProductOffer> getProductOffersFromString(String productOffersString) {
        List<ProductOffer> result = new ArrayList<>();
        for (String line : productOffersString.split("\n")) {
            if (!line.isBlank()) {
                result.add(getProductOfferFromString(line));
            }
        }
        return result;
    }

    /**
     * Parst einen einzelnen ProductOffer-Eintrag aus einem formatierten String.
     *
     * <p>Erwartet das Format aus {@link com.bikeparts.llama.client.LlamaHttpClientMain}:</p>
     * <pre>
     * id=8, productName=Shimano SLX Kette, shopName=bike-components.de, price=24.99, inStock=true
     * </pre>
     *
     * @param text ein einzelner Angebotsstring (eine Zeile aus dem productOffers-Block)
     * @return ein {@link ProductOffer} mit den geparsten Feldern
     */
    public static ProductOffer getProductOfferFromString(String text) {
        String s = text.strip();
        String idStr    = between(s, "id=",          ", productName=");
        String priceStr = after(s,   "price=");
        return ProductOffer.builder()
                .id(idStr != null ? Long.parseLong(idStr) : null)
                .productName(between(s, "productName=", ", price="))
                .price(priceStr != null ? new BigDecimal(priceStr) : null)
                .build();
    }

    private static String between(String text, String start, String end) {
        int s = text.indexOf(start);
        int e = text.indexOf(end);
        if (s == -1 || e == -1) return null;
        return text.substring(s + start.length(), e);
    }

    private static String after(String text, String start) {
        int s = text.indexOf(start);
        if (s == -1) return null;
        return text.substring(s + start.length()).strip();
    }
}
