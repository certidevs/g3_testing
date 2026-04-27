package com.demo.model;

import com.demo.model.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "bookings")
@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime checkIn;
    private LocalDateTime chekOut;

    private Double totalPrice;

    private BookingStatus status;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "listing_id")
    private Listing listing;

}
