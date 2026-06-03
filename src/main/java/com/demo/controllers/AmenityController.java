package com.demo.controllers;

import com.demo.model.Amenity;
import com.demo.model.AmenityLine;
import com.demo.repositories.AmenityRepository;
import com.demo.repositories.ListingRepository;
import com.demo.repositories.AmenityLineRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class AmenityController {

    private final AmenityRepository amenityRepository;
    private final ListingRepository listingRepository;
    private final AmenityLineRepository amenityLineRepository;

    @GetMapping("/amenity")
    public String amenity(Model model) {
        model.addAttribute("amenities", amenityRepository.findAll());
        return "amenity/amenity-list";
    }

    @GetMapping("/amenity/{id}")
    public String amenityDetail(Model model, @PathVariable Long id) {
        model.addAttribute("amenity", amenityRepository.findById(id).orElseThrow());
        return "amenity/amenity-detail";
    }

    @GetMapping("/amenity/new")
    public String create(Model model) {
        model.addAttribute("amenityLine", new AmenityLine());
        model.addAttribute("amenities", amenityRepository.findAll());
        model.addAttribute("listings", listingRepository.findAll());
        return "amenity/amenity-form";
    }

    @PostMapping("/amenity")
    public String save(@ModelAttribute AmenityLine amenityLine) {
        amenityLineRepository.save(amenityLine);
        return "redirect:/listings/" + amenityLine.getListing().getId();
    }

    @GetMapping("/amenity/create")
    public String showCreateForm(Model model) {
        model.addAttribute("amenity", new Amenity());
        return "amenity/amenity-form";
    }

    @PostMapping("/amenity/create")
    public String saveNewAmenity(@ModelAttribute Amenity amenity) {
        amenityRepository.save(amenity);
        return "redirect:/amenity";
    }
}