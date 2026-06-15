package com.demo.controllers;

import com.demo.model.Amenity;
import com.demo.model.AmenityLine;
import com.demo.model.Listing;
import com.demo.model.User;
import com.demo.model.enums.City;
import com.demo.model.enums.ListingType;
import com.demo.model.enums.Role;
import com.demo.model.enums.BookingStatus;
import com.demo.repositories.*;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@AllArgsConstructor
@Controller
@RequestMapping("/listings") //ruta base para todos los métodos
public class ListingController {

    private final ListingRepository listingRepository;
    private final AmenityRepository amenityRepository;
    private final UserRepository userRepository;
    private final ListingService listingService;
    private final AmenityLineRepository amenityLineRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

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
            @RequestParam(required = false) String sort,
            Model model
    ) {

        if (guests != null && guests == 0) guests = null;
        if (nights != null && nights == 0) nights = null;

        // Validación: minPrice no puede ser mayor que maxPrice
        boolean priceRangeError = minPrice != null && maxPrice != null && minPrice > maxPrice;

        List<Listing> listings;
        if (priceRangeError) {
            listings = List.of();
        } else {
        listings = listingService.search(
                type, minPrice, maxPrice, guests, nights, city, startDate, endDate,sort
        );
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Evita NPE cuando no hay autenticación
        boolean isAnonymous = (auth == null) || auth instanceof AnonymousAuthenticationToken;

        User currentUser = (!isAnonymous && auth.getPrincipal() instanceof User)
                ? (User) auth.getPrincipal()
                : null;
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

        Map<Long, Double> ratings = reviewRepository.findAverageRatingsByListing()
                .stream()
                .collect(Collectors.toMap(
                        ListingRatingProjection::getListingId,
                        ListingRatingProjection::getAvgRating
                ));
        model.addAttribute("ratings", ratings);
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
        model.addAttribute("priceRangeError", priceRangeError);
        model.addAttribute("selectedSort", sort);

        return "listing/listing-list";
    }


//    @GetMapping("/{id}")
//    public String detail(@PathVariable Long id, Model model, @AuthenticationPrincipal User currentUser) {
//        //List<Amenity> amenities = amenityRepository.findByListing_Id(id);
//        Listing listing = listingRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
//
//        List<Amenity> amenities = amenityLineRepository.findByListingId(id)
//                .stream()
//                .map(AmenityLine::getAmenity)
//                .toList();
//
//        boolean isOwner = currentUser != null
//                && listing.getOwner() != null
//                && listing.getOwner().getId().equals(currentUser.getId());
//
//        model.addAttribute("listing", listing);
//        model.addAttribute("amenities", amenities);
//        model.addAttribute("isOwner", isOwner);
//        return "listing/listing-detail";
//    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, @AuthenticationPrincipal User currentUser) {

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean isInactive = Boolean.FALSE.equals(listing.getIsActive());
        boolean isAdmin = currentUser != null && currentUser.getRole() == Role.ROLE_ADMIN;
        boolean isOwner = currentUser != null
                && listing.getOwner() != null
                && listing.getOwner().getId().equals(currentUser.getId());

        // Regla de negocio robusta:
        // Un listing inactivo solo lo ven: ADMIN o el dueño
        if (isInactive && !isAdmin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        List<Amenity> amenities = amenityLineRepository.findByListingId(id)
                .stream()
                .map(AmenityLine::getAmenity)
                .toList();

        model.addAttribute("listing", listing);
        model.addAttribute("amenities", amenities);
        model.addAttribute("isOwner", isOwner);

        return "listing/listing-detail";
    }


    @GetMapping("/new")
    public String createListing(Model model) {
        model.addAttribute("listing", new Listing());
        model.addAttribute("types", ListingType.values());
        model.addAttribute("cities", City.values());
        model.addAttribute("amenities", amenityRepository.findAll());       // ← nuevo
        model.addAttribute("selectedAmenityIds", List.of());
        return "listing/listing-form";
    }

    @GetMapping("/edit/{id}")
    public String editListing(@PathVariable Long id, Model model) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<Long> selectedAmenityIds = amenityLineRepository.findByListingId(id)
                .stream()
                .map(al -> al.getAmenity().getId())
                .toList();

        model.addAttribute("listing", listing);
        model.addAttribute("types", ListingType.values());
        model.addAttribute("cities", City.values());
        model.addAttribute("amenities", amenityRepository.findAll());
        model.addAttribute("selectedAmenityIds", selectedAmenityIds);
        return "listing/listing-form";
    }
    @PostMapping
    public String saveListing(@ModelAttribute Listing listing,  @RequestParam(required = false) List<Long> amenityIds, @AuthenticationPrincipal User user) {


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
        Listing saved = listingRepository.save(listing);

        // Borrar amenityLines existentes (cubre tanto crear como editar)
        amenityLineRepository.deleteAll(
                amenityLineRepository.findByListingId(saved.getId())        // ← nuevo
        );

        // Crear las nuevas seleccionadas
        if (amenityIds != null && !amenityIds.isEmpty()) {                  // ← nuevo
            List<AmenityLine> lines = amenityIds.stream()
                    .map(amenityId -> AmenityLine.builder()
                            .amenity(amenityRepository.findById(amenityId).orElseThrow())
                            .listing(saved)
                            .quantity(1)
                            .build())
                    .toList();
            amenityLineRepository.saveAll(lines);
        }
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
    @PostMapping("/{id}/delete")
    public String deleteListing(@PathVariable Long id,
                                @AuthenticationPrincipal User currentUser,
                                RedirectAttributes redirectAttributes) {

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Ownership: ADMIN o HOST dueño
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        if (!isAdmin && !listing.getOwner().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // Validar reservas activas
        boolean hasActiveBookings =
                !bookingRepository.findByListingIdAndStatus(id, BookingStatus.PENDING).isEmpty()
                        || !bookingRepository.findByListingIdAndStatus(id, BookingStatus.CONFIRMED).isEmpty();

        if (hasActiveBookings) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "No se puede eliminar el alojamiento porque tiene reservas activas.");
            return "redirect:/listings/" + id;
        }

        // Soft delete
        listing.setDeleted(true);
        listingRepository.save(listing);

        return "redirect:/listings";
    }





}
