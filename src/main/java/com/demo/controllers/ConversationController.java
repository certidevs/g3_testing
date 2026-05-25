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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    String getConversationById(@PathVariable Long id, Model model, @AuthenticationPrincipal User user){
        Conversation conversation = conversationRepository.findByBookingId(id);
        User host = conversation.getBooking().getListing().getOwner();
        User guest = conversation.getBooking().getGuest();

        if (user != null && (user.getId().equals(host.getId()) || user.getId().equals(guest.getId()))) {
            model.addAttribute("conversation", conversation);
            List<Message> messages = messageRepository.findByConversationId(conversation.getId(), Sort.by(Sort.Direction.ASC, "sentAt"));
            model.addAttribute("messages", messages);
            return "conversation/conversation_detail";

        }else{
            return "redirect:/bookings";
        }
    }


    @GetMapping("/conversation")
    public String showConversations(@RequestParam(value = "search", required = false) String search, Model model,@AuthenticationPrincipal User user) {
        List<Conversation> conversations;
        Long userLogged = user.getId();

        if (search != null && !search.trim().isEmpty()) {
            conversations = conversationRepository.searchConversations(search);
        } else {
            conversations = conversationRepository.findByBookingHostId(userLogged);
            conversations.addAll(conversationRepository.findByBookingGuestId(userLogged));
        }

        model.addAttribute("conversations", conversations);
        return "conversation/conversation_list";
    }

    @PostMapping("/conversation/{id}/send")
    String createMessage(@PathVariable Long id, @RequestParam String content, HttpSession session, @AuthenticationPrincipal User user) {
        if (content == null || content.isBlank()) {
            return "redirect:/conversation/" + id;
        }

        Conversation conversation = conversationRepository.findByBookingId(id);

        if (conversation != null) {
            Message message = Message.builder()
                    .content(content)
                    .conversation(conversation)
                    .sender(user)
                    .sentAt(java.time.LocalDateTime.now())
                    .isRead(false)
                    .build();

            messageRepository.save(message);
        }

        return "redirect:/conversation/" + id;
    }

    @PostMapping("/conversation/{conversationId}/message/{messageId}/edit")
    String editMessage(@PathVariable Long conversationId, @PathVariable Long messageId, @RequestParam String content, @AuthenticationPrincipal User user) {
        Message message = messageRepository.findById(messageId).orElseThrow();

        if (!message.getSender().equals(user)) {
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
    String deleteMessage(@PathVariable Long conversationId, @PathVariable Long messageId,@AuthenticationPrincipal User user) {
        Message message = messageRepository.findById(messageId).orElseThrow();

        if (!message.getSender().equals(user)) {
            return "redirect:/conversation/" + conversationId;
        }

        messageRepository.delete(message);

        return "redirect:/conversation/" + conversationId;
    }


}
