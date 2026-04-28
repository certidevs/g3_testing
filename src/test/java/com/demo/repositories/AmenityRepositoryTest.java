package com.demo.repositories;

import com.demo.model.Amenity;
import com.demo.model.Listing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
class AmenityRepositoryTest {

    @Autowired
    AmenityRepository amenityRepository;

    @Autowired
    ListingRepository listingRepository;

    Listing casa1;
    Listing casa2;

    @BeforeEach
    void setUp() {
            casa1 = Listing.builder().title("Casa 1").registeredAt(LocalDateTime.now()).build();
            casa2 = Listing.builder().title("Casa 2").build();

            listingRepository.saveAll(List.of(casa1, casa2));
            //añadir características a los amenities
            var wifi = Amenity.builder().name("Wifi").listing(casa1).build();
            var piscina = Amenity.builder().name("Piscina").listing(casa1).build();
            var parking = Amenity.builder().name("Parking").listing(casa2).build();
            var cocina = Amenity.builder().name("Cocina").listing(casa2).build();
            amenityRepository.saveAll(List.of(wifi, piscina, parking, cocina));

    }
    @Test
    void findByListing_Id() {
        List<Amenity> amenitiesCasa1 = amenityRepository.findByListing_Id(casa1.getId());
        List<Amenity> amenitiesCasa2 = amenityRepository.findByListing_Id(casa2.getId());

        assertEquals(2, amenitiesCasa1.size());
        assertEquals(2, amenitiesCasa2.size());
    }

}