package com.demo.config;

import com.demo.model.*;
import com.demo.model.enums.BookingStatus;
import com.demo.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("!test")
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
    @Transactional
    public void run(String... args) throws Exception {

        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        reviewRepository.deleteAll();
        amenityRepository.deleteAll();
        bookingRepository.deleteAll();
        listingRepository.deleteAll();
        addonRepository.deleteAll();
        userRepository.deleteAll();

        Addon breakfast = Addon.builder()
                .title("Desayuno Buffet")
                .description("Variedad de frutas, panes y café de especialidad")
                .price(15.0)
                .build();

        Addon transport = Addon.builder()
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
        Listing ap = Listing.builder()
                .title("Apartamento con Vistas")
                .shortDescription("Apartamento moderno con vistas al mar")
                .longDescription("Ubicado en la zona turística, con balcón y piscina")
                .pricePerNight(150.0)
                .minNights(2)
                .maxNights(15)
                .maxGuests(4)
                .imageUrl("https://images.com/apartment")
                .registeredAt(LocalDateTime.now())
                .owner(owner)
                .isActive(true)
                .build();
        List<Listing> listings = List.of(loft, ap);

        listingRepository.saveAll(listings);

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
                //.totalPrice(220.0)
                .status(BookingStatus.CONFIRMED)
                .guest(guest)
                .listing(loft)
                .build();

        Booking booking2 = Booking.builder()
                .checkIn(LocalDateTime.of(2026,4,20,15,30))
                .checkOut(LocalDateTime.of(2026,4,25,15,30))
                //.totalPrice(750.0)
                .status(BookingStatus.CONFIRMED)
                .guest(guest)
                .listing(ap)
                .build();

        List<Booking> bookings = List.of(booking, booking2);
        bookingRepository.saveAll(bookings);

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

        Message m3 = Message.builder()
                .content("Te esperamos con muchas ganas.")
                .sender(owner)
                .conversation(conv)
                .sentAt(LocalDateTime.now().plusMinutes(6))
                .isRead(false)
                .build();

        Message m4 = Message.builder()
                .content("Muchas Gracias !!")
                .sender(guest)
                .conversation(conv)
                .sentAt(LocalDateTime.now().plusMinutes(8))
                .isRead(false)
                .build();

        messageRepository.saveAll(List.of(m1, m2, m3, m4));

        Review review = Review.builder()
                .rating(5)
                .comment("Increíble lugar, muy recomendado")
                .verified(true)
                .creationDate(LocalDate.now())
                .booking(booking)
                .build();

        Review review2 = Review.builder()
                .rating(4)
                .comment("Muy buen alojamiento, aunque el wifi a veces fallaba")
                .verified(true)
                .creationDate(LocalDate.now())
                .booking(booking2)
                .build();
        List<Review> reviews = List.of(review, review2);

        reviewRepository.saveAll(reviews);
    }
}