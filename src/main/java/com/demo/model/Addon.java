package com.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "addon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Addon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column (length = 1000)
    private String description;
    private Double price;
}
