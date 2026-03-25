package com.bikeparts.entity;

import com.bikeparts.price.entity.ProductOffer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.List;

/**
 * CartItem entity (Warenkorb-Artikel)
 * Zwischentabelle für N:M Beziehung zwischen Cart und Bikepart
 */
// GraalVM Native Image: BytecodeProvider ist 'none' - Hibernate kann keine
// HibernateProxy-Instanzen zur Laufzeit erzeugen. @Proxy(lazy = false) deaktiviert
// die Proxy-Generierung fuer diese Entity und verhindert den Laufzeitfehler.
@Proxy(lazy = false)
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore  // Verhindert zirkuläre Serialisierung
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bikepart_id")
    @JsonIgnore
    private Bikepart bikepart; // nullable - kann auch manuell eingetragen sein

    @OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductOffer> productOffers;

    @Column(length = 200)
    private String productName; // Falls nicht von eigenem Bike

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public CartItem() {
    }

    public CartItem(Cart cart, Bikepart bikepart,
                    Integer quantity) {
        this.cart = cart;
        cart.addCartItem(this);
        this.bikepart = bikepart;
        this.productName = bikepart.getName();
        this.quantity = quantity;
        this.notes = bikepart.getNotes();
    }

    public CartItem(Long id, Cart cart, Bikepart bikepart, String productName,
                    Integer quantity, String notes,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.cart = cart;
        this.bikepart = bikepart;
        this.productName = productName;
        this.quantity = quantity;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Bikepart getBikepart() {
        return bikepart;
    }

    public void setBikepart(Bikepart bikepart) {
        this.bikepart = bikepart;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper method - gibt den Produktnamen zurück (entweder von Bikepart oder manuell)
    public String getEffectiveProductName() {
        if (bikepart != null) {
            return bikepart.getName();
        }
        return productName;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(id, cartItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", productName='" + getEffectiveProductName() + '\'' +
                ", quantity=" + quantity +
                ", createdAt=" + createdAt +
                '}';
    }
}
