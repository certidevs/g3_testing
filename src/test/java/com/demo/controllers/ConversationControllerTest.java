package com.demo.controllers;

import com.demo.model.Booking;
import com.demo.model.Conversation;
import com.demo.model.Message;
import com.demo.model.User;
import com.demo.model.enums.BookingStatus;
import com.demo.repositories.BookingRepository;
import com.demo.repositories.ConversationRepository;
import com.demo.repositories.MessageRepository;
import com.demo.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ConversationControllerTest {

    @Autowired MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    private User owner;
    private User guest;
    private Booking booking;
    private Conversation conversation;
    private Long id;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        bookingRepository.deleteAll();
        userRepository.deleteAll();

        owner = User.builder().name("Host Test").email("host@test.com").build();
        guest = User.builder().name("Guest Test").email("guest@test.com").build();
        userRepository.saveAll(List.of(owner, guest));

        booking = Booking.builder()
                .checkIn(LocalDateTime.now().plusDays(1))
                .checkOut(LocalDateTime.now().plusDays(5))
                .totalPrice(200.0)
                .status(BookingStatus.CONFIRMED)
                .host(owner)
                .guest(guest)
                .listing(null)
                .build();

        id = booking.getId();
        bookingRepository.save(booking);

        conversation = Conversation.builder()
                .booking(booking)
                .build();
        conversationRepository.save(conversation);

        Message msg = Message.builder()
                .content("Hola, este es un mensaje de prueba")
                .sender(guest)
                .conversation(conversation)
                .sentAt(LocalDateTime.now())
                .build();
        messageRepository.save(msg);
    }

    @Test
    void conversationFull() throws Exception{
        mockMvc.perform(get("/conversation/" + booking.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("conversation/conversation_detail"))
                .andExpect(model().attributeExists("conversation"))
                .andExpect(model().attribute("conversation", hasProperty("id", is(conversation.getId()))));
    }

    @Test
    void conversationNoExists()throws Exception{
        mockMvc.perform(get("/conversation/99999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"));
    }

}