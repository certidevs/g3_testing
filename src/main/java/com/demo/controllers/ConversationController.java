package com.demo.controllers;

import com.demo.model.Conversation;
import com.demo.model.Message;
import com.demo.repositories.ConversationRepository;
import com.demo.repositories.MessageRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@AllArgsConstructor
public class ConversationController {

    private ConversationRepository conversationRepository;

    private MessageRepository messageRepository;

    @GetMapping("/conversation/{id}")
    String getConversation(@PathVariable("id") Long id, Model model){
        Conversation conversation = conversationRepository.findByBookingId(id);

        if(conversation != null) {
            model.addAttribute("conversation", conversation);
            List<Message> messages = messageRepository.findByConversationId(conversation.getId(), Sort.by(Sort.Direction.ASC, "sentAt"));
            model.addAttribute("messages", messages);
            return "conversation/conversation_detail";

        }else{
            return "redirect:/bookings";
        }
    }
}
