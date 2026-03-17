package com.bikeparts.price.enums;

/**
 * Gibt an, aus welcher Quelle ein {@link ProductDto} stammt.
 *
 * <p>Wird beim Scraping oder bei der Datenintegration gesetzt, damit die
 * Herkunft eines Produktdatensatzes jederzeit nachvollziehbar ist.</p>
 *
 * <ul>
 *   <li>{@link #WEB_SCRAPING} – Daten wurden direkt von der Shop-Website gescrapt</li>
 * </ul>
 */
public enum FetchMethod {

    /**
     * Produktdaten wurden direkt per HTTP-Request vom jeweiligen Online-Shop
     * gescrapt (z. B. bike-components.de oder rosebikes.de).
     */
    WEB_SCRAPING
}
