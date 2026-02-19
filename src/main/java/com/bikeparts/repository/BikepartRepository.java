package com.bikeparts.repository;

import com.bikeparts.entity.Bikepart;
import com.bikeparts.enums.BikepartType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Bikepart entity
 */
@Repository
public interface BikepartRepository extends JpaRepository<Bikepart, Long> {

    /**
     * Findet alle Bikeparts eines bestimmten Bikes
     */
    List<Bikepart> findByBikeId(Long bikeId);

    /**
     * Findet Bikeparts nach Typ
     */
    List<Bikepart> findByType(BikepartType type);

    /**
     * Findet Bikeparts nach Typ für ein bestimmtes Bike
     */
    List<Bikepart> findByBikeIdAndType(Long bikeId, BikepartType type);

    /**
     * Findet Bikeparts nach Marke
     */
    List<Bikepart> findByBrandIgnoreCase(String brand);

    /**
     * Findet Bikeparts nach Marke für ein bestimmtes Bike
     */
    List<Bikepart> findByBikeIdAndBrandIgnoreCase(Long bikeId, String brand);

    /**
     * Findet Bikeparts nach Qualität (z.B. "XT")
     */
    List<Bikepart> findByQuality(String quality);

    /**
     * Findet ein spezifisches Bikepart nach Name und Bike
     */
    Optional<Bikepart> findByBikeIdAndName(Long bikeId, String name);

//    /**
//     * Sucht Bikeparts nach Name, Marke oder Modell
//     */
//    @Query("SELECT bp FROM Bikepart bp WHERE bp.bike.id = :bikeId AND " +
//           "(LOWER(bp.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
//           "LOWER(bp.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
//           "LOWER(bp.model) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
//    List<Bikepart> searchBikeparts(@Param("bikeId") Long bikeId, @Param("searchTerm") String searchTerm);

    /**
     * Findet alle Bikeparts eines Accounts (über Bike)
     */
    @Query("SELECT bp FROM Bikepart bp WHERE bp.bike.account.id = :accountId")
    List<Bikepart> findByAccountId(@Param("accountId") Long accountId);

    /**
     * Findet Bikeparts die nach einem bestimmten Datum gekauft wurden
     */
    List<Bikepart> findByLastPurchaseDateAfter(LocalDate date);

//    /**
//     * Findet Bikeparts die nach einem bestimmten Datum installiert wurden
//     */
//    List<Bikepart> findByInstallationDateAfter(LocalDate date);

    /**
     * Findet Bikeparts nach Shop
     */
    List<Bikepart> findByLastPurchaseShopIgnoreCase(String shopName);

    /**
     * Findet alle Ketten (CHAIN) eines Accounts
     */
    @Query("SELECT bp FROM Bikepart bp WHERE bp.bike.account.id = :accountId AND bp.type = :type")
    List<Bikepart> findByAccountIdAndType(@Param("accountId") Long accountId, @Param("type") BikepartType type);

//    /**
//     * Zählt die Anzahl der Bikeparts eines Bikes
//     */
//    Long countByBikeId(Long bikeId);

    /**
     * Findet Bikeparts mit spezifischen Details (z.B. "10-fach")
     */
    @Query("SELECT bp FROM Bikepart bp WHERE bp.bike.id = :bikeId AND " +
           "LOWER(bp.specificDetails) LIKE LOWER(CONCAT('%', :details, '%'))")
    List<Bikepart> findByBikeIdAndSpecificDetails(@Param("bikeId") Long bikeId, @Param("details") String details);

    /**
     * Findet Bikeparts nach Reifenbreite
     */
    List<Bikepart> findByTireWidth(Integer tireWidth);

    /**
     * Findet die neuesten Bikeparts (nach Kaufdatum)
     */
    @Query("SELECT bp FROM Bikepart bp WHERE bp.bike.account.id = :accountId ORDER BY bp.lastPurchaseDate DESC")
    List<Bikepart> findRecentPurchasesByAccountId(@Param("accountId") Long accountId);
}
