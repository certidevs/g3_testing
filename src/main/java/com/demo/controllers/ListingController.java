package com.demo.controllers;

import com.demo.model.Listing;
import com.demo.repositories.ListingRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/listings") //ruta base para todos los métodos
class ListingController {

    private final ListingRepository listingRepository;


    @GetMapping
    public String list(Model model){
        model.addAttribute("listings", listingRepository.findAll());
        return "listing/listing-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("listing", listing);
        return "listing/listing-detail";
    }



}
