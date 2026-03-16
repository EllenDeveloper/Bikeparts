package com.bikeparts.price.service;

import com.bikeparts.price.entity.ProductOffer;

import java.util.List;

/**
 * Ergebnis einer Scraping-Anfrage an einen Online-Shop.
 *
 * <p>Kapselt die gefundenen {@link ProductOffer}s zusammen mit einem Status,
 * der zwischen einem erfolgreichen Abruf, keinen Treffern und einem
 * technischen Fehler unterscheidet. Wird nicht persistiert - dient nur
 * als Rückgabewert im Anwendungs-Layer.</p>
 *
 * @param offers       gefundene Angebote; leer bei {@code NO_RESULTS} und {@code ERROR}
 * @param status       Ergebnis-Status der Scraping-Anfrage
 * @param errorMessage technische Fehlermeldung bei {@code ERROR}, sonst {@code null}
 */
public record ScrapingResult(List<ProductOffer> offers, ScrapingStatus status, String errorMessage) {

    /** Mögliche Ergebnis-Zustände einer Scraping-Anfrage. */
    public enum ScrapingStatus {
        /** Mindestens ein Angebot wurde gefunden. */
        SUCCESS,
        /** Anfrage erfolgreich, aber keine Treffer im Shop. */
        NO_RESULTS,
        /** Technischer Fehler - Shop nicht erreichbar oder Parsing fehlgeschlagen. */
        ERROR
    }

    public static ScrapingResult success(List<ProductOffer> offers) {
        return new ScrapingResult(offers, ScrapingStatus.SUCCESS, null);
    }

    public static ScrapingResult noResults() {
        return new ScrapingResult(List.of(), ScrapingStatus.NO_RESULTS, null);
    }

    public static ScrapingResult error(String errorMessage) {
        return new ScrapingResult(List.of(), ScrapingStatus.ERROR, errorMessage);
    }
}
