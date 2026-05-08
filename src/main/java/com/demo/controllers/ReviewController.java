package com.demo.controllers;


import com.demo.model.Review;
import com.demo.repositories.BookingRepository;
import com.demo.repositories.ReviewRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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



}
