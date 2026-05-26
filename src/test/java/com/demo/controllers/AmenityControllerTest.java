package com.demo.controllers;

import com.demo.model.Amenity;
import com.demo.repositories.AmenityRepository;
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
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class AmenityControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AmenityRepository amenityRepository;


    @BeforeEach
    void setUp() {

        amenityRepository.deleteAll();

        amenityRepository.saveAll(List.of(
                Amenity.builder().name("Piscina").build(),
                Amenity.builder().name("Atención a la Habitación").build(),
                Amenity.builder().name("Restaurant").build()
        ));

    }

    @Test
    void amenityFull() throws Exception {
        mockMvc.perform(get("/amenity"))
                .andExpect(status().isOk())
                .andExpect(view().name("amenity/amenity-list"))
                .andExpect(model().attributeExists("amenities"))
                .andExpect(model().attribute("amenities", hasSize(3)));
    }
    @Test
    void amenitiesEmpty() throws Exception {

        amenityRepository.deleteAll();

        mockMvc.perform(get("/amenity"))
                .andExpect(status().isOk())
                .andExpect(view().name("amenity/amenity-list"))
                .andExpect(model().attributeExists("amenities"))
                .andExpect(model().attribute("amenities", hasSize(0)));
    }

    @Test
    void amenityDetailIsPresentTrue() throws Exception {

        Amenity amenity = amenityRepository.findAll().getFirst();
        Long amenityId = amenity.getId();

        mockMvc.perform(get("/amenity/" + amenity.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("amenity/amenity-detail"))
                .andExpect(model().attributeExists("amenity"))
                .andExpect(model().attribute("amenity", hasProperty("id", is(amenityId))))
                .andExpect(model().attribute("amenity", hasProperty("name", is(amenity.getName()))));

    }

}

