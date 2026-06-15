package com.demo.controllers;

import com.demo.model.Booking;
import com.demo.model.Listing;
import com.demo.model.User;
import com.demo.model.enums.BookingStatus;
import com.demo.model.enums.Role;
import com.demo.repositories.BookingRepository;
import com.demo.repositories.ListingRepository;
import com.demo.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

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
@Transactional
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    ListingRepository listingRepository;

    @Autowired
    MockMvc mockMvc;

    Booking b1;
    Booking b2;
    Booking b3;
    Listing apartamento;
    User admin;
    User userNormal;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();

        admin = User.builder()
                .name("Juan")
                .email("juanito@gmail.com")
                .username("juan")
                .password("$2a$10$JB44S6i2C4t67VquEOpiIua1lDDXmLv54NDnnCKBoC/E6WjHyk.Ei")
                .role(Role.ROLE_ADMIN)
                .build();
        userRepository.save(admin);

        userNormal = User.builder()
                .name("Pedro")
                .email("pedro@gmail.com")
                .username("pedro")
                .password("$2a$10$JB44S6i2C4t67VquEOpiIua1lDDXmLv54NDnnCKBoC/E6WjHyk.Ei")
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(userNormal);

        apartamento = Listing.builder()
                .title("Casa en la playa")
                .isActive(true)
                .owner(admin)
                .maxGuests(5)
                .minNights(2)
                .maxNights(10)
                .registeredAt(LocalDateTime.now())
                .pricePerNight(25.0)
                .build();
        listingRepository.save(apartamento);

        b1 = Booking.builder()
                .listing(apartamento)
                .status(BookingStatus.CONFIRMED)
                .checkIn(LocalDateTime.of(2026, 4, 22, 15, 0))
                .checkOut(LocalDateTime.of(2026, 4, 26, 15, 0))
                .guest(admin)
                .build();
        b2 = Booking.builder()
                .guest(admin)
                .listing(apartamento)
                .status(BookingStatus.PENDING)
                .checkIn(LocalDateTime.of(2026, 3, 22, 15, 0))
                .checkOut(LocalDateTime.of(2026, 3, 26, 15, 0))
                .build();
        b3 = Booking.builder()
                .guest(admin)
                .listing(apartamento)
                .status(BookingStatus.PENDING)
                .checkIn(LocalDateTime.of(2026, 7, 1, 15, 0))
                .checkOut(LocalDateTime.of(2026, 7, 5, 15, 0))
                .build();
        bookingRepository.saveAll(List.of(b1, b2, b3));
    }

    // ── GET /bookings/{id} ──────────────────────────────────────────────────

    @Test
    void detectarBooking() throws Exception {
        assertTrue(bookingRepository.findById(b1.getId()).isPresent());
        mockMvc.perform(get("/bookings/" + b1.getId()).with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/booking-detail"))
                .andExpect(model().attributeExists("booking"))
                .andExpect(model().attributeExists("listing"));
    }

    // ── GET /bookings ───────────────────────────────────────────────────────

    @Test
    void listaBooking() throws Exception {
        assertFalse(bookingRepository.findAll().isEmpty());
        mockMvc.perform(get("/bookings").with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/booking-list"))
                .andExpect(model().attributeExists("bookings"))
                .andExpect(model().attribute("bookings", hasSize(3)));
    }

    // ── DELETE /bookings/{id}/delete ────────────────────────────────────────

    @Test
    void borrarBooking() throws Exception {
        assertTrue(bookingRepository.findById(b1.getId()).isPresent());

        mockMvc.perform(post("/bookings/" + b1.getId() + "/delete")
                        .with(csrf()).with(user(admin)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"))
                .andExpect(flash().attribute("message", "Reserva y entidades relacionadas eliminadas exitosamente."));

        assertFalse(bookingRepository.findById(b1.getId()).isPresent());
    }

    // ── POST /bookings/{id}/confirm ─────────────────────────────────────────

    @Test
    void confirmarBookingPendiente() throws Exception {
        mockMvc.perform(post("/bookings/" + b2.getId() + "/confirm")
                        .with(csrf()).with(user(admin)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/" + b2.getId()))
                .andExpect(flash().attribute("message", "Reserva confirmada exitosamente."));

        assertEquals(BookingStatus.CONFIRMED, bookingRepository.findById(b2.getId()).get().getStatus());
    }

    @Test
    void confirmarBookingYaConfirmadaDevuelveError() throws Exception {
        // b1 ya está CONFIRMED → no debe confirmarse de nuevo
        mockMvc.perform(post("/bookings/" + b1.getId() + "/confirm")
                        .with(csrf()).with(user(admin)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/" + b1.getId()))
                .andExpect(flash().attributeExists("error"));

        // El estado no debe haber cambiado
        assertEquals(BookingStatus.CONFIRMED, bookingRepository.findById(b1.getId()).get().getStatus());
    }

    @Test
    void confirmarBookingSinPermisoDevuelveError() throws Exception {
        // userNormal no es ADMIN ni HOST propietario → no puede confirmar
        mockMvc.perform(post("/bookings/" + b2.getId() + "/confirm")
                        .with(csrf()).with(user(userNormal)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/" + b2.getId()))
                .andExpect(flash().attributeExists("error"));

        // La reserva sigue PENDING
        assertEquals(BookingStatus.PENDING, bookingRepository.findById(b2.getId()).get().getStatus());
    }

    // ── POST /bookings/{id}/cancel ──────────────────────────────────────────

    @Test
    void cancelarBookingPendiente() throws Exception {
        mockMvc.perform(post("/bookings/" + b3.getId() + "/cancel")
                        .with(csrf()).with(user(admin)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/" + b3.getId()))
                .andExpect(flash().attribute("message", "Reserva cancelada exitosamente."));

        assertEquals(BookingStatus.CANCELED, bookingRepository.findById(b3.getId()).get().getStatus());
    }

    @Test
    void cancelarBookingYaCanceladaDevuelveError() throws Exception {
        // Cancelamos b3 primero
        b3.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(b3);

        mockMvc.perform(post("/bookings/" + b3.getId() + "/cancel")
                        .with(csrf()).with(user(admin)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));

        assertEquals(BookingStatus.CANCELED, bookingRepository.findById(b3.getId()).get().getStatus());
    }

    // ── POST /bookings (crear) ──────────────────────────────────────────────

    @Test
    void crearBookingHappyPath() throws Exception {
        long bookingsAntes = bookingRepository.count();

        mockMvc.perform(post("/bookings")
                        .with(csrf()).with(user(admin))
                        .param("listing.id", String.valueOf(apartamento.getId()))
                        .param("checkIn", "2026-06-01")
                        .param("checkOut", "2026-06-05"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/bookings/*"))
                .andExpect(flash().attributeExists("message"));

        assertEquals(bookingsAntes + 1, bookingRepository.count());

        Booking nueva = bookingRepository.findAll().stream()
                .filter(b -> b.getCheckIn().equals(LocalDateTime.of(2026, 6, 1, 15, 0)))
                .findFirst()
                .orElseThrow();

        assertEquals(BookingStatus.PENDING, nueva.getStatus());
        assertEquals(apartamento.getId(), nueva.getListing().getId());
        assertEquals(100.0, nueva.getTotalPrice()); // 4 noches * 25.0
    }

    @Test
    void crearBookingCheckOutAntesQueCheckInDevuelveError() throws Exception {
        long bookingsAntes = bookingRepository.count();

        mockMvc.perform(post("/bookings")
                        .with(csrf()).with(user(admin))
                        .param("listing.id", String.valueOf(apartamento.getId()))
                        .param("checkIn", "2026-09-10")
                        .param("checkOut", "2026-09-05"))   // checkOut < checkIn
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));

        assertEquals(bookingsAntes, bookingRepository.count());
    }

    @Test
    void crearBookingMenosDeMinNochesDevuelveError() throws Exception {
        // minNights = 2, reservamos solo 1 noche
        long bookingsAntes = bookingRepository.count();

        mockMvc.perform(post("/bookings")
                        .with(csrf()).with(user(admin))
                        .param("listing.id", String.valueOf(apartamento.getId()))
                        .param("checkIn", "2026-09-01")
                        .param("checkOut", "2026-09-02"))   // 1 noche < minNights(2)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));

        assertEquals(bookingsAntes, bookingRepository.count());
    }

    @Test
    void crearBookingConConflictoFechasDevuelveError() throws Exception {
        // b1 ocupa 2026-04-22 → 2026-04-26 (CONFIRMED); intentamos solapar
        long bookingsAntes = bookingRepository.count();

        mockMvc.perform(post("/bookings")
                        .with(csrf()).with(user(admin))
                        .param("listing.id", String.valueOf(apartamento.getId()))
                        .param("checkIn", "2026-04-23")
                        .param("checkOut", "2026-04-27"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));

        assertEquals(bookingsAntes, bookingRepository.count());
    }
}