package com.bikeparts.repository;

import com.bikeparts.entity.Bikepart;
import com.bikeparts.enums.BikepartType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Bikepart entity
 */
@Repository
public interface BikepartRepository extends JpaRepository<Bikepart, Long> {

    /**
     * Findet alle Bikeparts eines Bikes
     */
    List<Bikepart> findByBikeId(Long bikeId);

    /**
     * Findet Bikeparts eines Bikes nach Typ
     */
    List<Bikepart> findByBikeIdAndType(Long bikeId, BikepartType type);

    /**
     * Findet alle Bikeparts eines Accounts (über Bike)
     */
    @Query("SELECT bp FROM Bikepart bp WHERE bp.bike.account.id = :accountId")
    List<Bikepart> findByAccountId(@Param("accountId") Long accountId);
}
