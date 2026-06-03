package com.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@Entity
@Table(name = "amenity_lines")
@AllArgsConstructor
@ToString
public class AmenityLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    @ToString.Exclude
    @ManyToOne
    private Amenity amenity;

    @ToString.Exclude
    @ManyToOne
    private Listing listing;
}