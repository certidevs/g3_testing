package com.demo.controllers;

import com.demo.model.Booking;
import com.demo.model.Review;
import com.demo.model.enums.BookingStatus;
import com.demo.repositories.BookingRepository;
import com.demo.repositories.ReviewRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewControllerTest {

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    MockMvc mockMvc;

    Review rev1;

    Booking b1;

    @BeforeEach
    void setUp(){
        reviewRepository.deleteAll();
        bookingRepository.deleteAll();

        b1= Booking.builder().status(BookingStatus.CONFIRMED).build();

        bookingRepository.save(b1);

        rev1= Review.builder().creationDate(LocalDate.now()).booking(b1).build();
        reviewRepository.save(rev1);
    }

    @Test
    void detectarReview(){
        assertTrue(reviewRepository.findById(rev1.getId()).isPresent());
    }

    @Test
    void eliminarReview(){
        reviewRepository.deleteById(rev1.getId());
        assertFalse(reviewRepository.findById(rev1.getId()).isPresent());
    }





}