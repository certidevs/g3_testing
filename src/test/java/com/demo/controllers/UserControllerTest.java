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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private UserRepository userRepository;
    @Autowired private ListingRepository listingRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User hostUser;
    private User guestUser;
    private Listing listing;
    private Booking booking;
    private Review review;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        bookingRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = User.builder().username("admin").name("System Admin").email("admin@test.com").password(passwordEncoder.encode("password")).role(Role.ROLE_ADMIN).build();
        hostUser = User.builder().username("host1").name("Host User").email("host@test.com").password(passwordEncoder.encode("password")).role(Role.ROLE_HOST).build();
        guestUser = User.builder().username("guest1").name("Guest User").email("guest@test.com").password(passwordEncoder.encode("password")).role(Role.ROLE_USER).build();

        userRepository.saveAll(List.of(adminUser, hostUser, guestUser));
        adminUser = userRepository.findById(adminUser.getId()).orElseThrow();
        hostUser = userRepository.findById(hostUser.getId()).orElseThrow();
        guestUser = userRepository.findById(guestUser.getId()).orElseThrow();

        listing = Listing.builder()
                .title("Villa Vista Mar")
                .owner(hostUser)
                .pricePerNight(120.0)
                .isActive(true)
                .build();
        listingRepository.save(listing);

        booking = Booking.builder()
                .checkIn(LocalDateTime.now().plusDays(5))
                .checkOut(LocalDateTime.now().plusDays(10))
                .totalPrice(600.0)
                .status(BookingStatus.CONFIRMED)
                .guest(guestUser)
                .listing(listing)
                .build();
        bookingRepository.save(booking);

        review = Review.builder()
                .rating(4)
                .comment("Muy limpio y excelente ubicación.")
                .verified(true)
                .creationDate(LocalDateTime.now())
                .booking(booking)
                .build();
        reviewRepository.save(review);
    }

    private void autenticarComo(User usuario) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(usuario, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void showUserProfileAsGuest() throws Exception {
        autenticarComo(guestUser);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/user-detail"))
                .andExpect(model().attributeExists("user", "bookings", "reviews", "listings", "hostBookings"))
                .andExpect(model().attribute("bookings", hasSize(1)))
                .andExpect(model().attribute("listings", empty()));
    }

    @Test
    void showUserProfileAsHost() throws Exception {
        autenticarComo(hostUser);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/user-detail"))
                .andExpect(model().attributeExists("user", "bookings", "reviews", "listings", "hostBookings"))
                .andExpect(model().attribute("listings", hasSize(1)))
                .andExpect(model().attribute("hostBookings", hasSize(1)));
    }

    @Test
    void auditUserSuccessAsAdmin() throws Exception {
        autenticarComo(adminUser);

        mockMvc.perform(get("/profile/audit/" + guestUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("ROLE_USER")))
                .andExpect(jsonPath("$.bookings", hasSize(1)))
                .andExpect(jsonPath("$.bookings[0].title", is("Villa Vista Mar")))
                .andExpect(jsonPath("$.bookings[0].totalPrice", is(600.0)))
                .andExpect(jsonPath("$.reviews", hasSize(1)))
                .andExpect(jsonPath("$.reviews[0].comment", is("Muy limpio y excelente ubicación.")));
    }

    @Test
    void auditUserAsAdminTargetingAdmin() throws Exception {
        autenticarComo(adminUser);

        mockMvc.perform(get("/profile/audit/" + adminUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("ROLE_ADMIN")))
                .andExpect(jsonPath("$.bookings", empty()))
                .andExpect(jsonPath("$.reviews", empty()));
    }

    @Test
    void auditUserForbiddenForNonAdmin() throws Exception {
        autenticarComo(guestUser);

        mockMvc.perform(get("/profile/audit/" + hostUser.getId()))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Acceso denegado")));
    }

    @Test
    void auditUserNotFound() throws Exception {
        autenticarComo(adminUser);

        mockMvc.perform(get("/profile/audit/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProfileSuccessWithoutPasswordChange() throws Exception {
        autenticarComo(guestUser);

        mockMvc.perform(post("/profile/update").with(csrf())
                        .param("name", "Nuevo Nombre")
                        .param("username", "nuevousername")
                        .param("email", "nuevoemail@test.com")
                        .param("newPassword", "")
                        .param("confirmPassword", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?success"));

        User updatedUser = userRepository.findById(guestUser.getId()).orElseThrow();
        assertEquals("Nuevo Nombre", updatedUser.getName());
        assertEquals("nuevousername", updatedUser.getUsername());
        assertEquals("nuevoemail@test.com", updatedUser.getEmail());
    }

    @Test
    void updateProfileSuccessWithPasswordChange() throws Exception {
        autenticarComo(guestUser);

        mockMvc.perform(post("/profile/update").with(csrf())
                        .param("name", "Guest User")
                        .param("username", "guest1")
                        .param("email", "guest@test.com")
                        .param("newPassword", "Pass1wrd@")
                        .param("confirmPassword", "Pass1wrd@"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?success"));

        User updatedUser = userRepository.findById(guestUser.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("Pass1wrd@", updatedUser.getPassword()));
    }

    @Test
    void updateProfileValidationErrorUsernameAndEmailInUse() throws Exception {
        autenticarComo(guestUser);

        mockMvc.perform(post("/profile/update").with(csrf())
                        .param("name", "Intento Fallido")
                        .param("username", "host1") // Ya lo tiene el anfitrión
                        .param("email", "host@test.com") // Ya lo tiene el anfitrión
                        .param("newPassword", "")
                        .param("confirmPassword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("user/user-detail"))
                .andExpect(model().attributeExists("usernameError", "emailError"))
                .andExpect(model().attribute("user", hasProperty("name", is("Intento Fallido"))));

        User unchangedUser = userRepository.findById(guestUser.getId()).orElseThrow();
        assertEquals("Guest User", unchangedUser.getName()); // No debió guardarse en DB
    }

    @Test
    void updateProfileValidationErrorShortPassword() throws Exception {
        autenticarComo(guestUser);

        mockMvc.perform(post("/profile/update").with(csrf())
                        .param("name", "Guest User")
                        .param("username", "guest1")
                        .param("email", "guest@test.com")
                        .param("newPassword", "12345") // Menor a 8 caracteres
                        .param("confirmPassword", "12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/user-detail"))
                .andExpect(model().attributeExists("passwordError"));
    }

    @Test
    void updateProfileValidationErrorMismatchedPasswords() throws Exception {
        autenticarComo(guestUser);

        mockMvc.perform(post("/profile/update").with(csrf())
                        .param("name", "Guest User")
                        .param("username", "guest1")
                        .param("email", "guest@test.com")
                        .param("newPassword", "segura12345")
                        .param("confirmPassword", "diferente12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/user-detail"))
                .andExpect(model().attributeExists("passwordError"));
    }
}
