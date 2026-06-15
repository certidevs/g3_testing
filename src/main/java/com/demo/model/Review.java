package com.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalDateTime;

@ToString
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Setter
@Getter
@Table(name = "reviews")
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La puntuación es obligatoria")
    @Min(1)
    @Max(5)
    private Integer rating;

    @NotBlank(message = "El comentario no puede estar vacío")
    @Column(length = 1000)
    private String comment;

    // poner false pro defecto
    @Builder.Default
    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean verified = false;

    private LocalDateTime creationDate; // LocalDateTime
    //private LocalDate modifiedDate;


    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

}
