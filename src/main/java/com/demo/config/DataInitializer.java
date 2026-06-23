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

        User guest3 = User.builder()
                .name("Pedro Sanchez")
                .email("pedro@mail.com")
                .username("pedro@mail.com")
                .password(passwordEncriptada)
                .role(Role.ROLE_USER)
                .build();

        userRepository.saveAll(List.of(admin, admin2, guest, guest2, userExtra, guest3));

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

        Listing estudio = Listing.builder()
                .title("Estudio Moderno en el Albaicín")
                .shortDescription("Encanto andaluz en pleno casco histórico")
                .longDescription("Estudio reformado a pocos pasos de la Alhambra, ideal para parejas")
                .pricePerNight(70.0)
                .minNights(1)
                .maxNights(12)
                .maxGuests(2)
                .imageUrl("/images/8.jpg")
                .registeredAt(LocalDateTime.now())
                .owner(admin2)
                .city(City.GRANADA)
                .isActive(true)
                .type(ListingType.APARTAMENTO)
                .build();

        Listing casaRural = Listing.builder()
                .title("Casa Rural con Encanto")
                .shortDescription("Naturaleza y tranquilidad")
                .longDescription("Casa de campo rodeada de olivos, perfecta para desconectar")
                .pricePerNight(90.0)
                .minNights(2)
                .maxNights(20)
                .maxGuests(5)
                .imageUrl("/images/9.jpg")
                .registeredAt(LocalDateTime.now())
                .owner(userExtra)
                .city(City.GRANADA)
                .isActive(true)
                .type(ListingType.CASA)
                .build();

        Listing atico = Listing.builder()
                .title("Ático con Vistas a Sierra Nevada")
                .shortDescription("Terraza privada con vistas únicas")
                .longDescription("Ático luminoso con terraza panorámica, totalmente equipado")
                .pricePerNight(120.0)
                .minNights(2)
                .maxNights(15)
                .maxGuests(4)
                .imageUrl("/images/10.jpg")
                .registeredAt(LocalDateTime.now())
                .owner(admin2)
                .city(City.GRANADA)
                .isActive(true)
                .type(ListingType.APARTAMENTO)
                .build();

        Listing bungalow = Listing.builder()
                .title("Bungalow Tranquilo")
                .shortDescription("Escapada relajante")
                .longDescription("Pequeño bungalow independiente con jardín privado")
                .pricePerNight(60.0)
                .minNights(1)
                .maxNights(10)
                .maxGuests(2)
                .imageUrl("/images/11.jpg")
                .registeredAt(LocalDateTime.now())
                .owner(userExtra)
                .city(City.ALICANTE)
                .isActive(true)
                .type(ListingType.CHALET)
                .build();

        Listing loftCentrico = Listing.builder()
                .title("Loft Céntrico")
                .shortDescription("En el corazón de la ciudad")
                .longDescription("Loft de diseño a un paso de todos los monumentos principales")
                .pricePerNight(100.0)
                .minNights(1)
                .maxNights(18)
                .maxGuests(3)
                .imageUrl("/images/12.jpg")
                .registeredAt(LocalDateTime.now())
                .owner(admin2)
                .city(City.BILBAO)
                .isActive(true)
                .type(ListingType.LOFT)
                .build();

        Listing villaAndaluza = Listing.builder()
                .title("Villa Andaluza con Piscina")
                .shortDescription("Lujo y descanso")
                .longDescription("Villa amplia con piscina privada y jardines cuidados")
                .pricePerNight(280.0)
                .minNights(3)
                .maxNights(25)
                .maxGuests(7)
                .imageUrl("/images/13.jpg")
                .registeredAt(LocalDateTime.now())
                .owner(userExtra)
                .city(City.GRANADA)
                .isActive(true)
                .type(ListingType.VILLA)
                .build();

        List<Listing> listings = List.of(loft, ap, ap2, cf, hp, vi, cha,
                estudio, casaRural, atico, bungalow, loftCentrico, villaAndaluza);
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
                .checkIn(LocalDateTime.of(2026, 1, 5, 15, 30))
                .checkOut(LocalDateTime.of(2026, 1, 8, 15, 30))
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

        Booking booking3 = Booking.builder()
                .checkIn(LocalDateTime.of(2026, 2, 10, 16, 0))
                .checkOut(LocalDateTime.of(2026, 2, 13, 11, 0))
                .status(BookingStatus.CONFIRMED)
                .guest(guest2)
                .totalPrice(255.0)
                .listing(ap2)
                .build();

        Booking booking4 = Booking.builder()
                .checkIn(LocalDateTime.of(2026, 3, 1, 16, 0))
                .checkOut(LocalDateTime.of(2026, 3, 5, 11, 0))
                .status(BookingStatus.CONFIRMED)
                .guest(guest3)
                .totalPrice(1400.0)
                .listing(vi)
                .build();

        Booking booking5 = Booking.builder()
                .checkIn(LocalDateTime.of(2026, 4, 10, 16, 0))
                .checkOut(LocalDateTime.of(2026, 4, 12, 11, 0))
                .status(BookingStatus.CONFIRMED)
                .guest(guest3)
                .totalPrice(140.0)
                .listing(estudio)
                .build();

        Booking booking6 = Booking.builder()
                .checkIn(LocalDateTime.of(2026, 5, 1, 16, 0))
                .checkOut(LocalDateTime.of(2026, 5, 4, 11, 0))
                .status(BookingStatus.CONFIRMED)
                .guest(guest)
                .totalPrice(270.0)
                .listing(casaRural)
                .build();

        Booking booking7 = Booking.builder()
                .checkIn(LocalDateTime.of(2026, 5, 15, 16, 0))
                .checkOut(LocalDateTime.of(2026, 5, 17, 11, 0))
                .status(BookingStatus.CONFIRMED)
                .guest(guest2)
                .totalPrice(200.0)
                .listing(loftCentrico)
                .build();

        List<Booking> bookings = List.of(booking, booking2, booking3, booking4, booking5, booking6, booking7);
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

        Review review3 = Review.builder()
                .rating(4)
                .comment("Apartamento muy céntrico, perfecto para visitar la ciudad")
                .verified(true)
                .creationDate(LocalDateTime.now())
                .booking(booking3)
                .build();

        Review review4 = Review.builder()
                .rating(5)
                .comment("La villa superó nuestras expectativas, volveremos seguro")
                .verified(true)
                .creationDate(LocalDateTime.now())
                .booking(booking4)
                .build();

        Review review5 = Review.builder()
                .rating(4)
                .comment("Estudio pequeño pero con muchísimo encanto y bien ubicado")
                .verified(true)
                .creationDate(LocalDateTime.now())
                .booking(booking5)
                .build();

        Review review6 = Review.builder()
                .rating(5)
                .comment("La casa rural es una pasada, mucha paz y tranquilidad")
                .verified(true)
                .creationDate(LocalDateTime.now())
                .booking(booking6)
                .build();

        Review review7 = Review.builder()
                .rating(3)
                .comment("Buena ubicación, aunque el loft era algo más pequeño de lo esperado")
                .verified(true)
                .creationDate(LocalDateTime.now())
                .booking(booking7)
                .build();

        List<Review> reviews = List.of(review, review2, review3, review4, review5, review6, review7);
        reviewRepository.saveAll(reviews);
    }
}