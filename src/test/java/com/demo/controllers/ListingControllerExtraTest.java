package com.demo.controllers;

import com.demo.model.Amenity;
import com.demo.model.Booking;
import com.demo.model.Listing;
import com.demo.model.User;
import com.demo.model.enums.AmenityType;
import com.demo.model.enums.BookingStatus;
import com.demo.model.enums.ListingType;
import com.demo.model.enums.Role;
import com.demo.repositories.AmenityLineRepository;
import com.demo.repositories.AmenityRepository;
import com.demo.repositories.BookingRepository;
import com.demo.repositories.ListingRepository;
import com.demo.repositories.UserRepository;
import com.demo.services.FileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests adicionales de {@link ListingController} centrados en las ramas que el
 * test original no cubría: filtrado por rol (anónimo/USER/HOST/ADMIN), visibilidad
 * de inactivos en el detalle, formularios new/edit y los POST save/toggle/delete.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ListingControllerExtraTest {

    @Autowired
    ListingRepository listingRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AmenityRepository amenityRepository;
    @Autowired
    AmenityLineRepository amenityLineRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    MockMvc mockMvc;

    User admin;
    private final List<Path> uploadedFiles = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        amenityLineRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();

        admin = userRepository.save(User.builder()
                .username("admin").name("Admin Test").email("admin@test.com")
                .role(Role.ROLE_ADMIN).build());

        listingRepository.saveAll(List.of(
                activa("Apartamento con Vistas", ListingType.APARTAMENTO, 150.0, admin),
                activa("Apartamento en el Centro", ListingType.APARTAMENTO, 85.0, admin),
                activa("Casa Rural", ListingType.CASA, 80.0, admin)
        ));
    }

    @AfterEach
    void cleanUploads() throws IOException {
        for (Path p : uploadedFiles) {
            Files.deleteIfExists(p);
        }
    }

    // ───────────────────────── helpers ─────────────────────────

    private Listing activa(String title, ListingType type, double price, User owner) {
        return Listing.builder()
                .title(title).type(type).pricePerNight(price)
                .maxGuests(4).minNights(1).maxNights(30)
                .isActive(true).owner(owner).build();
    }

    private Listing inactiva(String title, User owner) {
        return Listing.builder()
                .title(title).type(ListingType.LOFT).pricePerNight(70.0)
                .maxGuests(2).minNights(1).maxNights(30)
                .isActive(false).owner(owner).build();
    }

    private User saveUser(String username, String email, Role role) {
        return userRepository.save(User.builder()
                .username(username).email(email).password("x").role(role).build());
    }

    private void rememberUpload(String url) {
        String filename = url.substring("/uploads/".length());
        uploadedFiles.add(Paths.get(FileService.UPLOAD_DIR).toAbsolutePath().normalize().resolve(filename));
    }

    // ───────────────────────── list(): validación y roles ─────────────────────────

    @Test
    @DisplayName("list: minPrice > maxPrice marca priceRangeError y no muestra resultados")
    void listPriceRangeErrorReturnsEmpty() throws Exception {
        mockMvc.perform(get("/listings")
                        .param("minPrice", "200")
                        .param("maxPrice", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/listing-list"))
                .andExpect(model().attribute("priceRangeError", is(true)))
                .andExpect(model().attribute("listings", hasSize(0)));
    }

    @Test
    @DisplayName("list anónimo: solo se ven los alojamientos activos")
    void listAnonymousSeesOnlyActive() throws Exception {
        listingRepository.save(inactiva("Inactivo", admin));

        mockMvc.perform(get("/listings"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("listings", hasSize(3)))
                .andExpect(model().attribute("listings", everyItem(hasProperty("isActive", is(true)))));
    }

    @Test
    @DisplayName("list HOST: ve los activos y además sus propios inactivos")
    void listHostSeesOwnInactive() throws Exception {
        User host = saveUser("hostuser", "host2@test.com", Role.ROLE_HOST);
        listingRepository.save(inactiva("Mi inactivo", host));
        listingRepository.save(inactiva("Inactivo ajeno", admin));

        mockMvc.perform(get("/listings").with(user(host)))
                .andExpect(status().isOk())
                // 3 activos + su propio inactivo = 4; el inactivo ajeno no aparece
                .andExpect(model().attribute("listings", hasSize(4)));
    }

    @Test
    @DisplayName("list ADMIN: ve también los inactivos")
    void listAdminSeesInactive() throws Exception {
        listingRepository.save(inactiva("Inactivo", admin));

        mockMvc.perform(get("/listings").with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("listings", hasSize(4)));
    }

    // ───────────────────────── detail(): visibilidad de inactivos ─────────────────────────

    @Test
    @DisplayName("detail: alojamiento inactivo devuelve 404 para anónimo")
    void detailInactiveNotFoundForAnonymous() throws Exception {
        Listing inactivo = listingRepository.save(inactiva("Oculto", admin));

        mockMvc.perform(get("/listings/" + inactivo.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("detail: alojamiento inactivo es visible para ADMIN")
    void detailInactiveVisibleForAdmin() throws Exception {
        User otro = saveUser("otro", "otro@test.com", Role.ROLE_HOST);
        Listing inactivo = listingRepository.save(inactiva("Oculto", otro));

        mockMvc.perform(get("/listings/" + inactivo.getId()).with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/listing-detail"))
                .andExpect(model().attribute("listing", hasProperty("id", is(inactivo.getId()))));
    }

    @Test
    @DisplayName("detail: el dueño HOST ve su propio alojamiento inactivo")
    void detailInactiveVisibleForOwnerHost() throws Exception {
        User host = saveUser("owner", "owner3@test.com", Role.ROLE_HOST);
        Listing inactivo = listingRepository.save(inactiva("Mi pausado", host));

        mockMvc.perform(get("/listings/" + inactivo.getId()).with(user(host)))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/listing-detail"));
    }

    // ───────────────────────── formularios new / edit ─────────────────────────

    @Test
    @DisplayName("GET /listings/new devuelve el formulario con sus atributos")
    void newListingFormHasAttributes() throws Exception {
        User host = saveUser("nuevohost", "nuevo@test.com", Role.ROLE_HOST);

        mockMvc.perform(get("/listings/new").with(user(host)))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/listing-form"))
                .andExpect(model().attributeExists("listing", "types", "cities", "amenities", "selectedAmenityIds"));
    }

    @Test
    @DisplayName("GET /listings/edit/{id} carga el alojamiento existente")
    void editListingFormLoadsExisting() throws Exception {
        Listing existing = listingRepository.findAll().getFirst();

        mockMvc.perform(get("/listings/edit/" + existing.getId()).with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/listing-form"))
                .andExpect(model().attribute("listing", hasProperty("id", is(existing.getId()))))
                .andExpect(model().attributeExists("amenities", "selectedAmenityIds"));
    }

    @Test
    @DisplayName("GET /listings/edit/{id} inexistente devuelve 404")
    void editListingFormNotFound() throws Exception {
        mockMvc.perform(get("/listings/edit/99999").with(user(admin)))
                .andExpect(status().isNotFound());
    }

    // ───────────────────────── saveListing (POST /listings) ─────────────────────────

    @Test
    @DisplayName("POST /listings: un USER que publica es promovido a HOST y queda como dueño")
    void saveListingPromotesUserToHost() throws Exception {
        User normal = saveUser("cliente", "cliente@test.com", Role.ROLE_USER);
        long before = listingRepository.count();

        mockMvc.perform(multipart("/listings")
                        .file(new MockMultipartFile("imageFile", "", "image/png", new byte[0]))
                        .param("title", "Nuevo piso")
                        .param("type", "APARTAMENTO")
                        .param("pricePerNight", "99.0")
                        .param("maxGuests", "3")
                        .param("minNights", "1")
                        .param("maxNights", "10")
                        .with(user(normal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/listings"));

        assertEquals(before + 1, listingRepository.count());
        User reloaded = userRepository.findById(normal.getId()).orElseThrow();
        assertEquals(Role.ROLE_HOST, reloaded.getRole());
    }

    @Test
    @DisplayName("POST /listings con amenities crea las amenity lines correspondientes")
    void saveListingWithAmenitiesCreatesLines() throws Exception {
        User host = saveUser("hostam", "hostam@test.com", Role.ROLE_HOST);
        Amenity wifi = amenityRepository.save(Amenity.builder().name("WiFi").type(AmenityType.WIFI).build());

        mockMvc.perform(multipart("/listings")
                        .file(new MockMultipartFile("imageFile", "", "image/png", new byte[0]))
                        .param("title", "Con wifi")
                        .param("type", "APARTAMENTO")
                        .param("pricePerNight", "120.0")
                        .param("maxGuests", "2")
                        .param("minNights", "1")
                        .param("maxNights", "5")
                        .param("amenityIds", String.valueOf(wifi.getId()))
                        .with(user(host))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/listings"));

        Listing saved = listingRepository.findAll().stream()
                .filter(l -> "Con wifi".equals(l.getTitle()))
                .findFirst().orElseThrow();
        assertEquals(1, amenityLineRepository.findByListingId(saved.getId()).size());
    }

    @Test
    @DisplayName("POST /listings con imagen real guarda la imageUrl bajo /uploads/")
    void saveListingWithImageStoresUrl() throws Exception {
        User host = saveUser("hostimg", "hostimg@test.com", Role.ROLE_HOST);

        mockMvc.perform(multipart("/listings")
                        .file(new MockMultipartFile("imageFile", "foto.png", "image/png", "datos".getBytes()))
                        .param("title", "Con foto")
                        .param("type", "APARTAMENTO")
                        .param("pricePerNight", "120.0")
                        .param("maxGuests", "2")
                        .param("minNights", "1")
                        .param("maxNights", "5")
                        .with(user(host))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Listing saved = listingRepository.findAll().stream()
                .filter(l -> "Con foto".equals(l.getTitle()))
                .findFirst().orElseThrow();
        assertNotNull(saved.getImageUrl());
        rememberUpload(saved.getImageUrl());
        assertTrue(saved.getImageUrl().startsWith("/uploads/"));
    }

    // ───────────────────────── toggle / delete ─────────────────────────

    @Test
    @DisplayName("POST /listings/toggle/{id} cambia el estado activo del alojamiento")
    void toggleListingFlipsActive() throws Exception {
        Listing l = listingRepository.findAll().getFirst();
        boolean before = l.getIsActive();

        mockMvc.perform(post("/listings/toggle/" + l.getId())
                        .with(user(admin))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/listings/" + l.getId()));

        Listing reloaded = listingRepository.findById(l.getId()).orElseThrow();
        assertEquals(!before, reloaded.getIsActive());
    }

    @Test
    @DisplayName("POST /listings/{id}/delete: el ADMIN hace soft-delete")
    void deleteListingAsAdminSoftDeletes() throws Exception {
        Listing l = listingRepository.findAll().getFirst();

        mockMvc.perform(post("/listings/" + l.getId() + "/delete")
                        .with(user(admin))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/listings"));

        assertTrue(listingRepository.findById(l.getId()).orElseThrow().getDeleted());
    }

    @Test
    @DisplayName("POST /listings/{id}/delete: un HOST que no es dueño recibe 403")
    void deleteListingForbiddenForNonOwnerHost() throws Exception {
        User host = saveUser("intruso", "intruso@test.com", Role.ROLE_HOST);
        Listing ajeno = listingRepository.findAll().getFirst(); // dueño = admin

        mockMvc.perform(post("/listings/" + ajeno.getId() + "/delete")
                        .with(user(host))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertFalse(listingRepository.findById(ajeno.getId()).orElseThrow().getDeleted());
    }

    @Test
    @DisplayName("POST /listings/{id}/delete: con reservas activas no borra y muestra mensaje")
    void deleteListingWithActiveBookingsShowsError() throws Exception {
        Listing l = listingRepository.findAll().getFirst();
        bookingRepository.save(Booking.builder()
                .listing(l)
                .status(BookingStatus.CONFIRMED)
                .checkIn(LocalDateTime.now().plusDays(1))
                .checkOut(LocalDateTime.now().plusDays(3))
                .build());

        mockMvc.perform(post("/listings/" + l.getId() + "/delete")
                        .with(user(admin))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/listings/" + l.getId()))
                .andExpect(flash().attributeExists("errorMessage"));

        assertFalse(listingRepository.findById(l.getId()).orElseThrow().getDeleted());

    }
    @Test
    @DisplayName("POST /listings con id existente actualiza el alojamiento sin duplicar y redirige")
    void updateListingModificaExistenteYRedirige() throws Exception {
        Listing existente = listingRepository.findAll().getFirst();
        long total = listingRepository.count();

        mockMvc.perform(multipart("/listings")
                        .file(new MockMultipartFile("imageFile", "", "image/png", new byte[0]))
                        .param("id", String.valueOf(existente.getId()))
                        .param("title", "Apartamento con Vistas (Reformado)")
                        .param("type", existente.getType().name())
                        .param("pricePerNight", "175.0")
                        .param("maxGuests", "4")
                        .param("minNights", "1")
                        .param("maxNights", "30")
                        .with(user(admin))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/listings"));

        // Es una actualizacion: no se crea una fila nueva
        assertEquals(total, listingRepository.count());

        // La fila existente quedo modificada
        Listing actualizado = listingRepository.findById(existente.getId()).orElseThrow();
        assertEquals("Apartamento con Vistas (Reformado)", actualizado.getTitle());
        assertEquals(175.0, actualizado.getPricePerNight(), 0.001);
    }

    @Test
    @DisplayName("detail: el modelo expone listing, amenities e isOwner (relaciones)")
    void detailModelExposesRelations() throws Exception {
        Listing l = listingRepository.findAll().getFirst();

        mockMvc.perform(get("/listings/" + l.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("listing/listing-detail"))
                .andExpect(model().attributeExists("listing", "amenities", "isOwner"));
    }

    @Test
    @DisplayName("detail: el HTML renderizado contiene title, price y guests")
    void detailHtmlContainsKeyData() throws Exception {
        Listing l = listingRepository.save(Listing.builder()
                .title("Atico Vista Mar HTML")
                .type(ListingType.APARTAMENTO)
                .pricePerNight(137.0)
                .maxGuests(19)
                .minNights(2).maxNights(28)
                .isActive(true).owner(admin).build());

        mockMvc.perform(get("/listings/" + l.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Atico Vista Mar HTML"))) // title
                .andExpect(content().string(containsString("137")))                  // price
                .andExpect(content().string(containsString("19")));                  // guests (maxGuests)
    }
}
