package com.demo.controllers;

import com.demo.repositories.ListingRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/listings")
class ListingController {

    private final ListingRepository listingRepository;

    public ListingController(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

}
