package com.demo.repositories;

import com.demo.model.Listing;

import com.demo.model.enums.ListingType;
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

    //Pruebas del método @Search

    @Test
    @DisplayName("search() sin filtros devuelve todos los listings")
    void searchSinFiltros() {
        Listing l1 = crearListing(80.0, 3, true);
        Listing l2 = crearListing(120.0, 5, true);
        listingRepository.saveAll(List.of(l1, l2));

        List<Listing> result = listingRepository.search(null, null, null, null, null);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("search() filtra por type")
    void searchPorType() {
        Listing l1 = crearListing(80.0, 3, true);
        l1.setType(ListingType.CASA);

        Listing l2 = crearListing(80.0, 3, true);
        l2.setType(ListingType.APARTAMENTO);

        listingRepository.saveAll(List.of(l1, l2));

        List<Listing> result = listingRepository.search(ListingType.CASA, null, null, null, null);

        assertEquals(1, result.size());
        assertEquals(ListingType.CASA, result.getFirst().getType());
    }

    @Test
    @DisplayName("search() filtra por minPrice")
    void searchPorMinPrice() {
        Listing barato = crearListing(50.0, 3, true);
        Listing caro = crearListing(150.0, 3, true);

        listingRepository.saveAll(List.of(barato, caro));

        List<Listing> result = listingRepository.search(null, 100.0, null, null, null);

        assertEquals(1, result.size());
        assertTrue(result.getFirst().getPricePerNight() >= 100.0);
    }

    @Test
    @DisplayName("search() filtra por maxPrice")
    void searchPorMaxPrice() {
        Listing barato = crearListing(50.0, 3, true);
        Listing caro = crearListing(150.0, 3, true);

        listingRepository.saveAll(List.of(barato, caro));

        List<Listing> result = listingRepository.search(null, null, 100.0, null, null);

        assertEquals(1, result.size());
        assertTrue(result.getFirst().getPricePerNight() <= 100.0);
    }

    @Test
    @DisplayName("search() filtra por guests")
    void searchPorGuests() {
        Listing l1 = crearListing(80.0, 2, true);
        Listing l2 = crearListing(80.0, 5, true);

        listingRepository.saveAll(List.of(l1, l2));

        List<Listing> result = listingRepository.search(null, null, null, 4, null);

        assertEquals(1, result.size());
        assertTrue(result.getFirst().getMaxGuests() >= 4);
    }


    @Test
    @DisplayName("search() combina correctamente varios filtros")
    void searchCombinado() {
        Listing l1 = crearListing(100.0, 4, true);
        l1.setType(ListingType.CASA);
        l1.setMinNights(1);
        l1.setMaxNights(10);

        Listing l2 = crearListing(200.0, 2, true);
        l2.setType(ListingType.APARTAMENTO);
        l2.setMinNights(1);
        l2.setMaxNights(10);

        listingRepository.saveAll(List.of(l1, l2));

        List<Listing> result = listingRepository.search(
                ListingType.CASA, 80.0, 150.0, 3, 1
        );

        assertEquals(1, result.size());
        assertEquals(ListingType.CASA, result.getFirst().getType());
    }

    @Test
    @DisplayName("search() devuelve lista vacía cuando no hay coincidencias")
    void searchSinResultados() {
        Listing l1 = crearListing(80.0, 3, true);
        listingRepository.save(l1);

        List<Listing> result = listingRepository.search(null, 500.0, 600.0, null, null);

        assertTrue(result.isEmpty());
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