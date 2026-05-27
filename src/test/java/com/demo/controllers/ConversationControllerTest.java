package com.demo.controllers;

import com.demo.model.*;
import com.demo.model.enums.BookingStatus;
import com.demo.model.enums.Role;
import com.demo.repositories.*;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private UserRepository userRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ConversationRepository conversationRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private ListingRepository listingRepository;

    private User owner;
    private User guest;
    private Booking booking;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        bookingRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();

        owner = User.builder().username("pepe").name("Host Test").email("host@test.com").role(Role.ROLE_ADMIN).build();
        guest = User.builder().name("Guest Test").email("guest@test.com").build();
        userRepository.saveAll(List.of(owner, guest));

        guest = userRepository.findById(guest.getId()).orElseThrow();
        owner = userRepository.findById(owner.getId()).orElseThrow();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(guest, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

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
                .isRead(false)
                .build();
        messageRepository.save(msg);
    }

    @Test
    void conversationFull() throws Exception {
        mockMvc.perform(get("/conversation/" + booking.getId()).with(user(owner)))
                .andExpect(status().isOk())
                .andExpect(view().name("conversation/conversation_detail"))
                .andExpect(model().attributeExists("conversation"))
                .andExpect(model().attribute("conversation", hasProperty("id", is(conversation.getId()))));
    }

    @Test
    void conversationNoExists() throws Exception {
        User ajeno = User.builder().name("Ajeno").email("ajeno@test.com").build();
        userRepository.save(ajeno);

        UsernamePasswordAuthenticationToken authAjeno = new UsernamePasswordAuthenticationToken(ajeno, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authAjeno);

        mockMvc.perform(get("/conversation/" + booking.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"));
    }

    @Test
    void conversationList() throws Exception {
        mockMvc.perform(get("/conversation"))
                .andExpect(status().isOk())
                .andExpect(view().name("conversation/conversation_list"))
                .andExpect(model().attributeExists("conversations"))
                .andExpect(model().attribute("conversations", hasSize(1)));
    }

    @Test
    void conversationListWithSearch() throws Exception {
        mockMvc.perform(get("/conversation").param("search", "Apartamento"))
                .andExpect(status().isOk())
                .andExpect(view().name("conversation/conversation_list"))
                .andExpect(model().attributeExists("conversations"));
    }

    @Test
    void createMessageSuccess() throws Exception {
        mockMvc.perform(post("/conversation/" + booking.getId() + "/send")
                        .param("content", "Mensaje nuevo de prueba"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversation/" + booking.getId()));

        List<Message> messages = messageRepository.findAll();
        boolean exists = messages.stream()
                .anyMatch(m -> m.getContent().equals("Mensaje nuevo de prueba"));

        assertTrue(exists);
    }

    @Test
    void createMessageEmptyContent() throws Exception {
        mockMvc.perform(post("/conversation/" + booking.getId() + "/send")
                        .param("content", "   "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversation/" + booking.getId()));
    }

    @Test
    void editMessageSuccess() throws Exception {
        Message msg = Message.builder()
                .content("Texto original")
                .sender(guest)
                .conversation(conversation)
                .build();
        messageRepository.save(msg);

        mockMvc.perform(post("/conversation/" + booking.getId() + "/message/" + msg.getId() + "/edit")
                        .param("content", "Texto editado"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversation/" + booking.getId()));

        Message updatedMsg = messageRepository.findById(msg.getId()).orElseThrow();
        assertEquals("Texto editado", updatedMsg.getContent());
    }

    @Test
    void editMessageEmptyContent() throws Exception {
        Message msg = Message.builder()
                .content("Texto inicial")
                .sender(guest)
                .conversation(conversation)
                .build();
        messageRepository.save(msg);

        mockMvc.perform(post("/conversation/" + booking.getId() + "/message/" + msg.getId() + "/edit")
                        .param("content", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversation/" + booking.getId()));

        Message unchangedMsg = messageRepository.findById(msg.getId()).orElseThrow();
        assertEquals("Texto inicial", unchangedMsg.getContent());
    }

    @Test
    void editMessageIncorrectUser() throws Exception {
        Message msg = Message.builder()
                .content("Prueba Usuario Incorrecto")
                .sender(owner)
                .conversation(conversation)
                .build();
        messageRepository.save(msg);

        mockMvc.perform(post("/conversation/" + booking.getId() + "/message/" + msg.getId() + "/edit")
                        .param("content", "Intento de hack"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversation/" + booking.getId()));

        Message unchangedMsg = messageRepository.findById(msg.getId()).orElseThrow();
        assertEquals("Prueba Usuario Incorrecto", unchangedMsg.getContent());
    }

    @Test
    void deleteMessageSuccess() throws Exception {
        Message msg = Message.builder()
                .content("Mensaje a eliminar")
                .sender(guest)
                .conversation(conversation)
                .build();
        messageRepository.save(msg);

        mockMvc.perform(post("/conversation/" + booking.getId() + "/message/" + msg.getId() + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversation/" + booking.getId()));

        assertFalse(messageRepository.existsById(msg.getId()));
    }

    @Test
    void deleteMessageIncorrectUser() throws Exception {
        Message msg = Message.builder()
                .content("Prueba Usuario Incorrecto")
                .sender(owner)
                .conversation(conversation)
                .build();
        messageRepository.save(msg);

        mockMvc.perform(post("/conversation/" + booking.getId() + "/message/" + msg.getId() + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversation/" + booking.getId()));

        assertTrue(messageRepository.existsById(msg.getId()));
    }

    @Test
    void createConversationFromNewSuccess() throws Exception {
        conversationRepository.deleteAll();

        mockMvc.perform(post("/conversation/new")
                        .param("bookingId", booking.getId().toString())
                        .param("content", "Hola, me interesa el apartamento"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversation/" + booking.getId()));

        assertNotNull(conversationRepository.findByBookingId(booking.getId()));
    }

    @Test
    void orderConversationsWithoutMessages() throws Exception {
        Booking booking2 = Booking.builder()
                .checkIn(LocalDateTime.now().plusDays(2))
                .checkOut(LocalDateTime.now().plusDays(6))
                .totalPrice(150.0)
                .status(BookingStatus.CONFIRMED)
                .guest(guest)
                .listing(listingRepository.findAll().getFirst())
                .build();
        bookingRepository.save(booking2);

        Conversation convVacia = Conversation.builder().booking(booking2).build();
        conversationRepository.save(convVacia);

        mockMvc.perform(get("/conversation"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("conversations", hasSize(2)));
    }
}