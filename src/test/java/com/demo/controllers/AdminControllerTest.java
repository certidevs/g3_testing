package com.demo.controllers;

import com.demo.model.Booking;
import com.demo.model.Listing;
import com.demo.model.Review;
import com.demo.model.User;
import com.demo.model.enums.BookingStatus;
import com.demo.model.enums.Role;
import com.demo.repositories.BookingRepository;
import com.demo.repositories.ListingRepository;
import com.demo.repositories.ReviewRepository;
import com.demo.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private UserRepository userRepository;
    @Autowired private ListingRepository listingRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ReviewRepository reviewRepository;

    private User admin;
    private User guest;
    private Listing listing;
    private Booking booking;
    private Review review;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        bookingRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();

        admin = User.builder()
                .username("admin_test")
                .name("Admin System")
                .email("admin@test.com")
                .password("secret123")
                .role(Role.ROLE_ADMIN)
                .build();

        guest = User.builder()
                .username("guest_test")
                .name("Guest User")
                .email("guest@test.com")
                .password("password123")
                .role(Role.ROLE_USER)
                .build();

        userRepository.saveAll(List.of(admin, guest));

        admin = userRepository.findById(admin.getId()).orElseThrow();
        guest = userRepository.findById(guest.getId()).orElseThrow();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(admin, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        listing = Listing.builder()
                .title("Apartamento Centro Histórico")
                .owner(admin)
                .pricePerNight(85.0)
                .isActive(true)
                .build();
        listingRepository.save(listing);

        booking = Booking.builder()
                .checkIn(LocalDateTime.now().plusDays(2))
                .checkOut(LocalDateTime.now().plusDays(6))
                .totalPrice(340.0)
                .status(BookingStatus.CONFIRMED)
                .guest(guest)
                .listing(listing)
                .build();
        bookingRepository.save(booking);

        review = Review.builder()
                .rating(5)
                .comment("Una estancia increíble, todo perfecto.")
                .verified(true)
                .creationDate(LocalDateTime.now())
                .booking(booking)
                .build();
        reviewRepository.save(review);
    }

    @Test
    void adminDashboardFullSuccess() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("totalUsers", "totalListings", "totalBookings", "totalReviews"))
                .andExpect(model().attribute("totalUsers", is(2L)))
                .andExpect(model().attribute("totalListings", is(1L)))
                .andExpect(model().attribute("totalBookings", is(1L)))
                .andExpect(model().attribute("totalReviews", is(1L)))
                .andExpect(model().attributeExists("users", "listings", "bookings", "reviews"))
                .andExpect(model().attribute("users", hasSize(2)))
                .andExpect(model().attribute("listings", hasSize(1)))
                .andExpect(model().attribute("bookings", hasSize(1)))
                .andExpect(model().attribute("reviews", hasSize(1)));
    }

    @Test
    void adminDashboardEmptySystem() throws Exception {
        reviewRepository.deleteAll();
        bookingRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();

        User uniqueAdmin = User.builder()
                .username("admin_solitario")
                .email("admin2@test.com")
                .role(Role.ROLE_ADMIN)
                .build();
        userRepository.save(uniqueAdmin);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(uniqueAdmin, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalUsers", is(1L)))
                .andExpect(model().attribute("totalListings", is(0L)))
                .andExpect(model().attribute("totalBookings", is(0L)))
                .andExpect(model().attribute("totalReviews", is(0L)))
                .andExpect(model().attribute("users", hasSize(1)))
                .andExpect(model().attribute("listings", empty()))
                .andExpect(model().attribute("bookings", empty()))
                .andExpect(model().attribute("reviews", empty()));
    }
}