package com.bikeparts.repository;

import com.bikeparts.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    /**
     * Laedt Account mit Cart und CartItems in einem SQL-Statement (JOIN FETCH).
     * Vermeidet LazyInitializationException beim Zugriff auf account.getCart()
     * und cart.getCartItems() ausserhalb einer Hibernate-Session.
     */
    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.cart c LEFT JOIN FETCH c.cartItems WHERE a.email = :email")
    Optional<Account> findByEmailWithCart(@Param("email") String email);

    /**
     * Prüft ob Email bereits existiert
     */
    boolean existsByEmail(String email);

}
