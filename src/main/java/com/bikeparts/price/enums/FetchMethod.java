package com.bikeparts.price.enums;

/**
 * Gibt an, aus welcher Quelle ein {@link ProductDto} stammt.
 *
 * <p>Wird beim Scraping oder bei der Datenintegration gesetzt, damit die
 * Herkunft eines Produktdatensatzes jederzeit nachvollziehbar ist.</p>
 *
 * <ul>
 *   <li>{@link #AMAZON}       – Daten stammen aus dem Amazon-Marketplace</li>
 *   <li>{@link #EBAY}         – Daten stammen aus eBay</li>
 *   <li>{@link #IDEALO}       – Daten stammen vom Preisvergleichsportal Idealo</li>
 *   <li>{@link #WEB_SCRAPING} – Daten wurden direkt von der Shop-Website gescrapt</li>
 * </ul>
 */
public enum FetchMethod {

    /** Produktdaten wurden über die Amazon-API oder per Scraping bezogen. */
    AMAZON,

    /** Produktdaten wurden über die eBay-API oder per Scraping bezogen. */
    EBAY,

    /** Produktdaten wurden vom Preisvergleichsportal Idealo bezogen. */
    IDEALO,

    /**
     * Produktdaten wurden direkt per HTTP-Request vom jeweiligen Online-Shop
     * gescrapt (z. B. bike-components.de oder rosebikes.de).
     */
    WEB_SCRAPING
}
