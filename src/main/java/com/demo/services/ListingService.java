package com.demo.services;

import com.demo.model.Booking;
import com.demo.model.Listing;
import com.demo.model.enums.BookingStatus;
import com.demo.model.enums.City;
import com.demo.model.enums.ListingType;
import com.demo.repositories.BookingRepository;
import com.demo.repositories.ListingRepository;
import com.demo.repositories.ReviewRepository;
import com.demo.repositories.ListingRatingProjection;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    public ListingService(ListingRepository listingRepository,
                          BookingRepository bookingRepository,ReviewRepository reviewRepository) {
        this.listingRepository = listingRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<Listing> search(
            ListingType type,
            Double minPrice,
            Double maxPrice,
            Integer guests,
            Integer nights,
            City city,
            LocalDate start,
            LocalDate end,
            String sort) {

        // Validación: si minPrice > maxPrice, no se ejecuta la búsqueda
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            return List.of();
        }
        List<Listing> listings = listingRepository.findAll();

        listings = listings.stream()
                .filter(l -> !Boolean.TRUE.equals(l.getDeleted()))
                .filter(l -> type == null || l.getType() == type)
                .filter(l -> minPrice == null || l.getPricePerNight() >= minPrice)
                .filter(l -> maxPrice == null || l.getPricePerNight() <= maxPrice)
                .filter(l -> guests == null || l.getMaxGuests() >= guests)
                .filter(l -> nights == null || (l.getMinNights() <= nights && l.getMaxNights() >= nights))
                .filter(l -> city == null || l.getCity() == city)
                .toList();

        boolean filterByDates = (start != null || end != null);

        if (filterByDates) {
            listings = listings.stream()
                    .filter(l -> isAvailable(l, start, end))
                    .toList();
        }

        return applySort(listings, sort);
    }

    private boolean isAvailable(Listing listing, LocalDate start, LocalDate end) {

        if (start != null && end == null) {
            end = start;
        }

        if (start == null && end != null) {
            start = end;
        }

        if (start == null && end == null) {
            return true;
        }

        List<Booking> bookings = bookingRepository.findByListingId(listing.getId());

        for (Booking existing : bookings) {

            if (existing.getStatus() == BookingStatus.CONFIRMED ||
                    existing.getStatus() == BookingStatus.PENDING) {

                LocalDate existingStart = existing.getCheckIn().toLocalDate();
                LocalDate existingEnd = existing.getCheckOut().toLocalDate();

                boolean overlap =
                        !end.isBefore(existingStart) &&
                                !start.isAfter(existingEnd);

                if (overlap) {
                    return false;
                }
            }
        }

        return true;
    }
    private List<Listing> applySort(List<Listing> listings, String sort) {
        if (sort == null || sort.isBlank()) {
            return listings;
        }

        switch (sort) {
            case "priceAsc":
                return listings.stream()
                        .sorted(Comparator.comparing(Listing::getPricePerNight))
                        .toList();
            case "priceDesc":
                return listings.stream()
                        .sorted(Comparator.comparing(Listing::getPricePerNight).reversed())
                        .toList();
            case "dateNewest":
                return listings.stream()
                        .sorted(Comparator.comparing(Listing::getRegisteredAt).reversed())
                        .toList();
            case "dateOldest":
                return listings.stream()
                        .sorted(Comparator.comparing(Listing::getRegisteredAt))
                        .toList();
            case "ratingDesc":
                return sortByRating(listings, true);
            case "ratingAsc":
                return sortByRating(listings, false);
            default:
                return listings;
        }
    }
    private List<Listing> sortByRating(List<Listing> listings, boolean descending) {
        Map<Long, Double> ratings = reviewRepository.findAverageRatingsByListing()
                .stream()
                .collect(Collectors.toMap(
                        ListingRatingProjection::getListingId,
                        ListingRatingProjection::getAvgRating
                ));

        Comparator<Listing> baseComparator = Comparator.comparing(
                (Listing l) -> ratings.getOrDefault(l.getId(), -1.0)
        );
        Comparator<Listing> byRating = descending ? baseComparator.reversed() : baseComparator;

        // Listings sin rating (-1.0) siempre al final, sin importar el orden
        return listings.stream()
                .sorted((a, b) -> {
                    boolean aHasRating = ratings.containsKey(a.getId());
                    boolean bHasRating = ratings.containsKey(b.getId());
                    if (aHasRating != bHasRating) {
                        return aHasRating ? -1 : 1; // los que tienen rating van primero
                    }
                    return byRating.compare(a, b);
                })
                .toList();
    }
}