package com.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http.authorizeHttpRequests(auth -> auth
                //Listings
                .requestMatchers("/", "/register", "/error", "/login", "/css/**", "/webjars/**", "/images/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/listings", "/listings/{id:\\d+}").permitAll()
                .requestMatchers("/listings/edit/**", "/listings/toggle/**").hasAnyRole("ADMIN", "HOST")
                .requestMatchers("/listings/new").hasAnyRole("ADMIN", "HOST", "USER")
                .requestMatchers(HttpMethod.POST, "/listings").hasAnyRole("ADMIN", "HOST", "USER")

                //Bookings
                .requestMatchers("/bookings/**").authenticated()

                //Reviews
                .requestMatchers(HttpMethod.GET, "/reviews", "/reviews/{id:\\d+}", "/listing/{listingId:\\d+}/reviews").permitAll()
                .requestMatchers("/reviews/delete/**").hasAnyRole("ADMIN", "HOST","USER")
                .requestMatchers("/reviews/new/**").authenticated()

                //Amenities
                .requestMatchers(HttpMethod.GET, "/amenity", "/amenity/{id:\\d+}").permitAll()
                .requestMatchers("/amenity/new", "/amenity/edit/**", "/amenity/delete/**").hasAnyRole("ADMIN", "HOST")
                .requestMatchers(HttpMethod.POST, "/amenity").hasAnyRole("ADMIN", "HOST")

                //Conversation
                .requestMatchers("/conversation/**").authenticated()

                .anyRequest().authenticated()
        );

        http.formLogin(
                form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/listings")
                        .permitAll()
        );

        return http.build();
    }
}