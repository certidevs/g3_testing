package com.demo.controllers;

import com.demo.model.Listing;
import com.demo.model.User;
import com.demo.model.Amenity;
import com.demo.model.AmenityLine;
import com.demo.model.enums.ListingType;
import com.demo.model.enums.AmenityType;
import com.demo.model.enums.Role;
import com.demo.repositories.ListingRepository;
import com.demo.repositories.UserRepository;
import com.demo.repositories.AmenityRepository;
import com.demo.repositories.AmenityLineRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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
    private AmenityRepository amenityRepository;

    @Autowired
    private AmenityLineRepository amenityLineRepository;

    @Autowired
    private MockMvc mockMvc;

    User user;
     Authentication anonymousAuth;

    @BeforeEach
    void setUp() {
        listingRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder().username("pepe").name("Host Test").email("host@test.com").role(Role.ROLE_ADMIN).build());
        listingRepository.saveAll(List.of(
                Listing.builder().title("Apartamento con Vistas").type(ListingType.APARTAMENTO).pricePerNight(150.0).maxGuests(4).minNights(1).maxNights(30).isActive(true).owner(user).build(),
                Listing.builder().title("Apartamento en el Centro").type(ListingType.APARTAMENTO).pricePerNight(85.0).maxGuests(2).minNights(1).maxNights(30).isActive(true).owner(user).build(),
                Listing.builder().title("Casa Rural").type(ListingType.CASA).pricePerNight(80.0).maxGuests(5).minNights(2).maxNights(30).isActive(true).owner(user).build()
        ));

        anonymousAuth = new AnonymousAuthenticationToken(
                "anonymous-key",
                "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
        );
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
        mockMvc.perform(get("/listings")
//                        .with(user(user))
                )
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

    @Test
    void listingsWithoutFilterSelected() throws Exception {
        mockMvc.perform(get("/listings"))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/listing-list"))
                .andExpect(model().attributeExists("listings", "types"))
                .andExpect(model().attribute("selectedType", nullValue()))
                .andExpect(model().attribute("selectedMinPrice", nullValue()))
                .andExpect(model().attribute("selectedMaxPrice", nullValue()))
                .andExpect(model().attribute("selectedGuests", nullValue()))
                .andExpect(model().attribute("selectedNights", nullValue()));
    }

    @Test
    void listingsWithFiltersSelected() throws Exception {
        mockMvc.perform(get("/listings")
                        .param("type", "LOFT")
                        .param("minPrice", "50")
                        .param("maxPrice", "200")
                        .param("guests", "3")
                        .param("nights", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/listing-list"))
                .andExpect(model().attribute("selectedType", ListingType.LOFT))
                .andExpect(model().attribute("selectedMinPrice", 50.0))
                .andExpect(model().attribute("selectedMaxPrice", 200.0))
                .andExpect(model().attribute("selectedGuests", 3))
                .andExpect(model().attribute("selectedNights", 2));
    }

    @Test
    void listingsFilteredByMinPrice_realFiltering() throws Exception{
        mockMvc.perform(get("/listings")
                .param("minPrice","100"))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/listing-list"))
                .andExpect(model().attribute("selectedMinPrice",100.0))
                .andExpect(model().attribute("listings", hasSize(1)))
                .andExpect(model().attribute("listings", everyItem(hasProperty("pricePerNight", greaterThanOrEqualTo(100.0)))));
    }

    @Test
    void listingsGuestsZeroIsTreatedAsNull() throws Exception {
        mockMvc.perform(get("/listings")
                        .param("guests", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/listing-list"))
                .andExpect(model().attribute("selectedGuests", nullValue()))
                .andExpect(model().attribute("listings", hasSize(3))); // no filtra nada
    }

    @Test
    void listingsNightsZeroIsTreatedAsNull() throws Exception {
        mockMvc.perform(get("/listings")
                        .param("nights", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/listing-list"))
                .andExpect(model().attribute("selectedNights", nullValue()))
                .andExpect(model().attribute("listings", hasSize(3))); // no filtra nada
    }

    @Test
    void listingsFilteredByCombinedFilters() throws Exception {

        mockMvc.perform(get("/listings")
                        .param("type", "APARTAMENTO")
                        .param("minPrice", "80")
                        .param("maxPrice", "200")
                        .param("guests", "2")
                        .param("nights", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/listing-list"))
                .andExpect(model().attribute("selectedType", ListingType.APARTAMENTO))
                .andExpect(model().attribute("selectedMinPrice", 80.0))
                .andExpect(model().attribute("selectedMaxPrice", 200.0))
                .andExpect(model().attribute("selectedGuests", 2))
                .andExpect(model().attribute("selectedNights", 1))
                .andExpect(model().attribute("listings", hasSize(2)))
                .andExpect(model().attribute("listings", everyItem(allOf(
                        hasProperty("type", is(ListingType.APARTAMENTO)),
                        hasProperty("pricePerNight", allOf(
                                greaterThanOrEqualTo(80.0),
                                lessThanOrEqualTo(200.0)
                        )),
                        hasProperty("maxGuests", greaterThanOrEqualTo(2)),
                        hasProperty("minNights", lessThanOrEqualTo(1)),
                        hasProperty("maxNights", greaterThanOrEqualTo(1))
                ))));
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

    @DisplayName("Detalle de listing inactivo muestra aviso de pausa (owner)")
    @Test
    void listingDetailInactiveReturnsOkWithPauseWarning() throws Exception {

        // 1. Guardamos el owner real en la BD
        User owner = userRepository.save(
                User.builder()
                        .email("owner@mail.com")
                        .password("1234")
                        .role(Role.ROLE_HOST)
                        .build()
        );

        // 2. Guardamos el listing inactivo
        Listing inactive = listingRepository.save(
                Listing.builder()
                        .title("Apartamento Pausado")
                        .type(ListingType.APARTAMENTO)
                        .pricePerNight(99.0)
                        .maxGuests(2)
                        .minNights(1)
                        .maxNights(10)
                        .isActive(false)
                        .owner(owner)
                        .build()
        );

        // 3. Autenticamos al owner REAL como principal
        Authentication auth = new UsernamePasswordAuthenticationToken(
                owner,
                owner.getPassword(),
                owner.getAuthorities()
        );

        mockMvc.perform(get("/listings/" + inactive.getId()).with(user(user)))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/listing-detail"))
                .andExpect(model().attribute("listing", hasProperty("isActive", is(false))))
                .andExpect(content().string(containsString("pausado temporalmente")));
    }

}