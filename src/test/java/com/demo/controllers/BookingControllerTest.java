package com.demo.controllers;

import com.demo.model.Booking;
import com.demo.model.Listing;
import com.demo.model.enums.BookingStatus;
import com.demo.repositories.BookingRepository;
import com.demo.repositories.ListingRepository;
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
    BookingRepository bookingRepository;

    @Autowired
    ListingRepository listingRepository;

    @Autowired
    MockMvc mockMvc;

    Booking b1;
    Booking b2;
    Listing apartamento;

    @BeforeEach
    void setUp(){
        apartamento = Listing.builder().title("Casa en la playa").isActive(true).pricePerNight(25.0).build();
        listingRepository.save(apartamento);

        b1= Booking.builder().listing(apartamento).status(BookingStatus.CONFIRMED).checkIn(LocalDateTime.of(2026,4,22,15,30)).checkOut(LocalDateTime.of(2026,4,26,15,30)).build();
        b2= Booking.builder().listing(apartamento).status(BookingStatus.CONFIRMED).checkIn(LocalDateTime.of(2026,3,22,15,30)).checkOut(LocalDateTime.of(2026,3,26,15,30)).build();

        List<Booking> lista = List.of(b1,b2);
        bookingRepository.saveAll(lista);

    }

    @Test
    void detectarBooking() throws Exception{
        assertTrue(bookingRepository.findById(b1.getId()).isPresent());
        mockMvc.perform(get("/booking/" + b1.getId()))
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
                .andExpect(model().attribute("bookings", hasSize(2)));



    }

    @Test
    void borrarBooking() throws Exception{
        assertTrue(bookingRepository.findById(b1.getId()).isPresent());

        mockMvc.perform(post("/booking/"+b1.getId()+"/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "Reserva y entidades relacionadas eliminadas exitosamente."));

        assertFalse(bookingRepository.findById(b1.getId()).isPresent());

    }


}