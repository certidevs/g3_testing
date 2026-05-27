package com.demo.model;

import com.demo.model.enums.City;
import com.demo.model.enums.ListingType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

/**
 * Entidad que representa una propiedad publicada en la plataforma.
 * */

@Entity
@Table(name = "listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString

public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length=500) //aumenta espacio a 500 caracteres vs 256 por defecto
    private String shortDescription;

    @Column(length=10000)
    private String longDescription;

    private Double pricePerNight;
    private Integer minNights;
    private Integer maxNights;
    private Integer maxGuests;

    private String imageUrl;

    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Enumerated(EnumType.STRING)
    private ListingType type;

    @Enumerated(EnumType.STRING)
    private City city;

    @ToString.Exclude
    @ManyToOne
    private User owner;

    @PrePersist //Permite que campo no sea nulo al crear un Listing
    public void prePersist() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
    }



}
