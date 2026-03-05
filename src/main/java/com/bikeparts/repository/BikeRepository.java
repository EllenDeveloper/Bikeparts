package com.bikeparts.repository;

import com.bikeparts.entity.Bike;
import com.bikeparts.enums.BikeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Bike entity
 */
@Repository
public interface BikeRepository extends JpaRepository<Bike, Long> {

    /**
     * Findet alle Bikes eines Accounts
     */
    List<Bike> findByAccountId(Long accountId);

    /**
     * Findet Bikes eines Accounts nach Typ
     */
    List<Bike> findByAccountIdAndType(Long accountId, BikeType type);

    /**
     * Zählt die Bikes eines Accounts
     */
    Long countByAccountId(Long accountId);

    Bike getBikeById(Long id);
}
