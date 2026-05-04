package com.demo.repositories;


import com.demo.model.Booking;
import com.demo.model.Conversation;
import com.demo.model.Message;
import com.demo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class MessageRepositoryTest {

    @Autowired MessageRepository messageRepository;
    @Autowired ConversationRepository conversationRepository;
    @Autowired BookingRepository bookingRepository;
    @Autowired UserRepository userRepositoy;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        bookingRepository.deleteAll();
        userRepositoy.deleteAll();

        Booking booking = Booking.builder()
                .build();
        bookingRepository.save(booking);

        User host = User.builder()
                .name("Mohamed")
                .email("moha").build();

        User guest = User.builder()
                .name("Juan")
                .email("juan").build();
        userRepositoy.saveAll(List.of(host, guest));

        Conversation conversation = Conversation.builder()
                .booking(booking)
                .build();
        conversationRepository.save(conversation);

        List<Message> messages = List.of(
                Message.builder()
                        .content("Hola, ¿a qué hora llegas?")
                        .sender(host)
                        .conversation(conversation)
                        .build(),
                Message.builder()
                        .content("Llego a las 5pm, gracias.")
                        .sender(guest)
                        .conversation(conversation)
                        .build(),
                Message.builder()
                        .content("Perfecto, te espero.")
                        .sender(host)
                        .conversation(conversation)
                        .build()
        );

        messageRepository.saveAll(messages);
    }

    @Test
    void countMessages(){
        assertEquals(3, messageRepository.count());
    }

    @Test
    void ExistsById(){
        Message message = new Message();
        messageRepository.save(message);

        Long messageId = message.getId();

        assertEquals(messageId, (messageRepository.findById(messageId).get()).getId());
    }

    @Test
    void findAllMessages(){
        List<Message> messages = messageRepository.findAll();

        assertEquals(3, messages.size());
    }


    @Test
    void findMessageByIdFalse() {
        Long randomId = 997L;
        Optional<Message> message = messageRepository.findById(randomId);

        assertFalse(message.isPresent());
    }

    @Test
    void UpdateDoesNotChangeId(){
        Message message = messageRepository.findAll().getFirst();

        message.setContent("Updated Content");

        Message updated = messageRepository.save(message);

        assertEquals(message.getId(), updated.getId());
    }

    @Test
    void deleteAllMessages(){
        messageRepository.deleteAll();

        assertEquals(0, messageRepository.findAll().size());
    }

    @Test
    void deleteMessageById(){
        List<Message> messages = messageRepository.findAll();
        Message message = messages.get(2);

        messageRepository.deleteById(message.getId());

        assertNotEquals(message.getId(), messageRepository.findById(message.getId()));
    }

    @Test
    void saveMessage(){
        String content = "Mohamed Afallah";
        Message message = new Message();

        message.setContent(content);

        Message message1 = messageRepository.save(message);
        assertEquals(message.getContent(), message1.getContent());
    }

    @Test
    void findMessageByConversationId(){
        Booking booking = bookingRepository.findAll().getFirst();
        assertNotNull(booking);

        Conversation conversation = conversationRepository.findByBookingId(booking.getId());
        assertNotNull(conversation);

        List<Message> messageOrdered = messageRepository.findByConversationId(conversation.getId(), Sort.by("sentAt").descending());
        assertEquals(3, messageOrdered.size());
        assertEquals("Hola, ¿a qué hora llegas?", messageOrdered.get(0).getContent());
    }

}
