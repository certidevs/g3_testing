package com.demo.repositories;

import com.demo.model.Amenity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@DataJpaTest
class AmenityRepositoryTest {

    @Autowired
    private AmenityRepository amenityRepository;

    private Amenity wifi;
    private Amenity piscina;
    private Amenity parking;
    private Amenity cocina;

    @BeforeEach
    void setUp() {
        wifi = Amenity.builder().name("Wifi").description("Alta velocidad").icon("wifi").build();
        piscina = Amenity.builder().name("Piscina").description("Climatizada").icon("water").build();
        parking = Amenity.builder().name("Parking").description("Subterráneo").icon("car").build();
        cocina = Amenity.builder().name("Cocina").description("Equipada").icon("kitchen-set").build();

        amenityRepository.saveAll(List.of(wifi, piscina, parking, cocina));
    }

    @Test
    void amenityExists() {
        List<Amenity> amenities = amenityRepository.findAll();
        assertEquals(4, amenities.size());
    }

    @Test
    void findByName() {
        List<Amenity> result = amenityRepository.findByName("Wifi");
        assertFalse(result.isEmpty());
        assertEquals("Wifi", result.get(0).getName());
    }

    @Test
    void findByNameContainingIgnoreCase() {
        List<Amenity> amenitiesFound = amenityRepository.findByNameContainingIgnoreCase("wi");
        assertEquals(1, amenitiesFound.size());
        assertEquals("Wifi", amenitiesFound.get(0).getName());

        List<Amenity> amenitiesFoundCaseInsensitive = amenityRepository.findByNameContainingIgnoreCase("c");
        assertEquals(2, amenitiesFoundCaseInsensitive.size());
        assertTrue(amenitiesFoundCaseInsensitive.stream().anyMatch(a -> a.getName().equals("Cocina")));
        assertTrue(amenitiesFoundCaseInsensitive.stream().anyMatch(a -> a.getName().equals("Piscina")));
    }
}