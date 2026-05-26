package com.demo.controllers;


import com.demo.model.*;
import com.demo.model.enums.BookingStatus;
import com.demo.model.enums.Role;
import com.demo.repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@AllArgsConstructor
@Controller
public class BookingController {

    private final BookingRepository bookingRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDateTime.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text != null && !text.isEmpty()) {
                    LocalDate date = LocalDate.parse(text);
                    setValue(LocalDateTime.of(date, LocalTime.of(15, 0)));  // Fija la hora a 15:00
                }
            }
        });
    }

    @GetMapping("/bookings")
    public String bookings(Model model, @AuthenticationPrincipal User user) {

        String email = user.getEmail();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(()-> new IllegalArgumentException("Usuario no encontrado"));

        List<Booking> bookings = List.of();

        // ADMIN
        if (currentUser.getRole().equals(Role.ROLE_ADMIN)) {

            bookings = bookingRepository.findAll();


        }
        // HOST
        else if (currentUser.getRole().equals(Role.ROLE_HOST)) {


            bookings= bookingRepository.findByGuestId(currentUser.getId());





        }
        // USER NORMAL
        else if  (currentUser.getRole().equals(Role.ROLE_USER)) {

            bookings = bookingRepository.findByGuestId(currentUser.getId());



        }
        // Calcular totalPrice para cada reserva si no está establecido
        for (Booking booking : bookings) {
            if (booking.getTotalPrice() == null || booking.getTotalPrice() == 0) {
                long days = ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
                booking.setTotalPrice(days * booking.getListing().getPricePerNight());
            }
        }



        model.addAttribute("bookings", bookings);
        System.out.println("USUARIO ACTUAL: " + currentUser.getId());

        for (Booking booking : bookings) {
            System.out.println(
                    "BOOKING " + booking.getId()
                            + " GUEST ID: "
                            + booking.getGuest().getId()
            );
        }

        return "booking/booking-list";




    }

    @GetMapping("/bookings/{id}")
    public String bookingDetail(Model model, @PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow();

        // Calcular totalPrice si no está establecido
        if (booking.getTotalPrice() == null || booking.getTotalPrice() == 0) {
            long days = ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
            booking.setTotalPrice(days * booking.getListing().getPricePerNight());
        }

        model.addAttribute("booking", bookingRepository.findById(id).orElse(null));
        model.addAttribute("listing", bookingRepository.findListingByBookingId(id));
        model.addAttribute("owner", bookingRepository.findOwnerByBookingId(id).orElse(null));
        model.addAttribute("review",bookingRepository.findReviewByBookingId(id).orElse(null));

        return "booking/booking-detail";

    }

    @PostMapping("/bookings/{id}/confirm")
    public String confirmBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            redirectAttributes.addFlashAttribute("message", "Reserva confirmada exitosamente.");
        } else {
            redirectAttributes.addFlashAttribute("error", "La reserva no puede ser confirmada porque no está en estado pendiente.");
        }

        return "redirect:/bookings/" + id;
    }


    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes){

        Booking booking= bookingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        if(booking.getStatus()== BookingStatus.PENDING|| booking.getStatus()== BookingStatus.CONFIRMED){
            booking.setStatus(BookingStatus.CANCELED);
            bookingRepository.save(booking);
            redirectAttributes.addFlashAttribute("message", "Reserva cancelada exitosamente.");

        } else{
            redirectAttributes.addFlashAttribute("error", "La reserva no puede ser cancelada porque no está en estado pendiente.");
        }

        return "redirect:/bookings/" + id;
    }



    @PostMapping("/bookings/{id}/delete")
    public String deleteBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // 1. Buscar y eliminar messages de la conversación asociada
            Conversation conversation = conversationRepository.findByBookingId(id);
            if (conversation != null) {
                List<Message> messages = messageRepository.findByConversationId(conversation.getId(), Sort.unsorted());
                messageRepository.deleteAll(messages);  // Eliminar messages
                conversationRepository.delete(conversation);  // Eliminar conversación
            }

            // 2. Buscar y eliminar reviews asociadas al booking
            List<Review> reviews = reviewRepository.findByBookingId(id);
            reviewRepository.deleteAll(reviews);

            // 3. Eliminar el booking
            bookingRepository.deleteById(id);

            redirectAttributes.addFlashAttribute("message", "Reserva y entidades relacionadas eliminadas exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la reserva: " + e.getMessage());
        }

        return "redirect:/bookings";
    }

// ============== NUEVOS MÉTODOS PARA FORMULARIO DE RESERVA ==============

    @GetMapping("/bookings/new/{listingId}")
    public String showBookingForm(@PathVariable Long listingId, Model model) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Alojamiento no encontrado"));

        // Crear un nuevo booking vacío
        Booking booking = new Booking();
        booking.setListing(listing);

        model.addAttribute("booking", booking);
        model.addAttribute("listing", listing);

        return "booking/booking-form";
    }

    @PostMapping("/bookings")
    public String createBooking(@ModelAttribute Booking booking,
                                RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        try {

            Listing listing = listingRepository.findById(booking.getListing().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Alojamiento no encontrado"));

            booking.setListing(listing);

            // 2. Validar que checkIn < checkOut
            if (booking.getCheckIn() == null || booking.getCheckOut() == null) {
                throw new IllegalArgumentException("Las fechas de entrada y salida son requeridas");
            }

            if (booking.getCheckIn().isAfter(booking.getCheckOut()) ||
                    booking.getCheckIn().isEqual(booking.getCheckOut())) {
                throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada");
            }

            // TODO revisar número negativo, salió -22 en la UI
            // 3. Validar mínimas y máximas noches
            long nights = ChronoUnit.DAYS.between(
                    booking.getCheckIn().toLocalDate(),
                    booking.getCheckOut().toLocalDate()
            );

            if (nights < listing.getMinNights()) {
                throw new IllegalArgumentException(
                        String.format("Mínimo %d noches requeridas. Seleccionaste %d",
                                listing.getMinNights(), nights)
                );
            }

            if (nights > listing.getMaxNights()) {
                throw new IllegalArgumentException(
                        String.format("Máximo %d noches permitidas. Seleccionaste %d",
                                listing.getMaxNights(), nights)
                );
            }

            // 4. Validar disponibilidad (sin conflictos con otras reservas CONFIRMED)
            List<Booking> conflicts = bookingRepository.findByListingId(listing.getId());
            for (Booking existing : conflicts) {
                // Solo validar con reservas CONFIRMED o PENDING
                if (existing.getStatus() == BookingStatus.CONFIRMED ||
                        existing.getStatus() == BookingStatus.PENDING) {

                    // Verificar si hay solapamiento de fechas
                    if (!booking.getCheckOut().isBefore(existing.getCheckIn()) &&
                            !booking.getCheckIn().isAfter(existing.getCheckOut())) {
                        throw new IllegalArgumentException(
                                "Esas fechas no están disponibles. El alojamiento ya tiene una reserva en ese período."
                        );
                    }
                }
            }

            // 5. Asignar guest (usuario actual)
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            String email = user.getEmail();

            User guest = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            booking.setGuest(guest);

            // 6. Calcular totalPrice
            booking.setTotalPrice(nights * listing.getPricePerNight());

            // 7. Establecer estado inicial
            booking.setStatus(BookingStatus.PENDING);


            // 8. Guardar la reserva
            bookingRepository.save(booking);

            redirectAttributes.addFlashAttribute("message",
                    "Reserva creada exitosamente. Tu reserva está en estado pendiente.");

            return "redirect:/bookings/" + booking.getId();

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            // Redirigir de vuelta al formulario con el listingId
            Long listingId = booking.getListing() != null ? booking.getListing().getId() : null;
            if (listingId != null) {
                return "redirect:/bookings/new/" + listingId;
            }
            return "redirect:/listings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al crear la reserva: " + e.getMessage());
            return "redirect:/listings";
        }
    }




}
