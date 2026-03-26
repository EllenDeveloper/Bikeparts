package com.bikeparts.price.entity;

import com.bikeparts.entity.CartItem;
import com.bikeparts.price.enums.FetchMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.Proxy;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) für ein einzelnes Produkt aus einem Online-Shop.
 *
 * <p>Dieses DTO wird vom Scraper-Layer befüllt und über die REST-API
 * an den Client ausgeliefert. Es enthält ausschließlich die für den
 * Preisvergleich relevanten Felder – keine persistenzspezifischen Annotationen,
 * da das Objekt rein für den Datentransport verwendet wird.</p>
 *
 * <p>Wird mit Lombok {@code @Builder} erzeugt, sodass der aufrufende Code
 * lesbare Builder-Ausdrücke verwenden kann:</p>
 * <pre>{@code
 * ProductOffer dto = ProductOffer.builder()
 *     .productName("Shimano Kette")
 *     .price(new BigDecimal("19.99"))
 *     .inStock(true)
 *     .source(FetchMethod.WEB_SCRAPING)
 *     .fetchedAt(LocalDateTime.now())
 *     .build();
 * }</pre>
 *
 * @see FetchMethod
 * @see com.bikeparts.price.service.BikeComponentsScraperService
 */
// GraalVM Native Image: BytecodeProvider ist 'none' - Hibernate kann keine
// HibernateProxy-Instanzen zur Laufzeit erzeugen. @Proxy(lazy = false) deaktiviert
// die Proxy-Generierung fuer diese Entity und verhindert den Laufzeitfehler.
@Proxy(lazy = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_offer")
public class ProductOffer {

    /**
     * Interne ID des Produkts (optional, z. B. für spätere Datenbankanbindung).
     * Wird beim reinen Scraping nicht gesetzt und ist {@code null}.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_item_id")
    @JsonIgnore
    @ToString.Exclude // cartItem will not be included in the toString method
    private CartItem cartItem;

    /** Suchbegriff, mit dem dieses Angebot gefunden wurde. Wird fuer Cache-Lookup benoetigt. */
    @Column(name = "search_query", length = 200)
    private String searchQuery;

    /**
     * Vollständiger Produktname, wie er im Shop angezeigt wird.
     * Beispiel: {@code "Shimano XT / XTR / SLX CN-HG95 10-fach Kette"}
     */
    @Column(nullable = false, length = 200)
    private String productName;

    /**
     * Aktueller Verkaufspreis des Produkts als exakter Dezimalwert.
     * Ist {@code null}, wenn kein Preis ermittelbar war (z. B. priceRaw = 0).
     * Wird als {@link BigDecimal} gespeichert, um Rundungsfehler zu vermeiden.
     */
    @Column
    private BigDecimal price;

    /**
     * Direkte URL zur Produktdetailseite im jeweiligen Shop.
     * Beispiel: {@code "https://www.bike-components.de/de/Shimano/.../p35948/"}
     */
    @Column(nullable = false, length = 200)
    private String productUrl;

    /**
     * Gibt an, ob das Produkt aktuell bestellbar ist.
     * {@code true}, wenn {@code isBuyable = true} und {@code isSoldOut = false}.
     */
    @Column
    private boolean inStock;

    /**
     * Anzeigename des Shops, aus dem das Produkt stammt.
     * Beispiel: {@code "bike-components.de"}
     */
    @Column(nullable = false, length = 50)
    private String shopName;

    /**
     * Interne Shop-ID zur eindeutigen Identifikation des Datenlieferanten.
     * Beispiel: {@code 1L} für bike-components.de, {@code 2L} für rosebikes.de.
     */
    @Column
    private Long shopId;

    /**
     * Quelle, aus der das Produkt bezogen wurde.
     * Beim Web-Scraping wird dieser Wert auf {@link FetchMethod#WEB_SCRAPING} gesetzt.
     *
     * @see FetchMethod
     */
    @Column
    @Enumerated(EnumType.STRING)
    private FetchMethod source;

    /**
     * Zeitstempel, wann der Datensatz abgerufen wurde (UTC-Lokalzeit des Servers).
     * Wird beim Mapping mit {@link LocalDateTime#now()} befüllt
     * und dient zur Nachvollziehbarkeit der Datenaktualität.
     */
    @Column(nullable = false)
    private LocalDateTime fetchedAt;

    /**
     * Gibt eine kompakte Textdarstellung dieses Angebots zurück.
     *
     * <p>Enthält die für einen schnellen Überblick relevanten Felder:
     * ID, Produktname, Shopname, Preis und Verfügbarkeit.</p>
     *
     * <p>Beispielausgabe:</p>
     * <pre>{@code
     * id=42, productName=Shimano XT Kette, shopName=bike-components.de, price=19.99, inStock=true
     * }</pre>
     *
     * @return kompakte Darstellung des Angebots
     */
    public String toStringForLlama() {
        return "id=\"" + id
             + "\", productName=\"" + productName
             + "\", price=\"" + String.format("%.2f", price)
             + "\"  \n";
    }
}
