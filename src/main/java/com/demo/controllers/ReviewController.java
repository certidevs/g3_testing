package com.demo.controllers;


import com.demo.model.Booking;
import com.demo.model.Review;
import com.demo.model.enums.City;
import com.demo.repositories.BookingRepository;
import com.demo.repositories.ListingRepository;
import com.demo.repositories.ReviewRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Controller
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ListingRepository listingRepository;

    // ── Listado principal con filtros opcionales ─────────────────────────────
    // Parámetros opcionales en la URL:
    //   ?city=MADRID          → filtra por ciudad (nombre del enum)
    //   ?rating=5             → filtra por rating exacto (1-5)
    //   ?orden=reciente       → más reciente primero (por defecto)
    //   ?orden=antiguo        → más antiguo primero
    @GetMapping("reviews")
    public String reviews(
            @RequestParam(required = false) City city,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false, defaultValue = "reciente") String orden,
            Model model) {

        boolean tieneCity   = city != null;
        boolean tieneRating = rating != null;
        boolean ordenAsc    = "antiguo".equals(orden);

        List<Review> reviews;

        if (tieneCity && tieneRating) {
            reviews = ordenAsc
                    ? reviewRepository.findByCityAndRatingOrderByDateAsc(city, rating)
                    : reviewRepository.findByCityAndRatingOrderByDateDesc(city, rating);

        } else if (tieneCity) {
            reviews = ordenAsc
                    ? reviewRepository.findByCityOrderByDateAsc(city)
                    : reviewRepository.findByCityOrderByDateDesc(city);

        } else if (tieneRating) {
            reviews = ordenAsc
                    ? reviewRepository.findByRatingOrderByDateAsc(rating)
                    : reviewRepository.findByRatingOrderByDateDesc(rating);

        } else {
            reviews = ordenAsc
                    ? reviewRepository.findAllByOrderByCreationDateAsc()
                    : reviewRepository.findAllByOrderByCreationDateDesc();
        }

        model.addAttribute("reviews",          reviews);
        model.addAttribute("ciudades",          reviewRepository.findDistinctCities()); // List<City>
        model.addAttribute("todasLasCiudades",  City.values());                         // para el selector completo si se prefiere
        model.addAttribute("bookings",          bookingRepository.findAll());
        model.addAttribute("top10housesFilter", listingRepository.findTop10ByIsActiveTrueOrderByRegisteredAtDesc());
        model.addAttribute("filtroCity",        city);
        model.addAttribute("filtroRating",      rating);
        model.addAttribute("filtroOrden",       orden);

        return "review/review-list";
    }

    @GetMapping("reviews/{id}")
    public String reviewDetail(@PathVariable long id, Model model) {
        Optional<Review> reviewOpt = reviewRepository.findById(id);
        if (reviewOpt.isEmpty()) {
            // Si no existe la reseña, redirige al listado con un mensaje de error
            return "redirect:/reviews";
        }
        Review review = reviewOpt.get();
        model.addAttribute("review", review);
        model.addAttribute("booking", review.getBooking());  // Obtiene la booking desde la review
        return "review/review-detail";
    }
    @GetMapping("/listing/{listingId}/reviews")
    public String allListingReviews(@PathVariable Long listingId, Model model){
        List<Review> reviews = reviewRepository.findByBooking_ListingId(listingId);
        if(reviews.isEmpty()){
            model.addAttribute("message", "No hay reseñas para este listing");
            return "redirect:/listings/"+listingId;
        } else {
            model.addAttribute("reviews", reviews);
            return "review/review-list";
        }


    }


    @PostMapping("reviews/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reviewRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Borrado exitosamente");
        return "redirect:/reviews";
    }

    @GetMapping("/reviews/new/{bookingId}")
    public String navigateToForm(@PathVariable Long bookingId, Model model){
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking no encontrado"));
        Review review = new Review();
        review.setBooking(booking); // Asocia la review con la booking
        model.addAttribute("review", review);
        return "review/review-form";
    }

    @PostMapping("/reviews")
    public String createReview(@ModelAttribute Review review,RedirectAttributes redirectAttributes){
        try{
            if (review.getBooking() == null || review.getBooking().getId() == null) {
                throw new IllegalArgumentException("Reserva no especificada");
            }
            Booking booking = bookingRepository.findById(review.getBooking().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

            review.setBooking(booking);
            // Validación: rating obligatorio
            if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
                throw new IllegalArgumentException("La puntuación debe estar entre 1 y 5");
            }
            // Validación: comentario obligatorio
            if (review.getComment() == null || review.getComment().trim().isEmpty()) {
                throw new IllegalArgumentException("El comentario no puede estar vacío");
            }
            // verified por defecto
            if (review.getVerified() == null) {
                review.setVerified(true);
            }

            // Fecha de creación
            review.setCreationDate(LocalDate.now());

            reviewRepository.save(review);
            redirectAttributes.addFlashAttribute("message", "Reseña creada exitosamente");
            return "redirect:/reviews/"+review.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al crear la reseña: " + e.getMessage());
            // Volver al formulario de la reserva correspondiente para mostrar el error
            Long bookingId = review.getBooking() != null ? review.getBooking().getId() : null;
            if (bookingId != null) {
                return "redirect:/reviews/new/" + bookingId;
            }
            return "redirect:/bookings";
        }

    }





}