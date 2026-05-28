package com.demo.controllers;

import com.demo.model.Booking;
import com.demo.model.Listing;
import com.demo.model.Review;
import com.demo.model.User;
import com.demo.model.enums.Role;
import com.demo.repositories.BookingRepository;
import com.demo.repositories.ListingRepository;
import com.demo.repositories.ReviewRepository;
import com.demo.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class UserController {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    @GetMapping("/profile")
    public String showUser(Model model, @AuthenticationPrincipal User user) {
        User freshUser = userRepository.findById(user.getId()).orElse(user);
        List<Booking> bookings = bookingRepository.findByGuestId(freshUser.getId());
        List<Review> reviews = reviewRepository.findAllByUserId(freshUser.getId());
        if(user.getRole() == Role.ROLE_HOST){
            List<Listing> listings = listingRepository.findByOwnerId(freshUser.getId());
            List<Booking> hostBookings = bookingRepository.findByListingOwnerId(freshUser.getId());

            model.addAttribute("hostBookings", hostBookings);
            model.addAttribute("listings", listings);
        }else {
            model.addAttribute("hostBookings", java.util.Collections.emptyList());
            model.addAttribute("listings", java.util.Collections.emptyList());
        }

        model.addAttribute("bookings", bookings);
        model.addAttribute("reviews", reviews);
        model.addAttribute("user", freshUser);

        return "user/user-detail";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal User currentUser,
                                @RequestParam String name,
                                @RequestParam String username,
                                @RequestParam String email,
                                Model model) {

        boolean hasErrors = false;

        Optional<User> existingUserByUsername = userRepository.findByUsername(username);
        if (existingUserByUsername.isPresent() && !existingUserByUsername.get().getId().equals(currentUser.getId())) {
            model.addAttribute("usernameError", "El nombre de usuario '" + username + "' ya está en uso.");
            hasErrors = true;
        }

        Optional<User> existingUserByEmail = userRepository.findByEmail(email);
        if (existingUserByEmail.isPresent() && !existingUserByEmail.get().getId().equals(currentUser.getId())) {
            model.addAttribute("emailError", "El correo electrónico '" + email + "' ya está registrado.");
            hasErrors = true;
        }

        if (hasErrors) {
            List<Booking> bookings = bookingRepository.findByGuestId(currentUser.getId());
            List<Review> reviews = reviewRepository.findAllByUserId(currentUser.getId());

            model.addAttribute("bookings", bookings);
            model.addAttribute("reviews", reviews);

            User temporaryUser = new User();
            temporaryUser.setId(currentUser.getId());
            temporaryUser.setRole(currentUser.getRole());
            temporaryUser.setName(name);
            temporaryUser.setUsername(username);
            temporaryUser.setEmail(email);

            model.addAttribute("user", temporaryUser);

            return "redirect:/profile";
        }

        User dbUser = userRepository.findById(currentUser.getId()).orElse(currentUser);
        dbUser.setName(name);
        dbUser.setUsername(username);
        dbUser.setEmail(email);

        userRepository.save(dbUser);

        currentUser.setName(name);
        currentUser.setUsername(username);
        currentUser.setEmail(email);

        return "redirect:/profile?success";
    }
}