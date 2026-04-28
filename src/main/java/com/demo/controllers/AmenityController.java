package com.demo.controllers;

import com.demo.repositories.AmenityRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//inyectar repositorios
@Controller
@AllArgsConstructor

public class AmenityController {
    private final AmenityRepository amenityRepository;


    //mapping
    @GetMapping("amenity/{id}")
    public String amenityDetail(Model model, @PathVariable Long id) {

        model.addAttribute("amenity", amenityRepository.findById(id).orElseThrow());


        return "amenity/amenity-detail";

    }
}
