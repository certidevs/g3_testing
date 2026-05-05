package com.demo.repositories;

import com.demo.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findByIsActiveTrue();
    List<Listing> findByIsActiveTrueAndPricePerNightBetween(Double minPrice, Double maxPrice);
    List<Listing> findByIsActiveTrueAndMaxGuestsGreaterThanEqual(Integer minGuests);

}