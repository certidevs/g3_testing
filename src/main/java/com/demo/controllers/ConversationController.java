package com.demo.controllers;

import com.demo.model.Conversation;
import com.demo.model.Message;
import com.demo.model.User;
import com.demo.repositories.ConversationRepository;
import com.demo.repositories.MessageRepository;
import com.demo.repositories.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@AllArgsConstructor
public class ConversationController {

    private ConversationRepository conversationRepository;

    private MessageRepository messageRepository;

    private UserRepository userRepository;

    @GetMapping("/conversation/{id}")
    String getConversationById(@PathVariable("id") Long id, Model model){
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


    @GetMapping("/conversation")
    String getConversaionts(Model model){
        List<Conversation> conversations = conversationRepository.findAll();

        model.addAttribute("conversations", conversations);
        return "conversation/conversation_list";
    }

    @PostMapping("/conversation/{id}/send")
    String createMessage(@PathVariable Long id, @RequestParam String content, HttpSession session) {
        if (content == null || content.isBlank()) {
            return "redirect:/conversation/" + id;
        }

        Conversation conversation = conversationRepository.findByBookingId(id);

        if (conversation != null) {
            User sender = (User) session.getAttribute("loggedInUser");

            if (sender == null) {
                sender = userRepository.findById(1L).orElseThrow();
            }

            Message message = Message.builder()
                    .content(content)
                    .conversation(conversation)
                    .sender(sender)
                    .sentAt(java.time.LocalDateTime.now())
                    .isRead(false)
                    .build();

            messageRepository.save(message);
        }

        return "redirect:/conversation/" + id;
    }

    @PostMapping("/conversation/{conversationId}/message/{messageId}/edit")
    String editMessage(@PathVariable Long conversationId, @PathVariable Long messageId, @RequestParam String content) {
        Message message = messageRepository.findById(messageId).orElseThrow();

        if (!message.getSender().getId().equals(1L)) {
            return "redirect:/conversation/" + conversationId;
        }

        if (content == null || content.isBlank()) {
            return "redirect:/conversation/" + conversationId;
        }

        message.setContent(content);
        messageRepository.save(message);

        return "redirect:/conversation/" + conversationId;
    }

    @PostMapping("/conversation/{conversationId}/message/{messageId}/delete")
    String deleteMessage(@PathVariable Long conversationId, @PathVariable Long messageId) {
        Message message = messageRepository.findById(messageId).orElseThrow();

        if (!message.getSender().getId().equals(1L)) {
            return "redirect:/conversation/" + conversationId;
        }

        messageRepository.delete(message);

        return "redirect:/conversation/" + conversationId;
    }


}
