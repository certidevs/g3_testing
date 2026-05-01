package com.demo.controllers;


import com.demo.repositories.BookingRepository;
import com.demo.repositories.ListingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@AllArgsConstructor
@Controller
public class BookingController {

    private final BookingRepository bookingRepository;
    private final ListingRepository listingRepository;

    @GetMapping("/bookings")
    public String bookings(Model model) {

        model.addAttribute("bookings", bookingRepository.findAll());
        //model.addAttribute("listings", listingRepository.findAll());

        return "booking/booking-list";

    }

}
