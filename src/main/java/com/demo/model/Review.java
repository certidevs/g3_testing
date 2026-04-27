package com.demo.model;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

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


    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;

    // poner false pro defecto
    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean verified;

    private LocalDate creationDate;
    //private LocalDate modifiedDate;


    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

}
