package com.demo.controllers;


import com.demo.model.Booking;
import com.demo.model.Review;
import com.demo.repositories.BookingRepository;
import com.demo.repositories.ReviewRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@AllArgsConstructor
@Controller
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    @GetMapping("reviews")
    public String reviews(Model model){
        model.addAttribute("reviews", reviewRepository.findAll());
        model.addAttribute("bookings", bookingRepository.findAll());
        return "review/review-list";

    }

    @GetMapping("reviews/{id}")
    public String reviewDetail(@PathVariable long id, Model model) {
        Optional<Review> reviewOpt = reviewRepository.findById(id);
        if (reviewOpt.isEmpty()) {
            // Si no existe la reseña, redirige al listado con un mensaje de error
            return "redirect:/reviews";
        }
        Review review = reviewOpt.get();
        model.addAttribute("review", review);
        model.addAttribute("booking", review.getBooking());  // Obtiene la booking desde la review
        return "review/review-detail";
    }


    @GetMapping("reviews/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reviewRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Borrado exitosamente");
        return "redirect:/reviews";
    }

    @GetMapping("/reviews/new/{bookingId}")
    public String navigateToForm(@PathVariable Long bookingId, Model model){
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking no encontrado"));
        Review review = new Review();
        review.setBooking(booking); // Asocia la review con la booking
        model.addAttribute("review", review);
        return "review/review-form";
    }

    @PostMapping("/reviews")
    public String createReview(@ModelAttribute Review review,RedirectAttributes redirectAttributes){
        try{
            if (review.getBooking() == null || review.getBooking().getId() == null) {
                throw new IllegalArgumentException("Reserva no especificada");
            }
            Booking booking = bookingRepository.findById(review.getBooking().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

            review.setBooking(booking);
            // Validación: rating obligatorio
            if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
                throw new IllegalArgumentException("La puntuación debe estar entre 1 y 5");
            }
            // Validación: comentario obligatorio
            if (review.getComment() == null || review.getComment().trim().isEmpty()) {
                throw new IllegalArgumentException("El comentario no puede estar vacío");
            }
            // verified por defecto
            if (review.getVerified() == null) {
                review.setVerified(true);
            }

            // Fecha de creación
            review.setCreationDate(LocalDate.now());

            reviewRepository.save(review);
            redirectAttributes.addFlashAttribute("message", "Reseña creada exitosamente");
            return "redirect:/reviews/"+review.getId();

        }catch (Exception e){
            redirectAttributes.addFlashAttribute("error",
                    "Error al crear la reseña: " + e.getMessage());
            return "redirect:/bookings";

        }

    }



}
