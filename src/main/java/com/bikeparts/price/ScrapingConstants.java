package com.bikeparts.price;

/**
 * Konstanten fuer alle Scraper-Services, aufgeteilt in Inner Classes pro Shop.
 *
 * <p>Gemeinsame Konstanten befinden sich in {@link Common}.
 * Shop-spezifische Konstanten befinden sich in den jeweiligen Inner Classes
 * ({@link BikeComponents}, {@link BikeDiscount}).</p>
 */
public final class ScrapingConstants {

    private ScrapingConstants() {}

    // -------------------------------------------------------------------------

    /**
     * Shop-uebergreifende Konstanten (User-Agent, Cache-Dauer, Laenderbezeichnung).
     */
    public static final class Common {

        private Common() {}

        /** Browser-User-Agent fuer alle HTTP-Requests. Reduziert Bot-Erkennung. */
        public static final String USER_AGENT =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:148.0) Gecko/20100101 Firefox/148.0";

        /** Landesbezeichnung fuer Deutschland (Teilstring-Match in Versandkostentabellen). */
        public static final String COUNTRY_GERMANY = "Deutschland";

        /** Anzahl der Tage, nach denen gecachte Daten als veraltet gelten. */
        public static final int CACHE_DAYS = 14;

        /** Maximale Anzahl der zurueckgegebenen Produkte pro Suchanfrage. */
        public static final int MAX_NUMBER_PRODUCT_OFFERS = 6;
    }

    // -------------------------------------------------------------------------

    /**
     * Konstanten fuer den Shop
     * <a href="https://www.bike-components.de">bike-components.de</a>.
     */
    public static final class BikeComponents {

        private BikeComponents() {}

        /** Anzeigename des Shops. */
        public static final String SHOP_NAME = "bike-components.de";

        /** Interne Shop-ID. */
        public static final Long SHOP_ID = 1L;

        /** Basis-URL. Wird fuer absolute Produkt-URLs benoetigt. */
        public static final String BASE_URL = "https://www.bike-components.de";

        /** Such-URL. Suchbegriff wird direkt angehaengt. */
        public static final String SEARCH_URL = BASE_URL + "/de/s/?keywords=";

        /** URL der Versandkostenseite. */
        public static final String SHIPPING_URL = BASE_URL + "/de/service/versand/";
    }

    // -------------------------------------------------------------------------

    /**
     * Konstanten fuer den Shop
     * <a href="https://www.bike-discount.de">bike-discount.de</a>.
     */
    public static final class BikeDiscount {

        private BikeDiscount() {}

        /** Anzeigename des Shops. */
        public static final String SHOP_NAME = "bike-discount.de";

        /** Interne Shop-ID. */
        public static final Long SHOP_ID = 2L;

        /** Basis-URL. Wird fuer absolute Produkt-URLs benoetigt. */
        public static final String BASE_URL = "https://www.bike-discount.de";

        /** Such-URL. Suchbegriff wird direkt angehaengt. */
        public static final String SEARCH_URL = BASE_URL + "/de/search?search=";

        /** URL der Versandkostenseite. */
        public static final String SHIPPING_URL = BASE_URL + "/de/shippingcosts";
    }
}
