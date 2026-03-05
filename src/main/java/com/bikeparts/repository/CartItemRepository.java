package com.bikeparts.repository;

import com.bikeparts.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CartItem entity
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Findet alle Items eines Warenkorbs
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
}
