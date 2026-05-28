package com.demo.repositories;

import com.demo.model.Listing;
import com.demo.model.enums.ListingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findTop10ByIsActiveTrueOrderByRegisteredAtDesc();
    List<Listing> findByIsActiveTrue();
    List<Listing> findByIsActiveTrueAndPricePerNightBetween(Double minPrice, Double maxPrice);
    List<Listing> findByIsActiveTrueAndMaxGuestsGreaterThanEqual(Integer minGuests);

    @Query("""
    SELECT l FROM Listing l
    WHERE (:type IS NULL OR l.type = :type)
      AND (:minPrice IS NULL OR l.pricePerNight >= :minPrice)
      AND (:maxPrice IS NULL OR l.pricePerNight <= :maxPrice)
      AND (:guests IS NULL OR l.maxGuests >= :guests)
      AND (:nights IS NULL OR l.minNights <= :nights)
      AND (:nights IS NULL OR l.maxNights >= :nights)
""")
    List<Listing> search(
            @Param("type") ListingType type,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("guests") Integer guests,
            @Param("nights") Integer nights
    );

        List<Listing> findByOwnerId(Long ownerId);
}