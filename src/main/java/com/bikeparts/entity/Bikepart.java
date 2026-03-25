package com.bikeparts.entity;

import com.bikeparts.enums.BikepartType;
import com.bikeparts.validation.SafeText;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Bikepart entity (Fahrradteil)
 */
// GraalVM Native Image: BytecodeProvider ist 'none' - Hibernate kann keine
// HibernateProxy-Instanzen zur Laufzeit erzeugen. @Proxy(lazy = false) deaktiviert
// die Proxy-Generierung fuer diese Entity und verhindert den Laufzeitfehler.
@Proxy(lazy = false)
@Entity
@Table(name = "bikeparts")
public class Bikepart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bike_id", nullable = false)
    @JsonIgnore  // Verhindert zirkuläre Serialisierung
    private Bike bike;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BikepartType type;

    @Column(length = 100, nullable = false)
    private String name; // z.B. "Shimano XT CN-HG95"

    @SafeText
    @Column(length = 50)
    private String brand; // z.B. "Shimano"

    @SafeText
    @Column(length = 100)
    private String model;

    @SafeText
    @Column(length = 50)
    private String quality; // z.B. "XT", "Deore", "105"

    @SafeText
    @Column(length = 200)
    private String alternativeQualities; // Komma-getrennt: "LX, SLX"

    @SafeText
    @Column(length = 200)
    private String specificDetails; // z.B. "10-fach" bei Kette/Chain

    @SafeText
    @Column
    private String tireWidth; // Mantelbreite in mm (z.B. 2.25, 2.35)

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Bikepart() {
    }

    public Bikepart(Long id, Bike bike, BikepartType type, String name) {
        this.id = id;
        this.bike = bike;
        this.type = type;
        this.name = name;
      
    }

    
    public Bikepart(Long id, Bike bike, BikepartType type, String name, String brand, String model,
                    String quality, String alternativeQualities, String specificDetails, String tireWidth,
                    String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.bike = bike;
        this.type = type;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.quality = quality;
        this.alternativeQualities = alternativeQualities;
        this.specificDetails = specificDetails;
        this.tireWidth = tireWidth;
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

    public Bike getBike() {
        return bike;
    }

    public void setBike(Bike bike) {
        this.bike = bike;
    }

    public BikepartType getType() {
        return type;
    }

    public void setType(BikepartType type) {
        this.type = type;
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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getAlternativeQualities() {
        return alternativeQualities;
    }

    public void setAlternativeQualities(String alternativeQualities) {
        this.alternativeQualities = alternativeQualities;
    }

    public String getSpecificDetails() {
        return specificDetails;
    }

    public void setSpecificDetails(String specificDetails) {
        this.specificDetails = specificDetails;
    }

    public String getTireWidth() {
        return tireWidth;
    }

    public void setTireWidth(String tireWidth) {
        this.tireWidth = tireWidth;
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

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bikepart bikepart = (Bikepart) o;
        return Objects.equals(id, bikepart.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "Bikepart{" +
                "id=" + id +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", quality='" + quality + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
