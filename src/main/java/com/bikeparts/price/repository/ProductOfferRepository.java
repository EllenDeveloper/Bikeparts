package com.bikeparts.price.repository;

import com.bikeparts.price.entity.ProductOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductOfferRepository extends JpaRepository<ProductOffer, Long> {

    List<ProductOffer> findBySearchQuery(String searchQuery);

    /**
     * Cache-Lookup: Findet alle Angebote fuer einen Suchbegriff,
     * die nach einem bestimmten Zeitpunkt abgerufen wurden.
     * Beispiel: findBySearchQueryAndFetchedAtAfter(searchQuery, now().minusDays(14))
     */
    List<ProductOffer> findBySearchQueryAndFetchedAtAfter(String searchQuery, LocalDateTime fetchedAt);

    List<ProductOffer> findBySearchQueryAndFetchedAtBefore(String searchQuery, LocalDateTime fetchedAtBefore);
}
