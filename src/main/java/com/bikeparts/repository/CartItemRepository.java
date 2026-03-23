package com.bikeparts.repository;

import com.bikeparts.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * Findet alle Items eines Warenkorbs
     */
    List<CartItem> findByCartId(Long cartId);

    /**
     * Laedt alle CartItems eines Warenkorbs mit ihrem Bikepart in einem SQL-Statement (JOIN FETCH).
     * Vermeidet LazyInitializationException beim Zugriff auf cartItem.getBikepart()
     * ausserhalb einer Hibernate-Session.
     */
    @Query("SELECT ci FROM CartItem ci LEFT JOIN FETCH ci.bikepart WHERE ci.cart.id = :cartId")
    List<CartItem> findByCartIdWithBikepart(@Param("cartId") Long cartId);

    /**
     * Laedt ein CartItem mit seinem Bikepart in einem SQL-Statement (JOIN FETCH).
     * Vermeidet LazyInitializationException beim Zugriff auf cartItem.getBikepart()
     * ausserhalb einer Hibernate-Session.
     */
    @Query("SELECT ci FROM CartItem ci LEFT JOIN FETCH ci.bikepart WHERE ci.id = :id")
    Optional<CartItem> findByIdWithBikepart(@Param("id") Long id);

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
