package com.demo.repositories;

import com.demo.model.Booking;
import com.demo.model.Conversation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
public class ConversationRepositoryTest {

    @Autowired BookingRepository bookingRepository;
    @Autowired ConversationRepository conversationRepository;

    @BeforeEach
    void setUp(){
        conversationRepository.deleteAll();
        bookingRepository.deleteAll();

        Booking booking = new Booking();
        bookingRepository.save(booking);
        Conversation conversation = new Conversation();
        conversation.setBooking(booking);
        conversationRepository.save(conversation);
    }

    @Test
    void conversationExists(){
        Conversation conversation = conversationRepository.findAll().get(0);
        assertNotNull(conversation);
    }

    @Test
    void onlyOneConversationPerBooking(){
        Booking booking = new Booking();
        bookingRepository.save(booking);
        Conversation conversation = new Conversation();
        Conversation conversation1 = new Conversation();

        assertThrows(Exception.class, () -> {
            conversation.setBooking(booking);
            conversationRepository.save(conversation);
            conversation1.setBooking(booking);
            conversationRepository.save(conversation1);
        });
    }

    @Test
    void getConversarionByBooking(){
        Booking booking = bookingRepository.findAll().get(0);
        Conversation conversation = conversationRepository.findByBookingId(booking.getId());
        assertNotNull(conversation);
    }
}
