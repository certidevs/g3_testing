package com.demo.repositories;

import com.demo.model.Listing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ListingRepositoryTest {

    @Autowired
    private ListingRepository listingRepository;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Devuelve solo listings activos")
    void findByIsActiveTrue_devuelveSoloActivos() {
        Listing activo = crearListing(150.0, 4, true);
        Listing inactivo = crearListing(100.0, 2, false);

        listingRepository.saveAll(List.of(activo, inactivo));

        List<Listing> result = listingRepository.findByIsActiveTrue();
        assertEquals(1, result.size());
        assertTrue(result.getFirst().getIsActive());
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando no hay ofertas activas")
    void devuelveListaVaciaSiNoHayActivos() {
        Listing inactivo1 = crearListing(70.0, 3, false);
        Listing inactivo2 = crearListing(50.0, 2, false);

        listingRepository.saveAll(List.of(inactivo1, inactivo2));

        List<Listing> result = listingRepository.findByIsActiveTrue();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Devuelve varios listings activos")
    void devuelveVariosActivos() {
        Listing activo1 = crearListing(70.0, 3, true);
        Listing activo2 = crearListing(50.0, 2, true);
        Listing inactivo1= crearListing(40.0, 1, false);

        listingRepository.saveAll(List.of(activo1, activo2, inactivo1));

        List<Listing> result = List.of(activo1, activo2);
        assertEquals(2, listingRepository.findByIsActiveTrue().size()); //valida que el tamaño de la lista devuelta por el repositorio sea 2, es decir, que solo se hayan devuelto los activos
        assertTrue(result.stream().allMatch(Listing::getIsActive)); //valida que todos los de la lista result sean activos
    }

    @Test
    @DisplayName("Devuelve solo listings activos dentro del rango de precio")
    void findByIsActiveTrueAndPricePerNightBetween() {
        Listing activoDentro = crearListing(70.0, 4, true); //dentro del rango
        Listing activoFuera = crearListing(150.0, 5, true); //fuera del rango
        Listing inactivoDentro = crearListing(90.0, 4, false); //dentro del rango pero inactivo

        listingRepository.saveAll(List.of(activoDentro, activoFuera, inactivoDentro));

        List<Listing> result = listingRepository.findByIsActiveTrueAndPricePerNightBetween(70.0, 100.0);
        assertEquals(1, result.size());
        assertTrue(result.stream().allMatch(Listing::getIsActive));
        assertEquals(70.0, result.getFirst().getPricePerNight());
    }

    @Test
    @DisplayName("Devuelve lista vacía si no hay listings activos dentro del rango de precio")
    void devuelveListaVaciaSinoHayCoincidencias() {
        Listing activoFuera = crearListing(150.0, 4, true); //fuera del rango
        Listing inactivoDentro= crearListing(80.0, 4, false); //dentro del rango pero inactivo

        listingRepository.saveAll(List.of(activoFuera, inactivoDentro));

        List<Listing> result = listingRepository.findByIsActiveTrueAndPricePerNightBetween(60.0, 100.0);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Devuelve varios listings activos dentro del rango de precio")
    void devuelveVariosActivosDentroDelRango() {
        Listing activoDentro1 = crearListing(70.0, 4, true); //dentro del rango
        Listing activoDentro2 = crearListing(90.0, 4, true); //dentro del rango
        Listing activoFuera = crearListing(120.0, 4, true); //fuera del rango
        Listing inactivoDentro= crearListing(90.0, 4, false); //dentro del rango pero inactivo

        listingRepository.saveAll(List.of(activoDentro1,activoDentro2,activoFuera,inactivoDentro));

        List<Listing> result = listingRepository.findByIsActiveTrueAndPricePerNightBetween(60.0, 100.0);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Listing::getIsActive));
    }

    @Test
    @DisplayName("Devuelve solo listings activos con maxGuests >= minGuests")
    void devuelveActivosConMaxGuestsMayorOIgual() {
        Listing activoCumple = crearListing(80.0, 4, true); //cumple con maxGuests
        Listing activoNoCumple = crearListing(80.0, 2, true); //no cumple con maxGuests
        Listing inactivoCumple = crearListing(80.0, 5, false); //cumple con maxGuests pero inactivo

        listingRepository.saveAll(List.of(activoCumple, activoNoCumple, inactivoCumple));

        List<Listing> result = listingRepository.findByIsActiveTrueAndMaxGuestsGreaterThanEqual(3);
        assertEquals(1, result.size());
        assertTrue(result.stream().allMatch(Listing::getIsActive));
        assertEquals(4, result.getFirst().getMaxGuests());
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando no hay activos con maxGuests >= minGuests")
    void devuelveListaVaciaSiNoHayCoincidencias() {
        Listing activoNoCumple = crearListing(80.0, 2, true); //no cumple con maxGuests
        Listing inactivoCumple= crearListing(80.0, 5, false); //cumple con maxGuests pero inactivo

        listingRepository.saveAll(List.of(activoNoCumple, inactivoCumple));

        List<Listing> result = listingRepository.findByIsActiveTrueAndMaxGuestsGreaterThanEqual(3);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Devuelve varios listings activos dentro del rango de precio")
    void devuelveVariosActivosQueCumplen() {
        Listing activoCumple1 = crearListing(80.0, 4, true); //cumple con maxGuests
        Listing activoCumple2 = crearListing(80.0, 5, true); //cumple con maxGuests
        Listing activoNoCumple = crearListing(80.0, 2, true); //no cumple con maxGuests
        Listing inactivoCumple= crearListing(80.0, 4, false); //cumple con maxGuests pero inactivo

        listingRepository.saveAll(List.of(activoCumple1,activoCumple2,activoNoCumple,inactivoCumple));

        List<Listing> result = listingRepository.findByIsActiveTrueAndMaxGuestsGreaterThanEqual(3);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Listing::getIsActive));
    }

    private Listing crearListing(Double precio, Integer maxGuests, Boolean activo) {
        return Listing.builder()
                .title("Text")
                .pricePerNight(precio)
                .maxGuests(maxGuests)
                .isActive(activo)
                .registeredAt(LocalDateTime.now())
                .build();
    }
}