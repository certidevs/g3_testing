package com.demo.controllers;

import com.demo.model.Booking;
import com.demo.model.Conversation;
import com.demo.model.Message;
import com.demo.model.User;
import com.demo.repositories.BookingRepository;
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

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @GetMapping("/conversation/{id}")
    public String getConversationById(@PathVariable Long id, Model model, @AuthenticationPrincipal User user) {
        Conversation conversation = conversationRepository.findByBookingId(id);
        User host = conversation.getBooking().getListing().getOwner();
        User guest = conversation.getBooking().getGuest();

        if (user != null && (user.getId().equals(host.getId()) || user.getId().equals(guest.getId()))) {

            List<Message> mensajesNoLeidos = messageRepository.findByConversationId(conversation.getId(), Sort.by(Sort.Direction.ASC, "sentAt"))
                    .stream()
                    .filter(m -> !m.getSender().getId().equals(user.getId()) && !m.getIsRead())
                    .toList();

            if (!mensajesNoLeidos.isEmpty()) {
                mensajesNoLeidos.forEach(m -> m.setIsRead(true));
                messageRepository.saveAll(mensajesNoLeidos);
            }

            model.addAttribute("conversation", conversation);
            List<Message> messages = messageRepository.findByConversationId(conversation.getId(), Sort.by(Sort.Direction.ASC, "sentAt"));
            model.addAttribute("messages", messages);
            return "conversation/conversation_detail";
        } else {
            return "redirect:/bookings";
        }
    }

    @GetMapping("/conversation")
    public String showConversations(@RequestParam(value = "search", required = false) String search, Model model, @AuthenticationPrincipal User user) {

        if (user == null) {
            return "redirect:/login";
        }

        List<Conversation> conversations;
        Long userLogged = user.getId();

        if (search != null && !search.trim().isEmpty()) {
            conversations = conversationRepository.searchConversations(search);
        } else {
            conversations = conversationRepository.findByBookingHostIdOrBookingGuestId(userLogged, userLogged);
        }

        List<Conversation> conversationsOrdenadas = ordenarConversacionesPorUltimoMensaje(conversations);

        List<Booking> comoHuesped = bookingRepository.findByGuestId(userLogged);
        List<Booking> comoAnfitrion = bookingRepository.findByListingOwnerId(userLogged);

        List<Booking> todasLasReservas = new java.util.ArrayList<>();
        todasLasReservas.addAll(comoHuesped);
        todasLasReservas.addAll(comoAnfitrion);

        List<Booking> bookingsSinConversacion = todasLasReservas.stream()
                .distinct()
                .filter(booking -> conversationRepository.findByBookingId(booking.getId()) == null)
                .toList();

        model.addAttribute("conversations", conversationsOrdenadas);
        model.addAttribute("bookings", bookingsSinConversacion);
        return "conversation/conversation_list";
    }

    private List<Conversation> ordenarConversacionesPorUltimoMensaje(List<Conversation> lista) {
        return lista.stream()
                .sorted((c1, c2) -> {
                    var m1 = messageRepository.findByConversationId(c1.getId(), org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "sentAt"));
                    var m2 = messageRepository.findByConversationId(c2.getId(), org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "sentAt"));

                    java.time.LocalDateTime t1 = m1.isEmpty() ? java.time.LocalDateTime.MIN : m1.get(0).getSentAt();
                    java.time.LocalDateTime t2 = m2.isEmpty() ? java.time.LocalDateTime.MIN : m2.get(0).getSentAt();

                    return t2.compareTo(t1);
                })
                .toList();
    }

    @PostMapping("/conversation/{id}/send")
    public String createMessage(@PathVariable Long id, @RequestParam String content, HttpSession session, @AuthenticationPrincipal User user) {
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
    public String editMessage(@PathVariable Long conversationId, @PathVariable Long messageId, @RequestParam String content, @AuthenticationPrincipal User user) {
        Message message = messageRepository.findById(messageId).orElseThrow();

        if (!message.getSender().getId().equals(user.getId())) {
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
    public String deleteMessage(@PathVariable Long conversationId, @PathVariable Long messageId, @AuthenticationPrincipal User user) {
        Message message = messageRepository.findById(messageId).orElseThrow();

        if (!message.getSender().getId().equals(user.getId())) {
            return "redirect:/conversation/" + conversationId;
        }

        messageRepository.delete(message);

        return "redirect:/conversation/" + conversationId;
    }

    @PostMapping("/conversation/new")
    public String createConversation(@RequestParam Long bookingId, @RequestParam String content, @AuthenticationPrincipal User user) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();

        Conversation conversation = Conversation.builder()
                .booking(booking)
                .build();
        Conversation conversationSaved = conversationRepository.save(conversation);

        var managedUser = userRepository.findById(user.getId()).orElseThrow();
        Message message = Message.builder()
                .content(content)
                .conversation(conversationSaved)
                .sender(user)
                .sentAt(java.time.LocalDateTime.now())
                .isRead(false)
                .build();
        messageRepository.save(message);

        return "redirect:/conversation/" + booking.getId();
    }
}