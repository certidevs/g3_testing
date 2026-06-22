package com.demo.config;

import com.demo.model.*;
import com.demo.model.enums.*;
import com.demo.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
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
    private final AmenityLineRepository amenityLineRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        reviewRepository.deleteAll();
        amenityLineRepository.deleteAll();
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

        String passwordEncriptada = passwordEncoder.encode("1234");

        User admin = User.builder()
                .name("Admin")
                .email("admin@openhouse.com")
                .username("admin@openhouse.com")
                .password(passwordEncriptada)
                .role(Role.ROLE_ADMIN)
                .build();

        User admin2 = User.builder()
                .name("Alex Pro")
                .email("alex@pro.com")
                .username("alex@pro.com")
                .password(passwordEncriptada)
                .role(Role.ROLE_HOST)
                .build();

        User guest = User.builder()
                .name("Sonia Lopez")
                .email("sonia@mail.com")
                .username("sonia@mail.com")
                .password(passwordEncriptada)
                .role(Role.ROLE_USER)
                .build();

        User guest2 = User.builder()
                .name("Maria Lopez")
                .email("maria@pro.com")
                .username("maria@pro.com")
                .password(passwordEncriptada)
                .role(Role.ROLE_USER)
                .build();

        User userExtra = User.builder()
                .name("Juan Perez")
                .email("juan@pro.com")
                .username("juan@pro.com")
                .password(passwordEncriptada)
                .role(Role.ROLE_HOST)
                .build();

        userRepository.saveAll(List.of(admin, admin2, guest, guest2, userExtra));

        Listing loft = Listing.builder()
                .title("Loft Industrial")
                .shortDescription("Espacio abierto y moderno")
                .longDescription("Ubicado en la zona artística, con techos altos y mucha luz")
                .pricePerNight(110.0)
                .minNights(1)
                .maxNights(20)
                .maxGuests(3)
                .imageUrl("/images/1.jpg")
                .registeredAt(LocalDateTime.now())
                .owner(admin2)
                .city(City.MADRID)
                .isActive(true)
                .type(ListingType.LOFT)
                .build();

        Listing ap = Listing.builder()
                .title("Apartamento con Vistas")
                .shortDescription("Apartamento moderno con vistas al mar")
                .longDescription("Ubicado en la zona turística, con balcón y piscina")
                .pricePerNight(150.0)
                .minNights(2)
                .maxNights(15)
                .maxGuests(4)
                .imageUrl("/images/2.jpg")
                .registeredAt(LocalDateTime.now())
                .owner(admin2)
                .isActive(true)
                .city(City.ALICANTE)
                .type(ListingType.APARTAMENTO)
                .build();

        Listing ap2 = Listing.builder()
                .title("Apartamento en el Centro")
                .shortDescription("Perfecto para turistas")
                .longDescription("A 5 minutos de la plaza principal, totalmente equipado")
                .pricePerNight(85.0)
                .minNights(1)
                .maxNights(15)
                .maxGuests(2)
                .imageUrl("/images/3.jpg")
                .registeredAt(LocalDateTime.now())
                .owner(userExtra)
                .city(City.ASTURIAS)
                .isActive(true)
                .type(ListingType.APARTAMENTO)
                .build();

        Listing cf = Listing.builder()
                .title("Casa Familiar con Jardín")
                .shortDescription("Ideal para familias")
                .longDescription("Amplio jardín, zona tranquila, perfecta para vacaciones largas")
                .pricePerNight(150.0)
                .minNights(2)
                .maxNights(30)
                .maxGuests(6)
                .imageUrl("/images/5.jpg")
                .registeredAt(LocalDateTime.now())
                .owner(guest2)
                .city(City.SEVILLA)
                .isActive(true)
                .type(ListingType.CASA)
                .build();

        Listing hp = Listing.builder()
                .title("Habitación Privada en Piso Compartido")
                .shortDescription("Buena ubicación")
                .longDescription("Habitación luminosa, acceso a cocina y salón")
                .pricePerNight(30.0)
                .minNights(1)
                .maxNights(10)
                .maxGuests(1)
                .imageUrl("/images/4.jpg")
                .registeredAt(LocalDateTime.now())
                .owner(guest2)
                .city(City.VALENCIA)
                .isActive(true)
                .type(ListingType.HABITACION_PRIVADA)
                .build();

        Listing vi = Listing.builder()
                .title("Villa de Lujo con Piscina")
                .shortDescription("Privacidad y Confort")
                .longDescription("Piscina privada, vistas al mar, acabados premium")
                .pricePerNight(350.0)
                .minNights(3)
                .maxNights(30)
                .maxGuests(8)
                .imageUrl("/images/6.jpg")
                .registeredAt(LocalDateTime.now())
                .owner(userExtra)
                .city(City.BARCELONA)
                .isActive(true)
                .type(ListingType.VILLA)
                .build();

        Listing cha = Listing.builder()
                .title("Chalet en el Bosque")
                .shortDescription("Desconexión total")
                .longDescription("Rodeada de naturaleza, perfecta para escapadas")
                .pricePerNight(95.0)
                .minNights(2)
                .maxNights(15)
                .maxGuests(4)
                .imageUrl("/images/7.jpg")
                .registeredAt(LocalDateTime.now())
                .owner(userExtra)
                .city(City.BILBAO)
                .isActive(false)
                .type(ListingType.CHALET)
                .build();

        List<Listing> listings = List.of(loft, ap, ap2, cf, hp, vi, cha);
        listingRepository.saveAll(listings);

        // ── CATÁLOGO DE AMENITIES (generado desde el enum) ────────────
        List<Amenity> catalogo = Arrays.stream(AmenityType.values())
                .map(type -> Amenity.builder()
                        .name(type.getLabel())
                        .icon(type.getIcon().replace("fa-", ""))
                        .type(type)
                        .build())
                .toList();
        List<Amenity> savedCatalogo = amenityRepository.saveAll(catalogo);

// ── AMENITY LINES (ejemplos para listings de prueba) ──────────
        Amenity wifi = savedCatalogo.stream()
                .filter(a -> a.getType() == AmenityType.WIFI).findFirst().orElseThrow();
        Amenity calefaccion = savedCatalogo.stream()
                .filter(a -> a.getType() == AmenityType.CALEFACCION).findFirst().orElseThrow();
        Amenity piscina = savedCatalogo.stream()
                .filter(a -> a.getType() == AmenityType.PISCINA).findFirst().orElseThrow();
        Amenity ac = savedCatalogo.stream()
                .filter(a -> a.getType() == AmenityType.AIRE_ACONDICIONADO).findFirst().orElseThrow();

        amenityLineRepository.saveAll(List.of(
                AmenityLine.builder().amenity(wifi).listing(loft).quantity(1).build(),
                AmenityLine.builder().amenity(calefaccion).listing(loft).quantity(1).build(),
                AmenityLine.builder().amenity(wifi).listing(ap).quantity(1).build(),
                AmenityLine.builder().amenity(piscina).listing(vi).quantity(1).build(),
                AmenityLine.builder().amenity(ac).listing(vi).quantity(1).build()
        ));

        Booking booking = Booking.builder()
                .checkIn(LocalDateTime.now().plusDays(1))
                .checkOut(LocalDateTime.now().plusDays(3))
                .status(BookingStatus.CONFIRMED)
                .guest(guest)
                .totalPrice(241.0)
                .listing(loft)
                .build();

        Booking booking2 = Booking.builder()
                .checkIn(LocalDateTime.of(2026, 4, 20, 15, 30))
                .checkOut(LocalDateTime.of(2026, 4, 25, 15, 30))
                .status(BookingStatus.CONFIRMED)
                .guest(guest)
                .totalPrice(220.0)
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
                .sentAt(LocalDateTime.now().minusMinutes(10))
                .isRead(true)
                .build();

        Message m2 = Message.builder()
                .content("Sí, se te enviará 2 horas antes de tu llegada.")
                .sender(admin2)
                .conversation(conv)
                .sentAt(LocalDateTime.now().minusMinutes(7))
                .isRead(false)
                .build();

        Message m3 = Message.builder()
                .content("Te esperamos con muchas ganas.")
                .sender(admin2)
                .conversation(conv)
                .sentAt(LocalDateTime.now().minusMinutes(6))
                .isRead(false)
                .build();

        Message m4 = Message.builder()
                .content("Muchas Gracias !!")
                .sender(guest)
                .conversation(conv)
                .sentAt(LocalDateTime.now().minusMinutes(2))
                .isRead(false)
                .build();

        messageRepository.saveAll(List.of(m1, m2, m3, m4));

        Review review = Review.builder()
                .rating(5)
                .comment("Increíble lugar, muy recomendado")
                .verified(true)
                .creationDate(LocalDateTime.now())
                .booking(booking)
                .build();

        Review review2 = Review.builder()
                .rating(4)
                .comment("Muy buen alojamiento, aunque el wifi a veces fallaba")
                .verified(true)
                .creationDate(LocalDateTime.now())
                .booking(booking2)
                .build();

        List<Review> reviews = List.of(review, review2);
        reviewRepository.saveAll(reviews);
    }
}