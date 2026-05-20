package com.demo.controllers;

import com.demo.model.Booking;
import com.demo.model.Listing;
import com.demo.model.Review;
import com.demo.model.enums.BookingStatus;
import com.demo.repositories.BookingRepository;
import com.demo.repositories.ListingRepository;
import com.demo.repositories.ReviewRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewControllerTest {

    @Autowired
    ListingRepository listingRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    MockMvc mockMvc;

    Review rev1;
    Listing ap;
    Booking b1;
    Booking b2;

    @BeforeEach
    void setUp(){
        listingRepository.deleteAll();
        bookingRepository.deleteAll();
        reviewRepository.deleteAll();

        ap = Listing.builder().title("Casa en la Playa").maxNights(7).minNights(2).maxGuests(4).pricePerNight(40.0).longDescription("Bien ubicado en la ciudad de Barcelona").shortDescription("Buen sitio").build();
        listingRepository.save(ap);
        b1= Booking.builder().checkIn(LocalDateTime.now()).checkOut(LocalDateTime.of(2026,5,17,15,30)).listing(ap).status(BookingStatus.CONFIRMED).build();
        b2 = Booking.builder().listing(ap).status(BookingStatus.CONFIRMED).checkIn(LocalDateTime.of(2026,5,10,15,30)).checkOut(LocalDateTime.of(2026,5,13,15,30)).build();
        List<Booking> bookings = List.of(b1,b2);
        bookingRepository.saveAll(bookings);

        rev1= Review.builder().creationDate(LocalDate.now()).rating(5).comment("La mejor experiencia de mi vida").booking(b1).build();
        reviewRepository.save(rev1);
    }

    @Test
    void allReviews() throws Exception {
        mockMvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andExpect(view().name("review/review-list"))
                .andExpect(model().attributeExists("reviews"))
                .andExpect(model().attributeExists("bookings"));
    }

    @Test
    void detectarReview() throws Exception{
        assertTrue(reviewRepository.findById(rev1.getId()).isPresent());
    }

    @Test
    void eliminarReview() throws Exception {
        reviewRepository.deleteById(rev1.getId());
        assertFalse(reviewRepository.findById(rev1.getId()).isPresent());

        mockMvc.perform(get("/reviews/delete/" + rev1.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviews"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Borrado exitosamente"));


    }

    @Test
    void obtenerReviewsPorListing() throws Exception {

        mockMvc.perform(get("/listing/" + ap.getId() + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(view().name("review/review-list"))
                .andExpect(model().attributeExists("reviews"));
    }

    @Test
    void crearReview() throws Exception {

        long reviewsAntes = reviewRepository.count();

        mockMvc.perform(post("/reviews")
                        .param("booking.id", String.valueOf(b2.getId()))
                        .param("rating", "4")
                        .param("comment", "Apartamento muy limpio y bien ubicado"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/reviews/*"))
                .andExpect(flash().attribute("message", "Reseña creada exitosamente"));

        assertEquals(reviewsAntes + 1, reviewRepository.count());

        Review guardada = reviewRepository.findAll().stream()
                .filter(r -> r.getBooking().getId().equals(b2.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(4, guardada.getRating());
        assertEquals("Apartamento muy limpio y bien ubicado", guardada.getComment());
        assertTrue(guardada.getVerified());
        assertEquals(LocalDate.now(), guardada.getCreationDate());
    }

    @Test
    void searchReviewsByListingId() throws Exception{

    }






}