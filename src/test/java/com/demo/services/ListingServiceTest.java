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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ListingService listingService;

    private Listing loft;
    private Listing apto;
    private Listing chalet;
    private List<Listing> allListings;

    @BeforeEach
    void setUp() {
        loft = Listing.builder()
                .id(1L)
                .title("Loft Industrial")
                .type(ListingType.LOFT)
                .pricePerNight(100.0)
                .maxGuests(2)
                .minNights(1)
                .maxNights(10)
                .city(City.MADRID)
                .registeredAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .deleted(false)
                .build();

        apto = Listing.builder()
                .id(2L)
                .title("Apartamento Centro")
                .type(ListingType.APARTAMENTO)
                .pricePerNight(150.0)
                .maxGuests(4)
                .minNights(2)
                .maxNights(20)
                .city(City.MADRID)
                .registeredAt(LocalDateTime.of(2026, 2, 1, 12, 0))
                .deleted(false)
                .build();

        chalet = Listing.builder()
                .id(3L)
                .title("Chalet de Lujo")
                .type(ListingType.CHALET)
                .pricePerNight(300.0)
                .maxGuests(6)
                .minNights(3)
                .maxNights(30)
                .city(City.ALICANTE)
                .registeredAt(LocalDateTime.of(2025, 12, 1, 12, 0))
                .deleted(false)
                .build();

        allListings = new ArrayList<>(List.of(loft, apto, chalet));
    }

    @Test
    void searchInvalidPriceRangeReturnsEmptyList() {
        List<Listing> result = listingService.search(null, 200.0, 100.0, null, null, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(listingRepository);
    }

    @Test
    void searchWithoutFiltersReturnsAllNonDeletedListings() {
        Listing deletedListing = Listing.builder().id(4L).deleted(true).build();
        allListings.add(deletedListing);

        when(listingRepository.findAll()).thenReturn(allListings);

        List<Listing> result = listingService.search(null, null, null, null, null, null, null, null, null);

        assertEquals(3, result.size());
        assertFalse(result.contains(deletedListing));
    }

    @Test
    void searchByFiltersCombination() {
        when(listingRepository.findAll()).thenReturn(allListings);

        List<Listing> result = listingService.search(
                ListingType.LOFT,
                50.0,
                120.0,
                2,
                5,
                City.MADRID,
                null, null, null
        );

        assertEquals(1, result.size());
        assertEquals("Loft Industrial", result.get(0).getTitle());
    }

    @Test
    void searchWithDatesAvailable() {
        when(listingRepository.findAll()).thenReturn(List.of(loft));

        Booking booking = Booking.builder()
                .status(BookingStatus.CONFIRMED)
                .checkIn(LocalDateTime.of(2026, 7, 10, 15, 0))
                .checkOut(LocalDateTime.of(2026, 7, 15, 12, 0))
                .build();

        when(bookingRepository.findByListingId(1L)).thenReturn(List.of(booking));

        List<Listing> result = listingService.search(
                null, null, null, null, null, null,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 5),
                null
        );

        assertEquals(1, result.size());
    }

    @Test
    void searchWithDatesOverlapReturnsEmpty() {
        when(listingRepository.findAll()).thenReturn(List.of(loft));

        Booking booking = Booking.builder()
                .status(BookingStatus.CONFIRMED)
                .checkIn(LocalDateTime.of(2026, 7, 10, 15, 0))
                .checkOut(LocalDateTime.of(2026, 7, 15, 12, 0))
                .build();

        when(bookingRepository.findByListingId(1L)).thenReturn(List.of(booking));

        List<Listing> result = listingService.search(
                null, null, null, null, null, null,
                LocalDate.of(2026, 7, 12),
                LocalDate.of(2026, 7, 18),
                null
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void searchWithDatesSingleStartAndNullEnd() {
        when(listingRepository.findAll()).thenReturn(List.of(loft));
        when(bookingRepository.findByListingId(1L)).thenReturn(List.of());

        List<Listing> result = listingService.search(
                null, null, null, null, null, null,
                LocalDate.of(2026, 7, 12),
                null,
                null
        );

        assertEquals(1, result.size());
    }

    @Test
    void searchWithDatesNullStartAndSingleEnd() {
        when(listingRepository.findAll()).thenReturn(List.of(loft));
        when(bookingRepository.findByListingId(1L)).thenReturn(List.of());

        List<Listing> result = listingService.search(
                null, null, null, null, null, null,
                null,
                LocalDate.of(2026, 7, 12),
                null
        );

        assertEquals(1, result.size());
    }

    @Test
    void applySortPriceAscending() {
        when(listingRepository.findAll()).thenReturn(allListings);

        List<Listing> result = listingService.search(null, null, null, null, null, null, null, null, "priceAsc");

        assertEquals(3, result.size());
        assertEquals(100.0, result.get(0).getPricePerNight());
        assertEquals(150.0, result.get(1).getPricePerNight());
        assertEquals(300.0, result.get(2).getPricePerNight());
    }

    @Test
    void applySortPriceDescending() {
        when(listingRepository.findAll()).thenReturn(allListings);

        List<Listing> result = listingService.search(null, null, null, null, null, null, null, null, "priceDesc");

        assertEquals(3, result.size());
        assertEquals(300.0, result.get(0).getPricePerNight());
        assertEquals(150.0, result.get(1).getPricePerNight());
        assertEquals(100.0, result.get(2).getPricePerNight());
    }

    @Test
    void applySortDateNewest() {
        when(listingRepository.findAll()).thenReturn(allListings);

        List<Listing> result = listingService.search(null, null, null, null, null, null, null, null, "dateNewest");

        assertEquals("Apartamento Centro", result.get(0).getTitle());
        assertEquals("Loft Industrial", result.get(1).getTitle());
        assertEquals("Chalet de Lujo", result.get(2).getTitle());
    }

    @Test
    void applySortDateOldest() {
        when(listingRepository.findAll()).thenReturn(allListings);

        List<Listing> result = listingService.search(null, null, null, null, null, null, null, null, "dateOldest");

        assertEquals("Chalet de Lujo", result.get(0).getTitle());
        assertEquals("Loft Industrial", result.get(1).getTitle());
        assertEquals("Apartamento Centro", result.get(2).getTitle());
    }

    @Test
    void applySortUnknownStringReturnsUnsorted() {
        when(listingRepository.findAll()).thenReturn(allListings);

        List<Listing> result = listingService.search(null, null, null, null, null, null, null, null, "unknown_sort");

        assertEquals(allListings, result);
    }

    @Test
    void applySortRatingDescWithProjections() {
        when(listingRepository.findAll()).thenReturn(allListings);

        ListingRatingProjection p1 = mock(ListingRatingProjection.class);
        when(p1.getListingId()).thenReturn(1L);
        when(p1.getAvgRating()).thenReturn(4.0);

        ListingRatingProjection p2 = mock(ListingRatingProjection.class);
        when(p2.getListingId()).thenReturn(2L);
        when(p2.getAvgRating()).thenReturn(5.0);

        when(reviewRepository.findAverageRatingsByListing()).thenReturn(List.of(p1, p2));

        List<Listing> result = listingService.search(null, null, null, null, null, null, null, null, "ratingDesc");

        assertEquals(3, result.size());
        assertEquals(2L, result.get(0).getId()); // 5.0 rating primero
        assertEquals(1L, result.get(1).getId()); // 4.0 rating segundo
        assertEquals(3L, result.get(2).getId()); // Sin rating (-1.0) al final
    }

    @Test
    void applySortRatingAscWithProjections() {
        when(listingRepository.findAll()).thenReturn(allListings);

        ListingRatingProjection p1 = mock(ListingRatingProjection.class);
        when(p1.getListingId()).thenReturn(1L);
        when(p1.getAvgRating()).thenReturn(4.0);

        ListingRatingProjection p2 = mock(ListingRatingProjection.class);
        when(p2.getListingId()).thenReturn(2L);
        when(p2.getAvgRating()).thenReturn(5.0);

        when(reviewRepository.findAverageRatingsByListing()).thenReturn(List.of(p1, p2));

        List<Listing> result = listingService.search(null, null, null, null, null, null, null, null, "ratingAsc");

        assertEquals(3, result.size());
        assertEquals(1L, result.get(0).getId()); // 4.0 rating primero en asc
        assertEquals(2L, result.get(1).getId()); // 5.0 rating segundo en asc
        assertEquals(3L, result.get(2).getId()); // Sin rating (-1.0) siempre al final
    }
}