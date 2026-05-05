package com.demo.controllers;


import com.demo.repositories.BookingRepository;
import com.demo.repositories.ReviewRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    public String reviewDetail(@PathVariable long id, Model model){
        model.addAttribute("review", reviewRepository.findById(id));
        model.addAttribute("booking", bookingRepository.findById(id));
        return "review/review-detail";
    }



}
