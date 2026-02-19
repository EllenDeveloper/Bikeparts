package com.bikeparts.repository;

import com.bikeparts.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Cart entity
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Findet alle Warenkörbe eines bestimmten Accounts
     */
    List<Cart> findByAccountId(Long accountId);

    /**
     * Findet alle aktiven Warenkörbe eines Accounts
     */
    List<Cart> findByAccountIdAndIsActiveTrue(Long accountId);

    /**
     * Findet den aktiven Warenkorb eines Accounts (sollte nur einer sein)
     */
    Optional<Cart> findByAccountIdAndIsActive(Long accountId, Boolean isActive);

    /**
     * Findet Warenkorb nach Account-ID und Name
     */
    Optional<Cart> findByAccountIdAndName(Long accountId, String name);

    /**
     * Prüft ob ein Account bereits einen aktiven Warenkorb hat
     */
    boolean existsByAccountIdAndIsActiveTrue(Long accountId);

    /**
     * Zählt die Anzahl der Artikel in einem Warenkorb
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Long countItemsByCartId(@Param("cartId") Long cartId);

    /**
     * Findet alle Warenkörbe mit Artikeln (nicht leer)
     */
    @Query("SELECT DISTINCT c FROM Cart c JOIN c.cartItems ci WHERE c.account.id = :accountId")
    List<Cart> findNonEmptyCartsByAccountId(@Param("accountId") Long accountId);

    /**
     * Löscht alle inaktiven Warenkörbe eines Accounts
     */
    void deleteByAccountIdAndIsActiveFalse(Long accountId);
}
