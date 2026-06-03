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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

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

        boolean isAnonymous = auth instanceof AnonymousAuthenticationToken;

        User currentUser = isAnonymous ? null : (User) auth.getPrincipal();
        String email = currentUser != null ? currentUser.getEmail() : null;

        boolean isUser = !isAnonymous && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_USER".equals(a.getAuthority()));
        boolean isHost = !isAnonymous && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_HOST".equals(a.getAuthority()));
        boolean isAdmin = !isAnonymous && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

// ANONYMOUS + USER → solo activos
        if (isAnonymous || isUser) {
            listings = listings.stream()
                    .filter(l -> Boolean.TRUE.equals(l.getIsActive()))
                    .toList();
        }

// HOST → activos + sus propios inactivos
        if (isHost) {
            listings = listings.stream()
                    .filter(l -> Boolean.TRUE.equals(l.getIsActive())
                            || Objects.equals(l.getOwner().getEmail(), email))
                    .toList();
        }
// ADMIN → no se filtra nada

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
        model.addAttribute("currentUserEmail", email);
        model.addAttribute("isAdmin", isAdmin);

        return "listing/listing-list";
    }


    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        //List<Amenity> amenities = amenityRepository.findByListing_Id(id);
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("listing", listing);
        //model.addAttribute("amenities", amenities);
        return "listing/listing-detail";
    }

    @GetMapping("/new")
    public String createListing(Model model) {
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

        System.out.println("Usuario en saveListing: " + user.getEmail());
        System.out.println("Rol antes de promover: " + user.getRole());

        // Si el usuario es USER, lo promovemos a HOST
        if (user.getRole() == Role.ROLE_USER) {
            user.setRole(Role.ROLE_HOST);
            userRepository.save(user);

            // Recargar la autenticación usando el propio objeto User (que implementa UserDetails)
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    user,                      // principal actualizado
                    user.getPassword(),        // credenciales
                    user.getAuthorities()      // roles actualizados
            );

            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }

        // Asignar propietario
        listing.setOwner(user);

        // Guardar listing
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
