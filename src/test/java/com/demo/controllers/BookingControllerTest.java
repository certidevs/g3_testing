package com.demo.controllers;

import com.demo.model.Booking;
import com.demo.model.Listing;
import com.demo.model.User;
import com.demo.model.enums.BookingStatus;
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
    User user;

    @BeforeEach
    void setUp(){
        userRepository.deleteAll();
        listingRepository.deleteAll();
        bookingRepository.deleteAll();

        user = User.builder().name("Juan").email("juanito@gmail.com").build();
        userRepository.save(user);

        apartamento = Listing.builder().title("Casa en la playa").isActive(true).owner(user).maxGuests(5).minNights(2).maxNights(10).registeredAt(LocalDateTime.now()).pricePerNight(25.0).build();
        listingRepository.save(apartamento);

        b1= Booking.builder().listing(apartamento).status(BookingStatus.CONFIRMED).checkIn(LocalDateTime.of(2026,4,22,15,30)).checkOut(LocalDateTime.of(2026,4,26,15,30)).build();
        b2= Booking.builder().listing(apartamento).status(BookingStatus.PENDING).checkIn(LocalDateTime.of(2026,3,22,15,30)).checkOut(LocalDateTime.of(2026,3,26,15,30)).build();
        b3 = Booking.builder().listing(apartamento).status(BookingStatus.PENDING).checkIn(LocalDateTime.now()).checkOut(LocalDateTime.now()).build();
        List<Booking> ap = List.of(b1,b2,b3);
        bookingRepository.saveAll(ap);

    }

    @Test
    void detectarBooking() throws Exception{
        assertTrue(bookingRepository.findById(b1.getId()).isPresent());
        mockMvc.perform(get("/bookings/" + b1.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/booking-detail"))
                .andExpect(model().attributeExists("booking"))
                .andExpect(model().attributeExists("listing"));

    }

    @Test
    void listaBooking() throws Exception{
        assertFalse(bookingRepository.findAll().isEmpty());
        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking/booking-list"))
                .andExpect(model().attributeExists("bookings"))
                .andExpect(model().attribute("bookings", hasSize(3)));



    }

    @Test
    void borrarBooking() throws Exception{
        assertTrue(bookingRepository.findById(b1.getId()).isPresent());

        mockMvc.perform(post("/bookings/"+b1.getId()+"/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Reserva y entidades relacionadas eliminadas exitosamente."));

        assertFalse(bookingRepository.findById(b1.getId()).isPresent());

    }

    @Test
    void confirmarBooking()throws Exception{
        assertTrue(bookingRepository.findById(b2.getId()).isPresent());

        mockMvc.perform(post("/bookings/"+b2.getId()+"/confirm"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/"+b2.getId()))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Reserva confirmada exitosamente."));


        assertEquals(BookingStatus.CONFIRMED, bookingRepository.findById(b2.getId()).get().getStatus());
    }

    @Test
    void cancelarBooking()throws Exception{
        assertTrue(bookingRepository.findById(b3.getId()).isPresent());

        mockMvc.perform(post("/bookings/"+b3.getId()+"/cancel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/"+b3.getId()))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Reserva cancelada exitosamente."));

        assertEquals(BookingStatus.CANCELED, bookingRepository.findById(b3.getId()).get().getStatus());


    }

    @Test
    void crearBooking() throws Exception {
        long bookingsAntes = bookingRepository.count();

        mockMvc.perform(post("/bookings")
                        .param("listing.id", String.valueOf(apartamento.getId()))
                        .param("checkIn", "2026-06-01")
                        .param("checkOut", "2026-06-05"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/bookings/*"))
                .andExpect(flash().attributeExists("message"));

        assertEquals(bookingsAntes + 1, bookingRepository.count());

        // Verificar que se guardó con los datos correctos
        List<Booking> todas = bookingRepository.findAll();
        Booking nueva = todas.stream()
                .filter(b -> b.getCheckIn().equals(LocalDateTime.of(2026, 6, 1, 15, 0)))
                .findFirst()
                .orElseThrow();

        assertEquals(BookingStatus.PENDING, nueva.getStatus());
        assertEquals(apartamento.getId(), nueva.getListing().getId());
        assertEquals(100.0, nueva.getTotalPrice()); // 4 noches * 25.0
    }


}