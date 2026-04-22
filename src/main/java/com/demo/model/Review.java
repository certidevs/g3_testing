package com.demo.model;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

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

    private LocalDate createdAt;


}
