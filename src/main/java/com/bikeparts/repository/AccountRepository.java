package com.bikeparts.repository;

import com.bikeparts.entity.Account;
import com.bikeparts.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Account entity
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {


    /**
     * Findet Account nach Email
     */
    Optional<Account> findByEmail(String email);

    
//    /**
//     * Prüft ob Accountname bereits existiert
//     */
//    boolean existsByAccountname(String accountname);
//
    /**
     * Prüft ob Email bereits existiert
     */
    boolean existsByEmail(String email);
//
//    /**
//     * Findet alle aktiven Account
//     */
//    List<Account> findByIsActiveTrue();
//
//    /**
//     * Findet alle inaktiven Account
//     */
//    List<Account> findByIsActiveFalse();
//
//    /**
//     * Findet Account nach Rolle
//     */
//    List<Account> findByRole(AccountRole role);
//
//    /**
//     * Findet alle Admins
//     */
//    List<Account> findByRoleAndIsActiveTrue(AccountRole role);
//
//    /**
//     * Findet Account die nach einem bestimmten Datum erstellt wurden
//     */
//    List<Account> findByCreatedAtAfter(LocalDateTime date);
//
//
//    /**
//     * Zählt aktive Account
//     */
//    Long countByIsActiveTrue();
//
}
