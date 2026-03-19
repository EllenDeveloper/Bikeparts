package com.bikeparts.price.entity;

import com.bikeparts.price.enums.FetchMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Enthält Metadaten und Versandkosten eines Online-Shops.
 *
 * <p>Wird von den Shipping-Cost-Scraper-Services befüllt und kapselt neben den
 * gescrapten Versandkosten alle zur Nachvollziehbarkeit notwendigen Informationen:
 * Shop-Identifikation, Quellenangabe und Abfragezeitpunkt.</p>
 *
 * <p>Felder laut Datenmodell:</p>
 * <ul>
 *   <li>{@link #shopName} - Anzeigename des Shops</li>
 *   <li>{@link #shippingCostUrl} - URL der gescrapten Versandkostenseite</li>
 *   <li>{@link #shippingCost} - Standard-Versandkosten für Deutschland</li>
 *   <li>{@link #freeShippingOnOrdersOver} - Mindestbestellwert für kostenlosen Versand</li>
 *   <li>{@link #source} - Bezugsquelle (immer {@link FetchMethod#WEB_SCRAPING})</li>
 *   <li>{@link #fetchedAt} - Zeitstempel des letzten Abrufs</li>
 * </ul>
 *
 * <p>Relation: N:1 zu CART_ITEM (mehrere ShopInfo-Einträge können einem Cart-Item
 * zugeordnet werden). Die Relation zu SEARCH_RESULT ist für eine spätere Phase
 * vorgesehen.</p>
 *
 * @see FetchMethod
 * @see com.bikeparts.price.service.BikeComponentsShippingCostScraperService
 * @see com.bikeparts.price.service.BikeDiscountShippingCostScraperService
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shop_info")
public class ShopInfo {

    /**
     * Primärschlüssel zur eindeutigen Identifikation des Shops.
     * Beispiel: {@code 1L} für bike-components.de.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Anzeigename des Shops.
     * Beispiel: {@code "bike-components.de"}
     */
    @Column(nullable = false, length = 50)
    private String shopName;

    /**
     * URL der Seite, von der die Versandkosten gescrapt wurden.
     * Beispiel: {@code "https://www.bike-components.de/de/service/versand/"}
     */
    @Column(nullable = false, length = 200)
    private String shippingCostUrl;

    /**
     * Gescrapte Standard-Versandkosten für Deutschland als exakter Dezimalwert.
     * Ist {@code null}, wenn beim Scraping kein Preis ermittelt werden konnte.
     * Beispiel: {@code 4.99}
     */
    @Column
    private BigDecimal shippingCost;

    /**
     * Mindestbestellwert, ab dem der Shop versandkostenfrei liefert.
     * Ist {@code null}, wenn kein kostenloser Versand angeboten wird oder der Wert
     * beim Scraping nicht ermittelt werden konnte.
     * Beispiel: {@code 49.00} (ab 49 € = kostenloser Versand)
     */
    @Column
    private BigDecimal freeShippingOnOrdersOver;

    /**
     * Quelle, aus der die Versandkosten bezogen wurden.
     * Beim Web-Scraping wird dieser Wert auf {@link FetchMethod#WEB_SCRAPING} gesetzt.
     *
     * @see FetchMethod
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FetchMethod source;

    /**
     * Zeitstempel des letzten Abrufs (Lokalzeit des Servers).
     * Wird beim Mapping mit {@link LocalDateTime#now()} befüllt
     * und dient zur Nachvollziehbarkeit der Datenaktualität.
     */
    @Column
    private LocalDateTime fetchedAt;
}
