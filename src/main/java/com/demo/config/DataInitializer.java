package com.demo.config;

import com.demo.model.*;
import com.demo.model.enums.BookingStatus;
import com.demo.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AmenityRepository amenityRepository;
    private final BookingRepository bookingRepository;
    private final ConversationRepository conversationRepository;
    private final ListingRepository listingRepository;
    private final MessageRepository messageRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final AddonRepository addonRepository;

    @Override
    public void run(String... args) throws Exception {

        Addon breakfast = Addon.builder()
                .id(1L)
                .title("Desayuno Buffet")
                .description("Variedad de frutas, panes y café de especialidad")
                .price(15.0)
                .build();

        Addon transport = Addon.builder()
                .id(2L)
                .title("Transporte Aeropuerto")
                .description("Recogida privada en coche de alta gama")
                .price(50.0)
                .build();

        addonRepository.saveAll(List.of(breakfast, transport));

        User owner = User.builder()
                .name("Alex Pro")
                .email("alex@pro.com")
                .build();

        User guest = User.builder()
                .name("Sonia Lopez")
                .email("sonia@mail.com")
                .build();

        userRepository.saveAll(List.of(owner, guest));

        Listing loft = Listing.builder()
                .title("Loft Industrial")
                .shortDescription("Espacio abierto y moderno")
                .longDescription("Ubicado en la zona artística, con techos altos y mucha luz")
                .pricePerNight(110.0)
                .minNights(1)
                .maxNights(20)
                .maxGuests(3)
                .imageUrl("https://images.com/loft")
                .registeredAt(LocalDateTime.now())
                .owner(owner)
                .isActive(true)
                .build();

        listingRepository.save(loft);

        Amenity wifi = Amenity.builder()
                .name("Fibra Optica")
                .description("600 Mbps")
                .icon("wifi-icon")
                .listing(loft)
                .build();

        Amenity heating = Amenity.builder()
                .name("Calefaccion")
                .description("Radiadores inteligentes")
                .icon("heat-icon")
                .listing(loft)
                .build();

        amenityRepository.saveAll(List.of(wifi, heating));

        Booking booking = Booking.builder()
                .checkIn(LocalDateTime.now().plusDays(1))
                .checkOut(LocalDateTime.now().plusDays(3))
                .totalPrice(220.0)
                .status(BookingStatus.CONFIRMED)
                .host(owner)
                .guest(guest)
                .listing(loft)
                .build();

        bookingRepository.save(booking);

        Conversation conv = Conversation.builder()
                .booking(booking)
                .build();

        conversationRepository.save(conv);

        Message m1 = Message.builder()
                .content("¿Tengo acceso al código de la puerta?")
                .sender(guest)
                .conversation(conv)
                .sentAt(LocalDateTime.now())
                .isRead(true)
                .build();

        Message m2 = Message.builder()
                .content("Sí, se te enviará 2 horas antes de tu llegada.")
                .sender(owner)
                .conversation(conv)
                .sentAt(LocalDateTime.now().plusMinutes(5))
                .isRead(false)
                .build();

        messageRepository.saveAll(List.of(m1, m2));

        Review review = Review.builder()
                .rating(5)
                .comment("Increíble lugar, muy recomendado")
                .verified(true)
                .creationDate(LocalDate.now())
                .booking(booking)
                .build();

        reviewRepository.save(review);
    }
}