package com.bikeparts.config;

import com.bikeparts.llama.server.LlamaServerManager;
import com.bikeparts.price.ScrapingConstants;
import com.bikeparts.price.entity.ShopInfo;
import com.bikeparts.price.repository.ShopInfoRepository;
import com.bikeparts.price.service.BikeComponentsShippingCostScraperService;
import com.bikeparts.price.service.BikeDiscountShippingCostScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Initialisiert beim Hochfahren der Anwendung den DB-Cache fuer Shop-Versandkosten.
 *
 * <p>Beim Start wird geprueft, ob fuer bike-components.de + bike-discount.de noch aktuelle Versandkosten
 * in der DB vorhanden sind (Alter < 14 Tage). Falls nicht, werden die Versandkosten
 * neu gescrapt und in der DB gespeichert.</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializerConfig {

    private final ShopInfoRepository shopInfoRepository;
    private final BikeComponentsShippingCostScraperService bikeComponentsShippingCostScraperService;
    private final BikeDiscountShippingCostScraperService bikeDiscountShippingCostScraperService;

    /**
     * Wird nach dem vollstaendigen Hochfahren der Anwendung ausgefuehrt.
     * Laedt Versandkosten aus der DB oder scrapt sie neu, falls der Cache abgelaufen ist.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        initShippingCostsBikeComponents();
        initShippingCostsBikeDiscount();
    }

    public void initShippingCostsBikeComponents() {
        LocalDateTime cacheThreshold = LocalDateTime.now().minusDays(ScrapingConstants.Common.CACHE_DAYS);
        boolean cacheValid = shopInfoRepository.existsByShopNameAndFetchedAtAfter(
                ScrapingConstants.BikeComponents.SHOP_NAME, cacheThreshold);

        if (cacheValid) {
            log.debug("Versandkosten fuer '{}' sind aktuell - kein Scraping noetig.",
                    ScrapingConstants.BikeComponents.SHOP_NAME);
            return;
        }

        log.info("Versandkosten fuer '{}' nicht im Cache - starte Scraping...",
                ScrapingConstants.BikeComponents.SHOP_NAME);
        ShopInfo shopInfo = bikeComponentsShippingCostScraperService.getStandardShippingCostForGermany();

        if (shopInfo != null) {
            shopInfoRepository.save(shopInfo);
            log.debug("Versandkosten gespeichert: {}", shopInfo.getShippingCost());
        } else {
            log.warn("Scraping der Versandkosten fehlgeschlagen.");
        }
    }

    public void initShippingCostsBikeDiscount() {
        LocalDateTime cacheThreshold = LocalDateTime.now().minusDays(ScrapingConstants.Common.CACHE_DAYS);
        boolean cacheValid = shopInfoRepository.existsByShopNameAndFetchedAtAfter(
                ScrapingConstants.BikeDiscount.SHOP_NAME, cacheThreshold);

        if (cacheValid) {
            log.debug("Versandkosten fuer '{}' sind aktuell - kein Scraping noetig.",
                    ScrapingConstants.BikeDiscount.SHOP_NAME);
            return;
        }

        log.info("Versandkosten fuer '{}' nicht im Cache - starte Scraping...",
                ScrapingConstants.BikeDiscount.SHOP_NAME);
        ShopInfo shopInfo = bikeDiscountShippingCostScraperService.getStandardShippingCostForGermany();

        if (shopInfo != null) {
            shopInfoRepository.save(shopInfo);
            log.debug("Versandkosten gespeichert: {}", shopInfo.getShippingCost());
        } else {
            log.warn("Scraping der Versandkosten fehlgeschlagen.");
        }
    }
}
