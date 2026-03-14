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
 * <p>Wird vom {@link com.bikeparts.price.service.BikeComponentsShippingCostScraperService}
 * befüllt und kapselt neben den gescrapten Versandkosten alle zur Nachvollziehbarkeit
 * notwendigen Informationen: Shop-Identifikation, Quellenangabe und Abfragezeitpunkt.</p>
 *
 * @see FetchMethod
 * @see com.bikeparts.price.service.BikeComponentsShippingCostScraperService
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
