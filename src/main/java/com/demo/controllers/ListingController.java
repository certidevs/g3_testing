package com.demo.controllers;

import com.demo.model.Amenity;
import com.demo.model.Listing;
import com.demo.model.User;
import com.demo.model.enums.City;
import com.demo.model.enums.ListingType;
import com.demo.model.enums.Role;
import com.demo.repositories.AmenityRepository;
import com.demo.repositories.ListingRepository;
import com.demo.repositories.UserRepository;
import com.demo.services.ListingService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/listings") //ruta base para todos los métodos
public class ListingController {

    private final ListingRepository listingRepository;
    private final AmenityRepository amenityRepository;
    private final UserRepository userRepository;
    private final ListingService listingService;

    @GetMapping
    public String list(
            @RequestParam(required = false) ListingType type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) Integer nights,
            @RequestParam(required = false) City city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) {

        if (guests != null && guests == 0) guests = null;
        if (nights != null && nights == 0) nights = null;

        //List<Listing> listings = listingRepository.search(type, minPrice, maxPrice, guests, nights);
        List<Listing> listings = listingService.search(
                type, minPrice, maxPrice, guests, nights, city, startDate, endDate
        );

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        String email = auth.getName(); // email del usuario autenticado

// GUEST → solo listings activos
        if (role.equals("ROLE_USER")) {
            listings = listings.stream()
                    .filter(l -> Boolean.TRUE.equals(l.getIsActive()))
                    .toList();
        }
        if (role.equals("ROLE_ANONYMOUS")) {
            listings = listings.stream()
                    .filter(l -> Boolean.TRUE.equals(l.getIsActive()))
                    .toList();
        }

// HOST → solo sus listings (activos e inactivos)
        if (role.equals("ROLE_HOST")) {
            listings = listings.stream()
                    .filter(l -> l.getOwner().getEmail().equals(email))
                    .toList();
        }

// ADMIN → ve todos (no filtramos)

        model.addAttribute("listings", listings);
        model.addAttribute("types", ListingType.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedGuests", guests);
        model.addAttribute("selectedNights", nights);
        model.addAttribute("cities", City.values());
        model.addAttribute("selectedCity", city);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "listing/listing-list";
    }


    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        List<Amenity> amenities = amenityRepository.findByListing_Id(id);
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("listing", listing);
        model.addAttribute("amenities", amenities);
        return "listing/listing-detail";
    }

    // TODO bajar a minusculas createListing
    @GetMapping("/new")
    public String CreateListing(Model model) {
        model.addAttribute("listing", new Listing());
        model.addAttribute("types", ListingType.values());
        model.addAttribute("cities", City.values());
        return "listing/listing-form";
    }

    @GetMapping("/edit/{id}")
    public String editListing(@PathVariable Long id, Model model) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("listing", listing);
        model.addAttribute("types", ListingType.values());
        model.addAttribute("cities", City.values());
        return "listing/listing-form";
    }
    @PostMapping
    public String saveListing(@ModelAttribute Listing listing, @AuthenticationPrincipal User user) {

        if (user != null && user.getRole() == Role.ROLE_USER) {
            user.setRole(Role.ROLE_HOST);
            userRepository.save(user);
        }
        listing.setOwner(user);

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
