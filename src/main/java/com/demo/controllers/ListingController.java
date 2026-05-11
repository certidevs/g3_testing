package com.demo.controllers;

import com.demo.model.Listing;
import com.demo.model.enums.ListingType;
import com.demo.repositories.ListingRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/listings") //ruta base para todos los métodos
class ListingController {

    private final ListingRepository listingRepository;

    @GetMapping
    public String list(
            @RequestParam(required = false) ListingType type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) Integer nights,
            Model model
    ) {
        if (guests != null && guests == 0) guests = null;
        if (nights != null && nights == 0) nights = null;

        List<Listing> listings = listingRepository.search(type, minPrice, maxPrice, guests, nights);

        model.addAttribute("listings", listings);
        model.addAttribute("types", ListingType.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedGuests", guests);
        model.addAttribute("selectedNights", nights);

        return "listing/listing-list";
    }


    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("listing", listing);
        return "listing/listing-detail";
    }

    @GetMapping("/new")
    public String CreateListing(Model model) {
        model.addAttribute("listing", new Listing());
        model.addAttribute("types", ListingType.values());
        return "listing/listing-form";
    }

    @GetMapping("/edit/{id}")
    public String editListing(@PathVariable Long id, Model model) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("listing", listing);
        model.addAttribute("types", ListingType.values());
        return "listing/listing-form";
    }
    @PostMapping
    public String saveListing(@ModelAttribute Listing listing) {

        // TODO: validaciones (precio, noches, etc.)
        listingRepository.save(listing);

        return "redirect:/listings";
    }

    @PostMapping("/toggle/{id}")
    public String toggleListing(@PathVariable Long id) {

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        listing.setIsActive(!listing.getIsActive());
        listingRepository.save(listing);

        return "redirect:/listings/" + id;
    }





}
