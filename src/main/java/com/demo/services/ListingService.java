package com.demo.services;

import com.demo.model.Booking;
import com.demo.model.Listing;
import com.demo.model.enums.BookingStatus;
import com.demo.model.enums.City;
import com.demo.model.enums.ListingType;
import com.demo.repositories.BookingRepository;
import com.demo.repositories.ListingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final BookingRepository bookingRepository;

    public ListingService(ListingRepository listingRepository,
                          BookingRepository bookingRepository) {
        this.listingRepository = listingRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Listing> search(
            ListingType type,
            Double minPrice,
            Double maxPrice,
            Integer guests,
            Integer nights,
            City city,
            LocalDate start,
            LocalDate end) {

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

        return listings;
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
}