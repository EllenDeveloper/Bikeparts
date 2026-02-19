package com.bikeparts.repository;

import com.bikeparts.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CartItem entity
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Findet alle Items in einem bestimmten Warenkorb
     */
    List<CartItem> findByCartId(Long cartId);

    /**
     * Findet alle Items die ein bestimmtes Bikepart referenzieren
     */
    List<CartItem> findByBikepartId(Long bikepartId);

    /**
     * Findet ein spezifisches Item in einem Warenkorb (über Bikepart)
     */
    Optional<CartItem> findByCartIdAndBikepartId(Long cartId, Long bikepartId);

    /**
     * Prüft ob ein Bikepart bereits im Warenkorb ist
     */
    boolean existsByCartIdAndBikepartId(Long cartId, Long bikepartId);

    /**
     * Zählt die Anzahl der Items in einem Warenkorb
     */
    Long countByCartId(Long cartId);

    /**
     * Findet alle Items ohne Bikepart-Referenz (manuell eingetragen)
     */
    List<CartItem> findByCartIdAndBikepartIsNull(Long cartId);

    /**
     * Findet alle Items mit Bikepart-Referenz
     */
    List<CartItem> findByCartIdAndBikepartIsNotNull(Long cartId);

    /**
     * Löscht alle Items eines Warenkorbs
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);

    /**
     * Löscht ein spezifisches Item
     */
    void deleteByCartIdAndBikepartId(Long cartId, Long bikepartId);

    /**
     * Berechnet die Gesamtmenge aller Items in einem Warenkorb
     */
    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer sumQuantityByCartId(@Param("cartId") Long cartId);

    /**
     * Findet Items nach Produktname (Suche)
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND " +
           "(LOWER(ci.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ci.bikepart.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<CartItem> searchByProductName(@Param("cartId") Long cartId, @Param("searchTerm") String searchTerm);
}
