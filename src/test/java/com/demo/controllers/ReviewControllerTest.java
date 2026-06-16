package com.demo.controllers;

import com.demo.model.Booking;
import com.demo.model.Listing;
import com.demo.model.Review;
import com.demo.model.enums.BookingStatus;
import com.demo.model.enums.City;
import com.demo.repositories.BookingRepository;
import com.demo.repositories.ListingRepository;
import com.demo.repositories.ReviewRepository;
import com.demo.model.User;
import com.demo.model.enums.Role;
import com.demo.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
    UserRepository userRepository;

    @Autowired
    MockMvc mockMvc;

    Review rev1;
    Listing ap;
    Booking b1;
    Booking b2;
    User admin;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        bookingRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();

        admin = User.builder()
                .name("Admin")
                .email("admin@test.com")
                .username("admin")
                .password("$2a$10$JB44S6i2C4t67VquEOpiIua1lDDXmLv54NDnnCKBoC/E6WjHyk.Ei")
                .role(Role.ROLE_ADMIN)
                .build();
        userRepository.save(admin);

        ap = Listing.builder()
                .title("Casa en la Playa")
                .maxNights(7)
                .minNights(2)
                .maxGuests(4)
                .pricePerNight(40.0)
                .longDescription("Bien ubicado en la ciudad de Barcelona")
                .shortDescription("Buen sitio")
                .city(City.ALICANTE)
                .build();
        listingRepository.save(ap);

        b1 = Booking.builder()
                .checkIn(LocalDateTime.of(2026, 5, 10, 15, 0))
                .checkOut(LocalDateTime.of(2026, 5, 17, 15, 0))
                .listing(ap)
                .status(BookingStatus.CONFIRMED)
                .build();
        b2 = Booking.builder()
                .listing(ap)
                .status(BookingStatus.CONFIRMED)
                .checkIn(LocalDateTime.of(2026, 5, 20, 15, 0))
                .checkOut(LocalDateTime.of(2026, 5, 23, 15, 0))
                .build();
        bookingRepository.saveAll(List.of(b1, b2));

        rev1 = Review.builder()
                .creationDate(LocalDateTime.now())
                .rating(5)
                .comment("La mejor experiencia de mi vida")
                .booking(b1)
                .build();
        reviewRepository.save(rev1);
    }

    // ── GET /reviews ────────────────────────────────────────────────────────

    @Test
    void listarReviews() throws Exception {
        mockMvc.perform(get("/reviews").with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("review/review-list"))
                .andExpect(model().attributeExists("reviews"))
                .andExpect(model().attributeExists("bookings"))
                .andExpect(model().attribute("reviews", hasSize(1)));
    }

    // ── GET /reviews/{id} ───────────────────────────────────────────────────

    @Test
    void detalleReviewExistente() throws Exception {
        mockMvc.perform(get("/reviews/" + rev1.getId()).with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("review/review-detail"))
                .andExpect(model().attributeExists("review"))
                .andExpect(model().attributeExists("booking"));
    }

    @Test
    void detalleReviewInexistenteRedirige() throws Exception {
        mockMvc.perform(get("/reviews/99999").with(user(admin)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviews"));
    }

    // ── POST /reviews/delete/{id} ────────────────────────────────────────────

    @Test
    void eliminarReview() throws Exception {
        assertTrue(reviewRepository.findById(rev1.getId()).isPresent());

        mockMvc.perform(post("/reviews/delete/" + rev1.getId()).with(csrf()).with(user(admin)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviews"))
                .andExpect(flash().attribute("message", "Borrado exitosamente"));

        assertFalse(reviewRepository.findById(rev1.getId()).isPresent());
    }

    // ── GET /listing/{id}/reviews ───────────────────────────────────────────

    @Test
    void obtenerReviewsPorListing() throws Exception {
        mockMvc.perform(get("/listing/" + ap.getId() + "/reviews").with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("review/review-list"))
                .andExpect(model().attributeExists("reviews"));
    }

    // ── POST /reviews (crear) ────────────────────────────────────────────────

    @Test
    void crearReviewHappyPath() throws Exception {
        long reviewsAntes = reviewRepository.count();

        mockMvc.perform(post("/reviews").with(csrf()).with(user(admin))
                        .param("booking.id", String.valueOf(b2.getId()))
                        .param("rating", "4")
                        .param("verified", "true")
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
        assertEquals(LocalDate.now(), guardada.getCreationDate().toLocalDate());
    }

    @Test
    void crearReviewConRatingInvalidoDevuelveError() throws Exception {
        long reviewsAntes = reviewRepository.count();

        mockMvc.perform(post("/reviews").with(csrf()).with(user(admin))
                        .param("booking.id", String.valueOf(b2.getId()))
                        .param("rating", "6")         // fuera de rango 1-5
                        .param("comment", "Comentario válido"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));

        assertEquals(reviewsAntes, reviewRepository.count());
    }

    @Test
    void crearReviewSinComentarioDevuelveError() throws Exception {
        long reviewsAntes = reviewRepository.count();

        mockMvc.perform(post("/reviews").with(csrf()).with(user(admin))
                        .param("booking.id", String.valueOf(b2.getId()))
                        .param("rating", "3")
                        .param("comment", "   "))     // comentario en blanco
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));

        assertEquals(reviewsAntes, reviewRepository.count());
    }

    @Test
    void crearReviewConBookingInexistenteDevuelveError() throws Exception {
        long reviewsAntes = reviewRepository.count();

        mockMvc.perform(post("/reviews").with(csrf()).with(user(admin))
                        .param("booking.id", "99999")  // booking que no existe
                        .param("rating", "3")
                        .param("comment", "Comentario"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));

        assertEquals(reviewsAntes, reviewRepository.count());
    }
}