package com.demo.controllers;

import com.demo.model.Listing;
import com.demo.model.User;
import com.demo.repositories.ListingRepository;
import com.demo.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional

class ListingControllerTest {

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        listingRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(User.builder().name("Juan Perez").build());
        listingRepository.saveAll(List.of(
                Listing.builder().title("Loft Industrial").isActive(true).owner(user).pricePerNight(110.0).maxGuests(3).build(),
                Listing.builder().title("Casa Rural").isActive(true).pricePerNight(80.0).maxGuests(5).build(),
                Listing.builder().title("Piso en el centro").isActive(true).pricePerNight(95.0).maxGuests(2).build()
        ));
    }

    @Test //Test1 Listado con datos
    void listingsFull() throws Exception {
        mockMvc.perform(get("/listings"))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/listing-list"))
                .andExpect(model().attributeExists("listings"))
                .andExpect(model().attribute("listings", hasSize(3)));
    }
    @Test //Listado vacío
    void listingsEmpty() throws Exception {
        listingRepository.deleteAll();
        mockMvc.perform(get("/listings"))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/listing-list"))
                .andExpect(model().attributeExists("listings"))
                .andExpect(model().attribute("listings", hasSize(0)));
    }

    @Test //Detalle inexistente
    void listingDetailNotFound() throws Exception{
        Long nonExistingId = 9999L;
        mockMvc.perform(get("/listings/" + nonExistingId))
                .andExpect(status().isNotFound());
    }

    @Test //Detalle existente y la vista recibe todos los atributos correctos
    void listingDetailHasCorrectAttributes() throws Exception {
        Listing listing = listingRepository.findAll().getFirst();
        Long id = listing.getId();

        mockMvc.perform(get("/listings/" + listing.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/listing-detail"))
                .andExpect(model().attributeExists("listing"))
                .andExpect(model().attribute("listing", hasProperty("id",is(id))))
                .andExpect(model().attribute("listing", hasProperty("title", is(listing.getTitle()))))
                .andExpect(model().attribute("listing", hasProperty("pricePerNight", is(listing.getPricePerNight()))))
                .andExpect(model().attribute("listing", hasProperty("maxGuests", is(listing.getMaxGuests()))))
                .andExpect(model().attribute("listing", hasProperty("owner", is(listing.getOwner()))))
                .andExpect(model().attribute("listing", hasProperty("isActive", is(listing.getIsActive()))));

    }
}