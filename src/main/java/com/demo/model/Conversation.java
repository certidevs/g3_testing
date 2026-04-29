package com.demo.model;

import jakarta.persistence.*;
import lombok.*;

/***
 * @Author: Mohamed Afallah
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "conversations")
@ToString
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @ToString.Exclude
    private Booking booking;
}
