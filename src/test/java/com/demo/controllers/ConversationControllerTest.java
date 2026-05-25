package com.demo.controllers;

import com.demo.model.*;
import com.demo.model.enums.BookingStatus;
import com.demo.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
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

    @Autowired
    private ListingRepository listingRepository;

    User owner;
    User guest;
    Booking booking;
    Conversation conversation;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        bookingRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();

        owner = User.builder().name("Host Test").email("host@test.com").build();
        guest = User.builder().name("Guest Test").email("guest@test.com").build();
        userRepository.saveAll(List.of(owner, guest));

        Listing listing = Listing.builder()
                .title("Apartamento Test")
                .owner(owner)
                .build();
        listingRepository.save(listing);

        booking = Booking.builder()
                .checkIn(LocalDateTime.now().plusDays(1))
                .checkOut(LocalDateTime.now().plusDays(5))
                .totalPrice(200.0)
                .status(BookingStatus.CONFIRMED)
                .guest(guest)
                .listing(listing)
                .build();
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
        mockMvc.perform(get("/conversation/" + conversation.getId()))
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

    @Test
    void conversationList()throws Exception{
        mockMvc.perform(get("/conversation"))
                .andExpect(status().isOk())
                .andExpect(view().name("conversation/conversation_list"))
                .andExpect(model().attributeExists("conversations"))
                .andExpect(model().attribute("conversations", hasSize(1)));
    }

    @Test
    void createMessageSuccess() throws Exception {
        mockMvc.perform(post("/conversation/" + conversation.getId() + "/send")
                        .param("content", "Mensaje nuevo de prueba")
                        .sessionAttr("loggedInUser", guest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversation/" + conversation.getId()));

        List<Message> messages = messageRepository.findAll();
        boolean exists = messages.stream()
                .anyMatch(m -> m.getContent().equals("Mensaje nuevo de prueba"));

        assertTrue(exists);
    }

    @Test
    void createMessageEmptyContent() throws Exception {
        mockMvc.perform(post("/conversation/" + conversation.getId() + "/send")
                        .param("content", "   "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversation/" + conversation.getId()));
    }

    @Test
    void editMessageSuccess() throws Exception {
        User sender = userRepository.findAll().getFirst();

        Message msg = Message.builder()
                .content("Texto original")
                .sender(sender)
                .conversation(conversation)
                .build();
        messageRepository.save(msg);

        mockMvc.perform(post("/conversation/" + conversation.getId() + "/message/" + msg.getId() + "/edit")
                        .param("content", "Texto editado"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversation/" + conversation.getId()));

        Message updatedMsg = messageRepository.findById(msg.getId()).orElseThrow();
        assertEquals("Texto editado", updatedMsg.getContent());
    }

    @Test
    void deleteMessageSuccess() throws Exception {
        User admin = userRepository.findById(1L).orElseGet(() -> {
            User u = User.builder().name("Admin").email("admin@test.com").build();
            return userRepository.save(u);
        });

        Message msg = Message.builder()
                .content("Mensaje a eliminar")
                .sender(admin)
                .conversation(conversation)
                .build();
        messageRepository.save(msg);

        Long idMsg = msg.getId();

        mockMvc.perform(post("/conversation/" + conversation.getId() + "/message/" + msg.getId() + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversation/" + conversation.getId()));

        assertFalse(messageRepository.existsById(msg.getId()));
    }

    @Test
    void editMessageEmptyContent() throws Exception {
        User sender = userRepository.findAll().getFirst();

        Message msg = Message.builder()
                .content("Texto inicial")
                .sender(sender)
                .conversation(conversation)
                .build();
        messageRepository.save(msg);

        mockMvc.perform(post("/conversation/" + conversation.getId() + "/message/" + msg.getId() + "/edit")
                        .param("content", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversation/" + conversation.getId()));

        Message unchangedMsg = messageRepository.findById(msg.getId()).orElseThrow();
        assertEquals("Texto inicial", unchangedMsg.getContent());
    }

    @Test
    void editMessageIncorrectUser() throws Exception{
        User sender = userRepository.findAll().get(1);
        Message msg = Message.builder()
                .content("Prueba Usuario Incorrecto")
                .sender(sender)
                .conversation(conversation)
                .build();
        messageRepository.save(msg);

        mockMvc.perform(post("/conversation/" + conversation.getId() + "/message/" + msg.getId() + "/edit")
                        .param("content", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversation/" + conversation.getId()));
    }

    @Test
    void deleteMessageIncorrectUser() throws Exception{
        User sender = userRepository.findAll().get(1);
        Message msg = Message.builder()
                .content("Prueba Usuario Incorrecto")
                .sender(sender)
                .conversation(conversation)
                .build();
        messageRepository.save(msg);

        mockMvc.perform(post("/conversation/" + conversation.getId() + "/message/" + msg.getId() + "/delete")
                        .param("content", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversation/" + conversation.getId()));
    }
}