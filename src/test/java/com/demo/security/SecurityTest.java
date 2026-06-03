package com.demo.security;

import com.demo.model.User;
import com.demo.model.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // 1. PRUEBAS DE RUTAS PÚBLICAS (.permitAll())
    @Test
    void publicEndpointsShouldBeAccessibleAnonymously() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/listings"))
                .andExpect(status().isOk());
    }

    // 2. PRUEBAS DE ACCESO DENEGADO (Error 403)
    @Test
    @WithMockUser(roles = "ADMIN")
    void adminShouldBeForbiddenFromConversations() throws Exception {
        mockMvc.perform(get("/conversation"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userShouldBeForbiddenFromEditingAmenities() throws Exception {
        mockMvc.perform(get("/amenity/new"))
                .andExpect(status().isForbidden());
    }

    // 3. PRUEBAS DE ACCESO PERMITIDO (.hasAnyRole)
    @Test
    void hostShouldBeAllowedInConversations() throws Exception {
        User mockUser = com.demo.model.User.builder()
                .id(1L)
                .name("anfitrion")
                .email("anfitrion@openhouse.com")
                .username("anfitrion5045214")
                .password("password")
                .role(Role.ROLE_HOST)
                .build();

        mockMvc.perform(get("/conversation")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(mockUser)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userShouldBeAllowedInConversations() throws Exception {
        User mockUser = com.demo.model.User.builder()
                .id(1L)
                .name("user")
                .email("user@openhouse.com")
                .username("user5045214")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        mockMvc.perform(get("/conversation")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user(mockUser)))
                .andExpect(status().isOk());
    }

    // 4. PRUEBAS DE REDIRECCIÓN AL LOGIN
    @Test
    void unauthenticatedUserShouldBeRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/bookings"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login")); // <-- Usa 'redirectedUrl' sin patrones
    }
}