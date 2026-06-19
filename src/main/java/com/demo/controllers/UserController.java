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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@AllArgsConstructor
public class UserController {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public String showUser(Model model, @AuthenticationPrincipal User user) {
        User freshUser = userRepository.findById(user.getId()).orElse(user);

        List<Booking> bookings = bookingRepository.findByGuestId(freshUser.getId());
        List<Review> reviews = reviewRepository.findAllByUserId(freshUser.getId());

        model.addAttribute("bookings", bookings);
        model.addAttribute("reviews", reviews);

        if (freshUser.getRole() == Role.ROLE_HOST) {
            model.addAttribute("listings", listingRepository.findByOwnerId(freshUser.getId()));
            model.addAttribute("hostBookings", bookingRepository.findByListingOwnerId(freshUser.getId()));
        } else {
            model.addAttribute("listings", Collections.emptyList());
            model.addAttribute("hostBookings", Collections.emptyList());
        }

        model.addAttribute("user", freshUser);
        return "user/user-detail";
    }

    @GetMapping("/profile/audit/{id}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> auditUser(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != Role.ROLE_ADMIN) {
            return ResponseEntity.status(403).body("Acceso denegado");
        }

        Optional<User> targetUserOpt = userRepository.findById(id);
        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User targetUser = targetUserOpt.get();
        Map<String, Object> data = new HashMap<>();
        data.put("role", targetUser.getRole().name());

        if (targetUser.getRole() == Role.ROLE_ADMIN) {
            data.put("bookings", Collections.emptyList());
            data.put("reviews", Collections.emptyList());
            data.put("listings", Collections.emptyList());
            data.put("hostBookings", Collections.emptyList());
            return ResponseEntity.ok(data);
        }

        List<Booking> bookings = bookingRepository.findByGuestId(targetUser.getId());
        List<Review> reviews = reviewRepository.findAllByUserId(targetUser.getId());

        List<Map<String, Object>> bookingsJson = bookings.stream().map(b -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("title", b.getListing().getTitle());
            m.put("checkIn", b.getCheckIn().toString());
            m.put("checkOut", b.getCheckOut().toString());
            m.put("totalPrice", b.getTotalPrice());
            m.put("status", b.getStatus().name());
            return m;
        }).toList();

        List<Map<String, Object>> reviewsJson = reviews.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("rating", r.getRating());
            m.put("comment", r.getComment());
            m.put("creationDate", r.getCreationDate().toString());
            m.put("listingId", r.getBooking().getListing().getId());
            m.put("listingTitle", r.getBooking().getListing().getTitle());
            m.put("verified", r.getVerified());
            return m;
        }).toList();

        data.put("bookings", bookingsJson);
        data.put("reviews", reviewsJson);

        if (targetUser.getRole() == Role.ROLE_HOST) {
            List<Listing> listings = listingRepository.findByOwnerId(targetUser.getId());
            List<Booking> hostBookings = bookingRepository.findByListingOwnerId(targetUser.getId());

            List<Map<String, Object>> listingsJson = listings.stream().map(l -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", l.getId());
                m.put("title", l.getTitle());
                m.put("city", l.getCity());
                m.put("pricePerNight", l.getPricePerNight());
                return m;
            }).toList();

            List<Map<String, Object>> hostBookingsJson = hostBookings.stream().map(hb -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", hb.getId());
                m.put("title", hb.getListing().getTitle());
                m.put("ownerName", hb.getListing().getOwner().getName() != null ? hb.getListing().getOwner().getName() : hb.getListing().getOwner().getUsername());
                m.put("checkIn", hb.getCheckIn().toString());
                m.put("checkOut", hb.getCheckOut().toString());
                m.put("totalPrice", hb.getTotalPrice());
                m.put("status", hb.getStatus().name());
                return m;
            }).toList();

            data.put("listings", listingsJson);
            data.put("hostBookings", hostBookingsJson);
        } else {
            data.put("listings", Collections.emptyList());
            data.put("hostBookings", Collections.emptyList());
        }

        return ResponseEntity.ok(data);
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal User currentUser,
                                @RequestParam String name,
                                @RequestParam String username,
                                @RequestParam String email,
                                @RequestParam(required = false) String newPassword,
                                @RequestParam(required = false) String confirmPassword,
                                Model model) {

        boolean hasErrors = false;

        Optional<User> existingByUsername = userRepository.findByUsername(username);
        if (existingByUsername.isPresent() && !existingByUsername.get().getId().equals(currentUser.getId())) {
            model.addAttribute("usernameError", "El nombre de usuario '" + username + "' ya está en uso.");
            hasErrors = true;
        }

        Optional<User> existingByEmail = userRepository.findByEmail(email);
        if (existingByEmail.isPresent() && !existingByEmail.get().getId().equals(currentUser.getId())) {
            model.addAttribute("emailError", "El correo electrónico '" + email + "' ya está registrado.");
            hasErrors = true;
        }

        if (newPassword != null && !newPassword.isBlank()) {
            // 👇 NUEVA VALIDACIÓN CON EXPRESIÓN REGULAR 👇
            String passwordPattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";

            if (!newPassword.matches(passwordPattern)) {
                model.addAttribute("passwordError", "La contraseña debe tener al menos 8 caracteres, incluir una mayúscula, un número y un carácter especial (@$!%*?&).");
                hasErrors = true;
            } else if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("passwordError", "Las contraseñas no coinciden.");
                hasErrors = true;
            }
        }

        if (hasErrors) {
            List<Booking> bookings = bookingRepository.findByGuestId(currentUser.getId());
            List<Review> reviews = reviewRepository.findAllByUserId(currentUser.getId());
            model.addAttribute("bookings", bookings);
            model.addAttribute("reviews", reviews);

            if (currentUser.getRole() == Role.ROLE_HOST) {
                model.addAttribute("listings", listingRepository.findByOwnerId(currentUser.getId()));
                model.addAttribute("hostBookings", bookingRepository.findByListingOwnerId(currentUser.getId()));
            } else {
                model.addAttribute("listings", Collections.emptyList());
                model.addAttribute("hostBookings", Collections.emptyList());
            }

            User temporaryUser = new User();
            temporaryUser.setId(currentUser.getId());
            temporaryUser.setRole(currentUser.getRole());
            temporaryUser.setName(name);
            temporaryUser.setUsername(username);
            temporaryUser.setEmail(email);
            model.addAttribute("user", temporaryUser);

            return "user/user-detail";
        }

        User dbUser = userRepository.findById(currentUser.getId()).orElse(currentUser);
        dbUser.setName(name);
        dbUser.setUsername(username);
        dbUser.setEmail(email);

        if (newPassword != null && !newPassword.isBlank()) {
            dbUser.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepository.save(dbUser);

        currentUser.setName(name);
        currentUser.setUsername(username);
        currentUser.setEmail(email);

        return "redirect:/profile?success";
    }
}