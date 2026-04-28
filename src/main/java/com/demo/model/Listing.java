package com.demo.model;

import jakarta.persistence.*;
import lombok.*;
//import org.apache.catalina.User;

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

public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@Column(nullable = false)
    private String title;
    @Column(length=500) //aumenta espacio a 500 caracteres vs 256 por defecto
    private String shortDescription;
    @Column(columnDefinition="TEXT") //permite almacenar textos largos sin límite de caracteres, ideal para descripciones detalladas
    private String longDescription;

    private Double pricePerNight;
    private Integer minNights;
    private Integer maxNights;
    private Integer maxGuests;


    @Builder.Default
    private Boolean isActive = true;

    //@Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    //@ManyToOne
    //private User user;



}
