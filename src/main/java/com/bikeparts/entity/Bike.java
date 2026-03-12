package com.bikeparts.entity;

import com.bikeparts.enums.BikeType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Bike entity (Fahrrad)
 */
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "bikes")
public class Bike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    // for lombok: @ToString.Exclude account will not be included in the toString method
    @JsonIgnore  // Verhindert zirkuläre Serialisierung
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BikeType type;

    @Column(nullable = true)
    private Boolean isEbike = false;

    @Column(length = 100)
    private String name; // Benutzerdefinierter Name

    @Column(length = 50)
    private String brand; // Marke

    @Column(length = 100)
    private String modelName; // Modellname

    @Column
    private Integer wheelSize; // 27, 29 Zoll

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "bike", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Bikepart> bikeparts = new ArrayList<>();

    // Helper-Methoden für bidirectionale Relationship
    public void addBikepart(Bikepart bikepart) {
        bikeparts.add(bikepart);
        bikepart.setBike(this);
    }
    
    public void removeBikepart(Bikepart bikepart) {
        bikeparts.remove(bikepart);
        bikepart.setBike(null);
    }
    
    // Constructors
    public Bike() {
    }

    public Bike(Long id, Account account, BikeType type, String name) {
        this.id = id;
        this.account = account;
        this.type = type;
        this.name = name;
    }
    
    public Bike(Long id, Account account, BikeType type, Boolean isEbike, String name, String brand,
                String modelName, Integer wheelSize, String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.account = account;
        this.type = type;
        this.isEbike = isEbike;
        this.name = name;
        this.brand = brand;
        this.modelName = modelName;
        this.wheelSize = wheelSize;
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BikeType getType() {
        return type;
    }

    public void setType(BikeType type) {
        this.type = type;
    }

    public Boolean getIsEbike() {
        return isEbike;
    }

    public void setIsEbike(Boolean isEbike) {
        this.isEbike = isEbike;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Integer getWheelSize() {
        return wheelSize;
    }

    public void setWheelSize(Integer wheelSize) {
        this.wheelSize = wheelSize;
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

    public List<Bikepart> getBikeparts() {
        return bikeparts;
    }

    public void setBikeparts(List<Bikepart> bikeparts) {
        this.bikeparts = bikeparts;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bike bike = (Bike) o;
        return Objects.equals(id, bike.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "Bike{" +
                "id=" + id +
                ", type=" + type +
                ", isEbike=" + isEbike +
                ", name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", modelName='" + modelName + '\'' +
                ", wheelSize=" + wheelSize +
                ", createdAt=" + createdAt +
                '}';
    }


}
