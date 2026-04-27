package com.demo.model;

import jakarta.persistence.*;
import lombok.*;
//import org.apache.catalina.User;

import java.time.LocalDateTime;

/**
 * Entidad que representa una propiedad publicada en la plataforma.
 * */

@Entity
@Table(name = "amenities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@Column(nullable = false)
    private String name;
    @Column(length=500) //aumenta espacio a 500 caracteres vs 256 por defecto
    private String description;
    @Column(columnDefinition="TEXT") //permite almacenar textos largos sin límite de caracteres, ideal para descripciones detalladas
    private String icon;

    //@ManyToOne
    //private User user;



}