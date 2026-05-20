package com.demo.controllers;

import com.demo.model.Amenity;
import com.demo.model.Listing;
import com.demo.repositories.AmenityRepository;
import com.demo.repositories.ListingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

//inyectar repositorios
@Controller
@AllArgsConstructor
public class AmenityController {
    private final AmenityRepository amenityRepository;
    private final ListingRepository listingRepository;



    @GetMapping("/amenity")
    public String amenity(Model model) {

        model.addAttribute("amenities", amenityRepository.findAll());


        return "amenity/amenity-list";

    }




    //mapping
    @GetMapping("/amenity/{id}")
    public String amenityDetail(Model model, @PathVariable Long id) {

        model.addAttribute("amenity", amenityRepository.findById(id).orElseThrow());


        return "amenity/amenity-detail";

    }

    // /amenities/new
    // model amenity
    // model listings

    @GetMapping("/amenity/new")
    public String create(Model model) {
        model.addAttribute("amenity", new Amenity()); // Crea un nuevo objeto Amenity
        model.addAttribute("listings", listingRepository.findAll()); // Obtiene la lista de listings
        return "amenity/amenity-form"; // Asegúrate de que esta vista existe
    }


    @PostMapping("/amenity") // Añade la barra inicial por buena práctica
    public String save(@ModelAttribute Amenity amenity) {
        amenityRepository.save(amenity);
        // Cambiado de /amenity/amenity-detail/ a /amenity/
        return "redirect:/amenity/" + amenity.getId();
    }




}
