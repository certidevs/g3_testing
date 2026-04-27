package com.demo.repositories;


import com.demo.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class MessageRepositoryTest {

    @Autowired MessageRepository messageRepositoy;

    @BeforeEach
    void setUp(){
        if(messageRepositoy.findAll().isEmpty()){
            messageRepositoy.deleteAll();
            messageRepositoy.save(new Message());
            messageRepositoy.save(new Message());
            messageRepositoy.save(new Message());
            messageRepositoy.save(new Message());
            messageRepositoy.save(new Message());
        }
    }

    @Test
    void countMessages(){
        assertEquals(5, messageRepositoy.count());
    }

    @Test
    void ExistsById(){
        Message message = new Message();
        messageRepositoy.save(message);

        Long messageId = message.getId();

        assertEquals(messageId, (messageRepositoy.findById(messageId).get()).getId());
    }

    @Test
    void findAllMessages(){
        List<Message> messages = messageRepositoy.findAll();

        assertEquals(5, messages.size());
    }


    @Test
    void findMessageByIdFalse() {
        Long randomId = 997L;
        Optional<Message> message = messageRepositoy.findById(randomId);

        assertFalse(message.isPresent());
    }

    @Test
    void UpdateDoesNotChangeId(){
        Message message = messageRepositoy.findAll().getFirst();

        message.setContent("Updated Content");

        Message updated = messageRepositoy.save(message);

        assertEquals(message.getId(), updated.getId());
    }

    @Test
    void deleteAllMessages(){
        messageRepositoy.deleteAll();

        assertEquals(0, messageRepositoy.findAll().size());
    }

    @Test
    void deleteMessageById(){
        List<Message> messages = messageRepositoy.findAll();
        Message message = messages.get(2);

        messageRepositoy.deleteById(message.getId());

        assertNotEquals(message.getId(), messageRepositoy.findById(message.getId()));
    }

    @Test
    void saveMessage(){
        String content = "Mohamed Afallah";
        Message message = new Message();

        message.setContent(content);

        Message message1 = messageRepositoy.save(message);
        assertEquals(message.getContent(), message1.getContent());
    }

}
