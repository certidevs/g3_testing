package com.demo.controllers;

import com.demo.model.Amenity;
import com.demo.repositories.AmenityRepository;
import com.demo.repositories.ListingRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AmenityControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AmenityRepository amenityRepository;
    @Autowired private ListingRepository listingRepository;


    @BeforeEach
    void setUp() {

        // crear restaurantes demo
        amenityRepository.deleteAll();
        listingRepository.deleteAll();

        amenityRepository.saveAll(List.of(
                Amenity.builder().name("Piscina").build(),
                Amenity.builder().name("Atención a la Habitación").build(),
                Amenity.builder().name("Restaurant").build()
        ));
        // crear amenities demo si se fueran a usar en varios métodos de test

    }

    @Test
    void amenityFull() throws Exception {
        // invocar endpoint http://localhost:8080/restaurants
        // se lanza una petición HTTP GET al controlador /restaurants
        // y verificamos que devuelve un status 200 OK
        mockMvc.perform(get("/amenity"))
                .andExpect(status().isOk())
                .andExpect(view().name("amenity/amenity-list"))
                .andExpect(model().attributeExists("amenity"))
                .andExpect(model().attribute("amenity", hasSize(3)));
    }
    @Test
    void restaurantsEmpty() throws Exception {

        amenityRepository.deleteAll();

        mockMvc.perform(get("/restaurants"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurants/restaurant-list"))
                .andExpect(model().attributeExists("restaurants"))
                .andExpect(model().attribute("restaurants", hasSize(0)));
    }

    @Test
    void restaurantDetailIsPresentTrue() throws Exception {

        Amenity restaurant = amenityRepository.findAll().getFirst();
        Long restaurantId = restaurant.getId();


        mockMvc.perform(get("/restaurants/" + restaurantId))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurants/restaurant-detail"))
                .andExpect(model().attributeExists("restaurant"))
                .andExpect(model().attributeExists("dishes"))
                .andExpect(model().attribute("restaurant", hasProperty("id", is(restaurantId))))
                .andExpect(model().attribute("restaurant", hasProperty("name", is(restaurant.getName()))));
    }

    @Test
    void restaurantDetailIsPresentFalse()  throws Exception{
        // buscar un restaurante que no exista y comprobar que hace un redirect

        Long idInexistente = 99999L;

        mockMvc.perform(get("/restaurants/" + idInexistente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/restaurants"));
    }






}

