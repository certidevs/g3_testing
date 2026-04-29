package com.demo.repositories;

import com.demo.model.Booking;
import com.demo.model.Listing;
import com.demo.model.Review;
import com.demo.model.enums.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ReviewRepositoryTest {

    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    ListingRepository listingRepository;

    Review rev1;
    Review rev2;
    Booking b1;
    Booking b2;
    Listing l1;

    @BeforeEach
    void setUp(){
        reviewRepository.deleteAll();
        bookingRepository.deleteAll();
        listingRepository.deleteAll();

        l1 = Listing.builder().title("Casa del Pardo").pricePerNight(55.0).registeredAt(LocalDateTime.of(2020,2,2,15,22)).isActive(true).build();
        listingRepository.save(l1);

        b1 = Booking.builder().listing(l1).status(BookingStatus.CONFIRMED).checkIn(LocalDateTime.of(2026,4,25,19,14)).checkOut(LocalDateTime.now()).build();
        b2 = Booking.builder().listing(l1).status(BookingStatus.CANCELLED).checkIn(LocalDateTime.of(2026,4,15,20,55)).checkOut(LocalDateTime.of(2026,4,18,20,55)).build();
        List<Booking> bookings = List.of(b1,b2);
        bookingRepository.saveAll(bookings);
        rev1 = Review.builder().comment("Muy buen restaurante").rating(4).verified(true).creationDate(LocalDate.now()).booking(b1).build();
        rev2 = Review.builder().comment("No me ha gustado").rating(1).verified(false).creationDate(LocalDate.of(2026,4,20)).booking(b2).build();
        List<Review>reviews= List.of(rev1, rev2);
        reviewRepository.saveAll(reviews);



    }

    @Test
    @DisplayName("Buscar todas las reviews")
    void findAll(){

        List<Review>lista = reviewRepository.findAll();
        assertNotNull(lista);
        assertEquals(2, lista.size());
        for(Review r : lista){
            System.out.println(r.toString());
        }


    }

    @Test
    @DisplayName("Buscar solo los verificados con true")
    void verificationTrue(){
        List<Review> lista = reviewRepository.findByVerifiedTrue();
        assertNotNull(lista);
        assertEquals(1, lista.size());
        for(Review r : lista){
            System.out.println(r.toString());
        }
    }

    @Test
    @DisplayName("Buscar por solo un review")
    void findByRaiting(){

        List<Review>lista = reviewRepository.findByRating(4);
        assertNotNull(lista);
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Buscar review entre rating")
    void findByRatingBetween(){
        List<Review>lista= reviewRepository.findByRatingBetween(2,5);
        assertNotNull(lista);
        assertEquals(1, lista.size());
        for(Review r : lista){
            System.out.println(r.toString());
        }
    }

    @Test
    @DisplayName("Buscar review entre rating y verificados")
    void findByRatingBetweenAndVerifiedTrue(){
        List<Review>lista= reviewRepository.findByVerifiedTrueAndRatingBetween(2,5);
        assertNotNull(lista);
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Buscar review entre fechas")
    void findByDateBetween(){
        List<Review>lista = reviewRepository.findByCreationDateBetween(LocalDate.of(2020,2,2),LocalDate.now());
        assertNotNull(lista);
        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("Buscar por verified false")
    void findByVerifiedFalse(){
        List<Review>lista = reviewRepository.findByVerifiedFalse();
        assertNotNull(lista);
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Buscar todas las reviews de una casa con su id")
    void findByBooking_ListingId(){
        List<Review>lista = reviewRepository.findByBooking_ListingId(l1.getId());
        assertNotNull(lista);
        assertEquals(2, lista.size());
    }




    //buscar entre diferentes raiting.
    // buscar solo los review que este verificados
    // buscar review entre fechas



}