package com.bikeparts.repository;

import com.bikeparts.entity.Bike;
import com.bikeparts.enums.BikeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Bike entity
 */
@Repository
public interface BikeRepository extends JpaRepository<Bike, Long> {

    /**
     * Findet alle Bikes eines bestimmten Accounts
     */
    List<Bike> findByAccountId(Long accountId);

    /**
     * Findet Bikes nach Typ
     */
    List<Bike> findByType(BikeType type);

    /**
     * Findet alle E-Bikes eines Accounts
     */
    List<Bike> findByAccountIdAndIsEbikeTrue(Long accountId);

    /**
     * Findet alle Nicht-E-Bikes eines Accounts
     */
    List<Bike> findByAccountIdAndIsEbikeFalse(Long accountId);

    /**
     * Findet Bikes nach Typ und Account
     */
    List<Bike> findByAccountIdAndType(Long accountId, BikeType type);

    /**
     * Findet Bike nach Name und Account
     */
    Optional<Bike> findByAccountIdAndName(Long accountId, String name);

    /**
     * Findet Bikes nach Marke
     */
    List<Bike> findByBrandIgnoreCase(String brand);

    /**
     * Findet Bikes nach Account und Marke
     */
    List<Bike> findByAccountIdAndBrandIgnoreCase(Long accountId, String brand);

//    /**
//     * Sucht Bikes nach Name, Marke oder Modell
//     */
//    @Query("SELECT b FROM Bike b WHERE b.account.id = :accountId AND " +
//           "(LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
//           "LOWER(b.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
//           "LOWER(b.modelName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
//    List<Bike> searchBikes(@Param("accountId") Long accountId, @Param("searchTerm") String searchTerm);

    /**
     * Zählt die Anzahl der Bikes eines Accounts
     */
    Long countByAccountId(Long accountId);

    /**
     * Findet Bikes mit bestimmter Radgröße
     */
    List<Bike> findByAccountIdAndWheelSize(Long accountId, Integer wheelSize);

    /**
     * Findet alle Bikes mit Bikeparts (EAGER loading)
     */
    @Query("SELECT DISTINCT b FROM Bike b LEFT JOIN FETCH b.bikeparts WHERE b.account.id = :accountId")
    List<Bike> findByAccountIdWithBikeparts(@Param("accountId") Long accountId);
}
