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
    void amenityExists(){
        List<Amenity> amenities = amenityRepository.findAll();

        assertEquals(4, amenities.size());
    }

    @Test
    void findByName(){
        Amenity amenity = amenityRepository.findByName("Wifi").get(0);

        assertEquals("Wifi", amenity.getName());
    }


    @Test
    void findByListing_Id() {
        List<Amenity> amenitiesCasa1 = amenityRepository.findByListing_Id(casa1.getId());
        List<Amenity> amenitiesCasa2 = amenityRepository.findByListing_Id(casa2.getId());

        assertEquals(2, amenitiesCasa1.size());
        assertEquals(2, amenitiesCasa2.size());
    }

    @Test
    void findByNameContainingIgnoreCase() {
        // Crea y guarda algunos amenities
        var wifi = Amenity.builder().name("Wifi").listing(casa1).build();
        var piscina = Amenity.builder().name("Piscina").listing(casa1).build();
        var parking = Amenity.builder().name("Parking").listing(casa2).build();
        var cocina = Amenity.builder().name("Cocina").listing(casa2).build();
        amenityRepository.saveAll(List.of(wifi, piscina, parking, cocina));

        // Busca amenities que contengan "wi" (debería encontrar "Wifi")
        List<Amenity> amenitiesFound = amenityRepository.findByNameContainingIgnoreCase("wi");

        // Verifica que se haya encontrado el amenity correcto
        assertEquals(1, amenitiesFound.size());
        assertEquals("Wifi", amenitiesFound.getFirst().getName());

        // Busca amenities que contengan "c" (debería encontrar "Cocina" y "Piscina")
        List<Amenity> amenitiesFoundCaseInsensitive = amenityRepository.findByNameContainingIgnoreCase("c");

        // Verifica que se hayan encontrado los amenities correctos
        assertEquals(2, amenitiesFoundCaseInsensitive.size());
        assertTrue(amenitiesFoundCaseInsensitive.stream().anyMatch(a -> a.getName().equals("Cocina")));
        assertTrue(amenitiesFoundCaseInsensitive.stream().anyMatch(a -> a.getName().equals("Piscina")));
    }

    @Test
    void findByNameAndListing_Id() {
        // Crea y guarda algunos amenities
        var wifi = Amenity.builder().name("Wifi").listing(casa1).build();
        var piscina = Amenity.builder().name("Piscina").listing(casa1).build();
        var parking = Amenity.builder().name("Parking").listing(casa2).build();
        var cocina = Amenity.builder().name("Cocina").listing(casa2).build();
        amenityRepository.saveAll(List.of(wifi, piscina, parking, cocina));

        // Busca el amenity "Wifi" para casa1
        List<Amenity> amenitiesFoundCasa1 = amenityRepository.findByNameAndListing_Id("Wifi", casa1.getId());

        // Verifica que se haya encontrado el amenity correcto para casa1
        assertEquals(2, amenitiesFoundCasa1.size());
        assertEquals("Wifi", amenitiesFoundCasa1.getFirst().getName());
        assertEquals(casa1.getId(), amenitiesFoundCasa1.getFirst().getListing().getId());

        // Busca el amenity "Parking" para casa1 (debería devolver una lista vacía)
        List<Amenity> amenitiesFoundCasa1Parking = amenityRepository.findByNameAndListing_Id("Parking", casa1.getId());

        // Verifica que no se haya encontrado ningún amenity para casa1
        assertTrue(amenitiesFoundCasa1Parking.isEmpty());

        // Busca el amenity "Cocina" para casa2
        List<Amenity> amenitiesFoundCasa2 = amenityRepository.findByNameAndListing_Id("Cocina", casa2.getId());

        // Verifica que se haya encontrado el amenity correcto para casa2
        assertEquals(2, amenitiesFoundCasa2.size());
        assertEquals("Cocina", amenitiesFoundCasa2.getFirst().getName());
        assertEquals(casa2.getId(), amenitiesFoundCasa2.getFirst().getListing().getId());
    }

}