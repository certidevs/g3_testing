package com.demo.services;

import com.demo.model.Booking;
import com.demo.model.Listing;
import com.demo.model.enums.BookingStatus;
import com.demo.model.enums.City;
import com.demo.model.enums.ListingType;
import com.demo.repositories.BookingRepository;
import com.demo.repositories.ListingRatingProjection;
import com.demo.repositories.ListingRepository;
import com.demo.repositories.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de {@link ListingService} con mocks de los repositorios.
 * Cubren: validación de precios, cada filtro de búsqueda, disponibilidad por
 * fechas (solape de reservas) y todas las ramas de ordenación.
 */
@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock
    ListingRepository listingRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    ReviewRepository reviewRepository;

    @InjectMocks
    ListingService listingService;

    // ───────────────────────── helpers ─────────────────────────

    private Listing listing(long id, ListingType type, double price, int guests,
                            int minNights, int maxNights, City city) {
        return Listing.builder()
                .id(id)
                .title("L" + id)
                .type(type)
                .pricePerNight(price)
                .maxGuests(guests)
                .minNights(minNights)
                .maxNights(maxNights)
                .city(city)
                .isActive(true)
                .deleted(false)
                .registeredAt(LocalDateTime.of(2026, 1, (int) id, 12, 0))
                .build();
    }

    private Booking booking(BookingStatus status, LocalDate checkIn, LocalDate checkOut) {
        return Booking.builder()
                .status(status)
                .checkIn(checkIn.atStartOfDay())
                .checkOut(checkOut.atStartOfDay())
                .build();
    }

    private ListingRatingProjection rating(long listingId, double avg) {
        return new ListingRatingProjection() {
            public Long getListingId() { return listingId; }
            public Double getAvgRating() { return avg; }
        };
    }

    /** Búsqueda sin filtros, solo con criterio de orden. */
    private List<Listing> searchSorted(String sort) {
        return listingService.search(null, null, null, null, null, null, null, null, sort);
    }

    // ───────────────────────── validación de precios ─────────────────────────

    @Test
    @DisplayName("minPrice > maxPrice devuelve lista vacía sin tocar repositorios")
    void minPriceGreaterThanMaxPriceReturnsEmpty() {
        List<Listing> result = listingService.search(
                null, 200.0, 100.0, null, null, null, null, null, null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(listingRepository, bookingRepository, reviewRepository);
    }

    // ───────────────────────── filtros ─────────────────────────

    @Test
    @DisplayName("sin filtros devuelve todos los no borrados")
    void noFiltersReturnsNonDeleted() {
        Listing a = listing(1, ListingType.APARTAMENTO, 100, 4, 1, 30, City.MADRID);
        Listing b = listing(2, ListingType.CASA, 80, 5, 2, 30, City.BARCELONA);
        Listing borrado = listing(3, ListingType.LOFT, 50, 2, 1, 30, City.SEVILLA);
        borrado.setDeleted(true);
        when(listingRepository.findAll()).thenReturn(List.of(a, b, borrado));

        List<Listing> result = searchSorted(null);

        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(a, b)));
        assertFalse(result.contains(borrado));
    }

    @Test
    @DisplayName("filtra por tipo")
    void filtersByType() {
        Listing apto = listing(1, ListingType.APARTAMENTO, 100, 4, 1, 30, City.MADRID);
        Listing casa = listing(2, ListingType.CASA, 80, 5, 2, 30, City.BARCELONA);
        when(listingRepository.findAll()).thenReturn(List.of(apto, casa));

        List<Listing> result = listingService.search(
                ListingType.CASA, null, null, null, null, null, null, null, null);

        assertEquals(List.of(casa), result);
    }

    @Test
    @DisplayName("filtra por rango de precio")
    void filtersByPriceRange() {
        Listing barato = listing(1, ListingType.APARTAMENTO, 50, 4, 1, 30, City.MADRID);
        Listing medio = listing(2, ListingType.APARTAMENTO, 100, 4, 1, 30, City.MADRID);
        Listing caro = listing(3, ListingType.APARTAMENTO, 200, 4, 1, 30, City.MADRID);
        when(listingRepository.findAll()).thenReturn(List.of(barato, medio, caro));

        List<Listing> result = listingService.search(
                null, 80.0, 150.0, null, null, null, null, null, null);

        assertEquals(List.of(medio), result);
    }

    @Test
    @DisplayName("filtra por número de huéspedes")
    void filtersByGuests() {
        Listing peque = listing(1, ListingType.APARTAMENTO, 50, 2, 1, 30, City.MADRID);
        Listing grande = listing(2, ListingType.APARTAMENTO, 100, 6, 1, 30, City.MADRID);
        when(listingRepository.findAll()).thenReturn(List.of(peque, grande));

        List<Listing> result = listingService.search(
                null, null, null, 5, null, null, null, null, null);

        assertEquals(List.of(grande), result);
    }

    @Test
    @DisplayName("filtra por noches dentro del rango min/max del alojamiento")
    void filtersByNights() {
        Listing cortas = listing(1, ListingType.APARTAMENTO, 50, 4, 1, 3, City.MADRID);
        Listing largas = listing(2, ListingType.APARTAMENTO, 100, 4, 5, 30, City.MADRID);
        when(listingRepository.findAll()).thenReturn(List.of(cortas, largas));

        List<Listing> result = listingService.search(
                null, null, null, null, 7, null, null, null, null);

        assertEquals(List.of(largas), result);
    }

    @Test
    @DisplayName("filtra por ciudad")
    void filtersByCity() {
        Listing madrid = listing(1, ListingType.APARTAMENTO, 50, 4, 1, 30, City.MADRID);
        Listing barcelona = listing(2, ListingType.APARTAMENTO, 100, 4, 1, 30, City.BARCELONA);
        when(listingRepository.findAll()).thenReturn(List.of(madrid, barcelona));

        List<Listing> result = listingService.search(
                null, null, null, null, null, City.BARCELONA, null, null, null);

        assertEquals(List.of(barcelona), result);
    }

    // ───────────────────────── disponibilidad por fechas ─────────────────────────

    @Test
    @DisplayName("excluye alojamiento con reserva CONFIRMED solapada")
    void excludesListingWithOverlappingConfirmedBooking() {
        Listing ocupado = listing(1, ListingType.APARTAMENTO, 100, 4, 1, 30, City.MADRID);
        Listing libre = listing(2, ListingType.APARTAMENTO, 100, 4, 1, 30, City.MADRID);
        when(listingRepository.findAll()).thenReturn(List.of(ocupado, libre));
        when(bookingRepository.findByListingId(1L)).thenReturn(List.of(
                booking(BookingStatus.CONFIRMED, LocalDate.of(2026, 7, 12), LocalDate.of(2026, 7, 18))));
        when(bookingRepository.findByListingId(2L)).thenReturn(List.of());

        List<Listing> result = listingService.search(
                null, null, null, null, null, null,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 15), null);

        assertEquals(List.of(libre), result);
    }

    @Test
    @DisplayName("una reserva CANCELED o no solapada no bloquea la disponibilidad")
    void canceledOrNonOverlappingBookingDoesNotBlock() {
        Listing l = listing(1, ListingType.APARTAMENTO, 100, 4, 1, 30, City.MADRID);
        when(listingRepository.findAll()).thenReturn(List.of(l));
        when(bookingRepository.findByListingId(1L)).thenReturn(List.of(
                booking(BookingStatus.CANCELED, LocalDate.of(2026, 7, 11), LocalDate.of(2026, 7, 14)),  // solapa pero cancelada
                booking(BookingStatus.CONFIRMED, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5))    // confirmada pero no solapa
        ));

        List<Listing> result = listingService.search(
                null, null, null, null, null, null,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 15), null);

        assertEquals(List.of(l), result);
    }

    @Test
    @DisplayName("con solo fecha de inicio (end=null) también evalúa disponibilidad")
    void onlyStartDateStillChecksAvailability() {
        Listing l = listing(1, ListingType.APARTAMENTO, 100, 4, 1, 30, City.MADRID);
        when(listingRepository.findAll()).thenReturn(List.of(l));
        when(bookingRepository.findByListingId(1L)).thenReturn(List.of(
                booking(BookingStatus.PENDING, LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 12))));

        List<Listing> result = listingService.search(
                null, null, null, null, null, null,
                LocalDate.of(2026, 7, 11), null, null);

        assertTrue(result.isEmpty()); // la reserva PENDING solapa el día 11
    }

    @Test
    @DisplayName("con solo fecha de fin (start=null) también evalúa disponibilidad")
    void onlyEndDateStillChecksAvailability() {
        Listing l = listing(1, ListingType.APARTAMENTO, 100, 4, 1, 30, City.MADRID);
        when(listingRepository.findAll()).thenReturn(List.of(l));
        when(bookingRepository.findByListingId(1L)).thenReturn(List.of());

        List<Listing> result = listingService.search(
                null, null, null, null, null, null,
                null, LocalDate.of(2026, 7, 20), null);

        assertEquals(List.of(l), result);
    }

    // ───────────────────────── ordenación ─────────────────────────

    @Test
    @DisplayName("sort=priceAsc ordena por precio ascendente")
    void sortPriceAsc() {
        Listing a = listing(1, ListingType.APARTAMENTO, 150, 4, 1, 30, City.MADRID);
        Listing b = listing(2, ListingType.APARTAMENTO, 80, 4, 1, 30, City.MADRID);
        Listing c = listing(3, ListingType.APARTAMENTO, 120, 4, 1, 30, City.MADRID);
        when(listingRepository.findAll()).thenReturn(List.of(a, b, c));

        assertEquals(List.of(b, c, a), searchSorted("priceAsc"));
    }

    @Test
    @DisplayName("sort=priceDesc ordena por precio descendente")
    void sortPriceDesc() {
        Listing a = listing(1, ListingType.APARTAMENTO, 150, 4, 1, 30, City.MADRID);
        Listing b = listing(2, ListingType.APARTAMENTO, 80, 4, 1, 30, City.MADRID);
        Listing c = listing(3, ListingType.APARTAMENTO, 120, 4, 1, 30, City.MADRID);
        when(listingRepository.findAll()).thenReturn(List.of(a, b, c));

        assertEquals(List.of(a, c, b), searchSorted("priceDesc"));
    }

    @Test
    @DisplayName("sort=dateNewest y dateOldest ordenan por fecha de alta")
    void sortByDate() {
        Listing viejo = listing(1, ListingType.APARTAMENTO, 100, 4, 1, 30, City.MADRID); // 2026-01-01
        Listing nuevo = listing(5, ListingType.APARTAMENTO, 100, 4, 1, 30, City.MADRID); // 2026-01-05
        when(listingRepository.findAll()).thenReturn(List.of(viejo, nuevo));

        assertEquals(List.of(nuevo, viejo), searchSorted("dateNewest"));
        assertEquals(List.of(viejo, nuevo), searchSorted("dateOldest"));
    }

    @Test
    @DisplayName("sort nulo, vacío o desconocido no cambia el orden")
    void sortNullBlankOrUnknownKeepsOrder() {
        Listing a = listing(1, ListingType.APARTAMENTO, 150, 4, 1, 30, City.MADRID);
        Listing b = listing(2, ListingType.APARTAMENTO, 80, 4, 1, 30, City.MADRID);
        when(listingRepository.findAll()).thenReturn(List.of(a, b));

        assertEquals(List.of(a, b), searchSorted(null));
        assertEquals(List.of(a, b), searchSorted(""));
        assertEquals(List.of(a, b), searchSorted("loQueSea"));
    }

    @Test
    @DisplayName("sort=ratingDesc ordena por valoración media; sin valoración al final")
    void sortRatingDesc() {
        Listing alta = listing(1, ListingType.APARTAMENTO, 100, 4, 1, 30, City.MADRID);   // 4.5
        Listing media = listing(2, ListingType.APARTAMENTO, 100, 4, 1, 30, City.MADRID);  // 3.0
        Listing sin = listing(3, ListingType.APARTAMENTO, 100, 4, 1, 30, City.MADRID);    // sin reseñas
        when(listingRepository.findAll()).thenReturn(List.of(sin, media, alta));
        when(reviewRepository.findAverageRatingsByListing())
                .thenReturn(List.of(rating(1, 4.5), rating(2, 3.0)));

        assertEquals(List.of(alta, media, sin), searchSorted("ratingDesc"));
    }

    @Test
    @DisplayName("sort=ratingAsc ordena ascendente; sin valoración al final")
    void sortRatingAsc() {
        Listing alta = listing(1, ListingType.APARTAMENTO, 100, 4, 1, 30, City.MADRID);   // 4.5
        Listing media = listing(2, ListingType.APARTAMENTO, 100, 4, 1, 30, City.MADRID);  // 3.0
        Listing sin = listing(3, ListingType.APARTAMENTO, 100, 4, 1, 30, City.MADRID);    // sin reseñas
        when(listingRepository.findAll()).thenReturn(List.of(sin, alta, media));
        when(reviewRepository.findAverageRatingsByListing())
                .thenReturn(List.of(rating(1, 4.5), rating(2, 3.0)));

        assertEquals(List.of(media, alta, sin), searchSorted("ratingAsc"));
    }
}
