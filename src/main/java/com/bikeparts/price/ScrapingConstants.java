package com.bikeparts.price;

/**
 * Gemeinsame Konstanten fuer alle Scraper-Services.
 *
 * <p>Enthaelt shop-uebergreifende Konstanten (User-Agent, Cache-Dauer)
 * sowie shop-spezifische Konstanten je Shop.</p>
 */
public final class ScrapingConstants {

    private ScrapingConstants() {}

    // --- Allgemein ---

    /** Browser-User-Agent fuer alle HTTP-Requests. Reduziert Bot-Erkennung. */
    public static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:148.0) Gecko/20100101 Firefox/148.0";

    /** Landesbezeichnung fuer Deutschland (Teilstring-Match in Versandkostentabellen). */
    public static final String COUNTRY_GERMANY = "Deutschland";

    /** Anzahl der Tage, nach denen gecachte Daten als veraltet gelten. */
    public static final int CACHE_DAYS = 14;

    /** Maximale Anzahl der zurueckgegebenen Produkte pro Suchanfrage. */
    public static final int MAX_NUMBER_PRODUCT_OFFERS = 8;

    // --- bike-components.de ---

    /** Anzeigename des Shops bike-components.de. */
    public static final String SHOP_NAME_BIKE_COMPONENTS = "bike-components.de";

    /** Interne Shop-ID fuer bike-components.de. */
    public static final Long SHOP_ID_BIKE_COMPONENTS = 1L;

    /** Basis-URL von bike-components.de. Wird fuer absolute Produkt-URLs benoetigt. */
    public static final String BASE_URL_BIKE_COMPONENTS = "https://www.bike-components.de";

    /** Such-URL von bike-components.de. Suchbegriff wird direkt angehaengt. */
    public static final String SEARCH_URL_BIKE_COMPONENTS =
            BASE_URL_BIKE_COMPONENTS + "/de/s/?keywords=";

    /** URL der Versandkostenseite von bike-components.de. */
    public static final String SHIPPING_URL_BIKE_COMPONENTS =
            BASE_URL_BIKE_COMPONENTS + "/de/service/versand/";
}
