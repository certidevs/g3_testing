package com.demo.controllers;


import com.demo.model.Booking;
import com.demo.model.Conversation;
import com.demo.model.Message;
import com.demo.model.Review;
import com.demo.model.enums.BookingStatus;
import com.demo.repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@AllArgsConstructor
@Controller
public class BookingController {

    private final BookingRepository bookingRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    @GetMapping("/bookings")
    public String bookings(Model model) {

        model.addAttribute("bookings", bookingRepository.findAll());
        model.addAttribute("listings", listingRepository.findAll());

        return "booking/booking-list";

    }

    @GetMapping("bookings/{id}")
    public String bookingDetail(Model model, @PathVariable Long id) {

        model.addAttribute("booking", bookingRepository.findById(id).orElseThrow());
        model.addAttribute("listing", bookingRepository.findListingByBookingId(id));
        model.addAttribute("review",bookingRepository.findReviewByBookingId(id));

        return "booking/booking-detail";

    }

    @PostMapping("/bookings/{id}/confirm")
    public String confirmBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            redirectAttributes.addFlashAttribute("message", "Reserva confirmada exitosamente.");
        } else {
            redirectAttributes.addFlashAttribute("error", "La reserva no puede ser confirmada porque no está en estado pendiente.");
        }

        return "redirect:/booking/" + id;
    }


    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes){

        Booking booking= bookingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        if(booking.getStatus()== BookingStatus.PENDING){
            booking.setStatus(BookingStatus.CANCELED);
            bookingRepository.save(booking);
            redirectAttributes.addFlashAttribute("message", "Reserva cancelada exitosamente.");

        } else{
            redirectAttributes.addFlashAttribute("error", "La reserva no puede ser cancelada porque no está en estado pendiente.");
        }

        return "redirect:/booking/" + id;
    }



    @PostMapping("/bookings/{id}/delete")
    public String deleteBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // 1. Buscar y eliminar messages de la conversación asociada
            Conversation conversation = conversationRepository.findByBookingId(id);
            if (conversation != null) {
                List<Message> messages = messageRepository.findByConversationId(conversation.getId(), Sort.unsorted());
                messageRepository.deleteAll(messages);  // Eliminar messages
                conversationRepository.delete(conversation);  // Eliminar conversación
            }

            // 2. Buscar y eliminar reviews asociadas al booking
            List<Review> reviews = reviewRepository.findByBookingId(id);
            reviewRepository.deleteAll(reviews);

            // 3. Eliminar el booking
            bookingRepository.deleteById(id);

            redirectAttributes.addFlashAttribute("message", "Reserva y entidades relacionadas eliminadas exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la reserva: " + e.getMessage());
        }

        return "redirect:/bookings";
    }



}
