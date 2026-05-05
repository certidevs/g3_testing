package com.demo.controllers;


import com.demo.model.Booking;
import com.demo.model.enums.BookingStatus;
import com.demo.repositories.BookingRepository;
import com.demo.repositories.ListingRepository;
import com.demo.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@AllArgsConstructor
@Controller
public class BookingController {

    private final BookingRepository bookingRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    @GetMapping("/bookings")
    public String bookings(Model model) {

        model.addAttribute("bookings", bookingRepository.findAll());
        model.addAttribute("listings", listingRepository.findAll());

        return "booking/booking-list";

    }

    @GetMapping("booking/{id}")
    public String bookingDetail(Model model, @PathVariable Long id) {

        model.addAttribute("booking", bookingRepository.findById(id).orElseThrow());
        model.addAttribute("listing", listingRepository.findById(id).orElseThrow());

        return "booking/booking-detail";

    }

    @PostMapping("/booking/{id}/confirm")
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


}
