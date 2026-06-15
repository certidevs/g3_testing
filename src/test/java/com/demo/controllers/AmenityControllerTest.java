package com.demo.controllers;

import com.demo.model.Amenity;
import com.demo.repositories.AmenityLineRepository;
import com.demo.repositories.AmenityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AmenityControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AmenityRepository amenityRepository;

    @Mock
    private AmenityLineRepository amenityLineRepository; // Aunque no se usa en el controlador actual, se inyecta por el @AllArgsConstructor

    @InjectMocks
    private AmenityController amenityController;

    @BeforeEach
    void setUp() {
        // Inicializa MockMvc de manera standalone con el controlador e inyecciones mockeadas
        mockMvc = MockMvcBuilders.standaloneSetup(amenityController).build();
    }

    @Test
    void testAmenity_ShouldReturnListViewAndAmenitiesList() throws Exception {
        // Given
        List<Amenity> amenities = Arrays.asList(new Amenity(), new Amenity());
        when(amenityRepository.findAll()).thenReturn(amenities);

        // When & Then
        mockMvc.perform(get("/amenity"))
                .andExpect(status().isOk())
                .andExpect(view().name("amenity/amenity-list"))
                .andExpect(model().attributeExists("amenities"))
                .andExpect(model().attribute("amenities", amenities));

        verify(amenityRepository, times(1)).findAll();
    }

    @Test
    void testAmenityDetail_ShouldReturnDetailViewAndAmenityObject() throws Exception {
        // Given
        Long id = 1L;
        Amenity amenity = new Amenity();
        when(amenityRepository.findById(id)).thenReturn(Optional.of(amenity));

        // When & Then
        mockMvc.perform(get("/amenity/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("amenity/amenity-detail"))
                .andExpect(model().attributeExists("amenity"))
                .andExpect(model().attribute("amenity", amenity));

        verify(amenityRepository, times(1)).findById(id);
    }

    @Test
    void testShowCreateForm_ShouldReturnFormViewWithEmptyAmenity() throws Exception {
        // When & Then
        mockMvc.perform(get("/amenity/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("amenity/amenity-form"))
                .andExpect(model().attributeExists("amenity"));

        // No interactúa con la base de datos en el GET del formulario
        verifyNoInteractions(amenityRepository);
    }

    @Test
    void testSaveNewAmenity_ShouldSaveAndRedirect() throws Exception {
        // When & Then
        mockMvc.perform(post("/amenity/create")
                        .param("name", "Pool") // Simula los campos que vendrían del formulario HTML
                        .param("description", "Swimming pool"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/amenity"));

        // Verificamos que se haya llamado al método save del repositorio una vez con cualquier objeto Amenity
        verify(amenityRepository, times(1)).save(any(Amenity.class));
    }
}



//
//package com.demo.controllers;
//
//import com.demo.model.Booking;
//import com.demo.model.Listing;
//import com.demo.model.Review;
//import com.demo.model.User;
//import com.demo.model.enums.BookingStatus;
//import com.demo.model.enums.Role;
//import com.demo.repositories.BookingRepository;
//import com.demo.repositories.ListingRepository;
//import com.demo.repositories.ReviewRepository;
//import com.demo.repositories.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.hamcrest.Matchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc(addFilters = false)
//@Transactional
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
//public class AdminControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired private UserRepository userRepository;
//    @Autowired private ListingRepository listingRepository;
//    @Autowired private BookingRepository bookingRepository;
//    @Autowired private ReviewRepository reviewRepository;
//
//    private User admin;
//    private User guest;
//    private Listing listing;
//    private Booking booking;
//    private Review review;
//
//    @BeforeEach
//    void setUp() {
//        reviewRepository.deleteAll();
//        bookingRepository.deleteAll();
//        listingRepository.deleteAll();
//        userRepository.deleteAll();
//
//        admin = User.builder()
//                .username("admin_test")
//                .name("Admin System")
//                .email("admin@test.com")
//                .password("secret123")
//                .role(Role.ROLE_ADMIN)
//                .build();
//
//        guest = User.builder()
//                .username("guest_test")
//                .name("Guest User")
//                .email("guest@test.com")
//                .password("password123")
//                .role(Role.ROLE_USER)
//                .build();
//
//        userRepository.saveAll(List.of(admin, guest));
//
//        admin = userRepository.findById(admin.getId()).orElseThrow();
//        guest = userRepository.findById(guest.getId()).orElseThrow();
//
//        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(admin, null, List.of());
//        SecurityContextHolder.getContext().setAuthentication(auth);
//
//        listing = Listing.builder()
//                .title("Apartamento Centro Histórico")
//                .owner(admin)
//                .pricePerNight(85.0)
//                .isActive(true)
//                .build();
//        listingRepository.save(listing);
//
//        booking = Booking.builder()
//                .checkIn(LocalDateTime.now().plusDays(2))
//                .checkOut(LocalDateTime.now().plusDays(6))
//                .totalPrice(340.0)
//                .status(BookingStatus.CONFIRMED)
//                .guest(guest)
//                .listing(listing)
//                .build();
//        bookingRepository.save(booking);
//
//        review = Review.builder()
//                .rating(5)
//                .comment("Una estancia increíble, todo perfecto.")
//                .verified(true)
//                .creationDate(LocalDate.now())
//                .booking(booking)
//                .build();
//        reviewRepository.save(review);
//    }
//
//    @Test
//    void adminDashboardFullSuccess() throws Exception {
//        mockMvc.perform(get("/dashboard"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("admin/dashboard"))
//                .andExpect(model().attributeExists("totalUsers", "totalListings", "totalBookings", "totalReviews"))
//                .andExpect(model().attribute("totalUsers", is(2L)))
//                .andExpect(model().attribute("totalListings", is(1L)))
//                .andExpect(model().attribute("totalBookings", is(1L)))
//                .andExpect(model().attribute("totalReviews", is(1L)))
//                .andExpect(model().attributeExists("users", "listings", "bookings", "reviews"))
//                .andExpect(model().attribute("users", hasSize(2)))
//                .andExpect(model().attribute("listings", hasSize(1)))
//                .andExpect(model().attribute("bookings", hasSize(1)))
//                .andExpect(model().attribute("reviews", hasSize(1)));
//    }
//
//    @Test
//    void adminDashboardEmptySystem() throws Exception {
//        reviewRepository.deleteAll();
//        bookingRepository.deleteAll();
//        listingRepository.deleteAll();
//        userRepository.deleteAll();
//
//        User uniqueAdmin = User.builder()
//                .username("admin_solitario")
//                .email("admin2@test.com")
//                .role(Role.ROLE_ADMIN)
//                .build();
//        userRepository.save(uniqueAdmin);
//
//        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(uniqueAdmin, null, List.of());
//        SecurityContextHolder.getContext().setAuthentication(auth);
//
//        mockMvc.perform(get("/dashboard"))
//                .andExpect(status().isOk())
//                .andExpect(model().attribute("totalUsers", is(1L)))
//                .andExpect(model().attribute("totalListings", is(0L)))
//                .andExpect(model().attribute("totalBookings", is(0L)))
//                .andExpect(model().attribute("totalReviews", is(0L)))
//                .andExpect(model().attribute("users", hasSize(1)))
//                .andExpect(model().attribute("listings", empty()))
//                .andExpect(model().attribute("bookings", empty()))
//                .andExpect(model().attribute("reviews", empty()));
//    }
//}