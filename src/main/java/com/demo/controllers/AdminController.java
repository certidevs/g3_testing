package com.demo.controllers;

import com.demo.repositories.BookingRepository;
import com.demo.repositories.ListingRepository;
import com.demo.repositories.ReviewRepository;
import com.demo.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalListings", listingRepository.count());
        model.addAttribute("totalBookings", bookingRepository.count());
        model.addAttribute("totalReviews", reviewRepository.count());

        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("listings", listingRepository.findAll());
        model.addAttribute("bookings", bookingRepository.findAll());
        model.addAttribute("reviews", reviewRepository.findAll());

        return "admin/dashboard";
    }
}