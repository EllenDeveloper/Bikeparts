package com.bikeparts.price.repository;

import com.bikeparts.price.entity.ShopInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ShopInfoRepository extends JpaRepository<ShopInfo, Long> {

    /**
     * Findet einen Shop anhand seines Namens.
     */
    Optional<ShopInfo> findByShopName(String shopName);

    /**
     * Cache-Lookup: Prueft ob fuer einen Shop noch aktuelle Versandkosten vorhanden sind.
     * Beispiel: existsByShopNameAndFetchedAtAfter("bike-components.de", now().minusDays(14))
     */
    boolean existsByShopNameAndFetchedAtAfter(String shopName, LocalDateTime fetchedAt);

    /**
     * Cache-Lookup: Findet den aktuellsten ShopInfo-Eintrag fuer einen Shop.
     */
    Optional<ShopInfo> findByShopNameAndFetchedAtAfter(String shopName, LocalDateTime fetchedAt);
}
