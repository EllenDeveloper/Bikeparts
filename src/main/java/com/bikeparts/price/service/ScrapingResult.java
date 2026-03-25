package com.bikeparts.price.service;

import com.bikeparts.price.entity.ProductOffer;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Ergebnis einer Scraping-Anfrage an einen Online-Shop.
 *
 * <p>Kapselt die gefundenen {@link ProductOffer}s zusammen mit einem Status,
 * der zwischen einem erfolgreichen Abruf, keinen Treffern und einem
 * technischen Fehler unterscheidet. Wird nicht persistiert - dient nur
 * als Rückgabewert im Anwendungs-Layer.</p>
 *
 * <p>GraalVM Native Image: Die Reflection-Registrierung dieser Klasse erfolgt
 * über {@code @RegisterReflectionForBinding} in
 * {@link com.bikeparts.controller.BikeViewController}, da Spring AOT
 * Reflection-Hints nur für Spring-Beans (nicht für Plain-POJOs) automatisch
 * verarbeitet. Damit kann Thymeleaf/SpEL auf {@code shopName}, {@code status}
 * und {@code offers} zugreifen.</p>
 *
 * @see ScrapingStatus
 * @see ProductOffer
 */
@Data
@Builder
public class ScrapingResult {

    /** Gefundene Angebote; leer bei {@code NO_RESULTS} und {@code ERROR}. */
    private List<ProductOffer> offers;

    /** Ergebnis-Status der Scraping-Anfrage. */
    private ScrapingStatus status;

    /** Technische Fehlermeldung bei {@code ERROR}, sonst {@code null}. */
    private String errorMessage;

    /** Name des Shops, z.B. {@code "bike-components.de"}. */
    private String shopName;

    // -------------------------------------------------------------------------
    // Factory-Methoden
    // -------------------------------------------------------------------------

    /**
     * Erzeugt ein erfolgreiches Ergebnis mit mindestens einem Angebot.
     *
     * @param offers   gefundene Angebote
     * @param shopName Name des Shops
     * @return {@code ScrapingResult} mit Status {@link ScrapingStatus#SUCCESS}
     */
    public static ScrapingResult success(List<ProductOffer> offers, String shopName) {
        return ScrapingResult.builder()
                .offers(offers)
                .status(ScrapingStatus.SUCCESS)
                .shopName(shopName)
                .build();
    }

    /**
     * Erzeugt ein Ergebnis ohne Treffer.
     *
     * @param shopName Name des Shops
     * @return {@code ScrapingResult} mit Status {@link ScrapingStatus#NO_RESULTS}
     */
    public static ScrapingResult noResults(String shopName) {
        return ScrapingResult.builder()
                .offers(List.of())
                .status(ScrapingStatus.NO_RESULTS)
                .shopName(shopName)
                .build();
    }

    /**
     * Erzeugt ein Fehler-Ergebnis.
     *
     * @param errorMessage technische Fehlermeldung
     * @param shopName     Name des Shops
     * @return {@code ScrapingResult} mit Status {@link ScrapingStatus#ERROR}
     */
    public static ScrapingResult error(String errorMessage, String shopName) {
        return ScrapingResult.builder()
                .offers(List.of())
                .status(ScrapingStatus.ERROR)
                .errorMessage(errorMessage)
                .shopName(shopName)
                .build();
    }

    // -------------------------------------------------------------------------
    // Enum
    // -------------------------------------------------------------------------

    /** Mögliche Ergebnis-Zustände einer Scraping-Anfrage. */
    public enum ScrapingStatus {
        /** Mindestens ein Angebot wurde gefunden. */
        SUCCESS,
        /** Anfrage erfolgreich, aber keine Treffer im Shop. */
        NO_RESULTS,
        /** Technischer Fehler - Shop nicht erreichbar oder Parsing fehlgeschlagen. */
        ERROR
    }
}
