package com.demo.repositories;

import com.demo.model.Booking;
import com.demo.model.Listing;
import com.demo.model.enums.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    ListingRepository listingRepository;

    Listing casa1;
    Booking reserva1;


    @BeforeEach
    void setUp(){
        bookingRepository.deleteAll();
        listingRepository.deleteAll();

        casa1 = Listing.builder().title("Casa del Pardo").pricePerNight(55.0).isActive(true).build();
        listingRepository.save(casa1);
        reserva1 = Booking.builder().listing(casa1).status(BookingStatus.CONFIRMED).build();
        bookingRepository.save(reserva1);

    }

}