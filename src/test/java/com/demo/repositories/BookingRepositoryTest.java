package com.demo.repositories;

import com.demo.model.Booking;
import com.demo.model.Listing;
import com.demo.model.enums.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    ListingRepository listingRepository;

    Listing casa1;
    Listing casa2;
    Booking res1;
    Booking res2;
    Booking res3;
    Booking res4;


    @BeforeEach
    void setUp(){
        bookingRepository.deleteAll();
        listingRepository.deleteAll();

        casa1 = Listing.builder().title("Casa del Pardo").pricePerNight(55.0).registeredAt(LocalDateTime.of(2026,3,22,17,15)).isActive(true).build();
        casa2 = Listing.builder().title("Casa de la playa").pricePerNight(42.0).registeredAt(LocalDateTime.now()).isActive(true).build();
        List<Listing> casas = List.of(casa1, casa2);
        listingRepository.saveAll(casas);

        res1 = Booking.builder().listing(casa1).status(BookingStatus.CONFIRMED).checkOut(LocalDateTime.now()).checkIn(LocalDateTime.of(2026,4,20,15,20)).build();
        res2 = Booking.builder().listing(casa1).status(BookingStatus.PENDING).build();
        res3= Booking.builder().listing(casa2).status(BookingStatus.CONFIRMED).build();
        res4 = Booking.builder().listing(casa2).status(BookingStatus.CONFIRMED).build();
        List<Booking> reservas = List.of(res1, res2, res3, res4);
        bookingRepository.saveAll(reservas);

    }

    @Test
    void findAll(){
        List<Booking> bookings = bookingRepository.findAll();
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
    }

    //ver si es mejor usar LocalDateTime o solo LocalDate para los calculos
    //ver si es mejor usar una query en el calculo del precio total o dejarlo asi de momento

    @Test
    @DisplayName("Test para calcular y actualizar el totalPrice de una reserva")
    void updateTotalPrice(){

        long days = ChronoUnit.DAYS.between(res1.getCheckIn(), res1.getCheckOut());
        System.out.println(days);
        res1.setTotalPrice(res1.getListing().getPricePerNight() * days);
        bookingRepository.save(res1);
        System.out.println(res1.getTotalPrice());


    }

    @Test
    @DisplayName("Buscar por id de la casa")
    void findByListingId(){
        List<Booking> bookings = bookingRepository.findByListingId(casa1.getId());
        assertNotNull(bookings);
        assertEquals(2, bookings.size());
    }

    @Test
    @DisplayName("Buscar por id de la casa y estado confirmado")
    void findByListingIdAndBookingStatusConfirmed(){
        List<Booking>bookings = bookingRepository.findByListingIdAndStatus(casa1.getId(), BookingStatus.CONFIRMED);
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
    }

    //Buscar todas las reseveras entre una fecha y otra o solo hacerlo de una reserva
    //Buscar todas las resevas de una casa de una fecha y otra




}