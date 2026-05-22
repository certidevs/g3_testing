package com.demo.services;

import com.demo.dto.RegisterDTO;
import com.demo.model.User;
import com.demo.model.enums.Role;
import com.demo.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    void registerOK(){
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("moha@moha.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");;


        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("user");
        registerDTO.setEmail("moha@moha.com");
        registerDTO.setPassword("123456");
        registerDTO.setPasswordConfirmed("123456");
        registerDTO.setAcceptRGPD(true);

        User user = userService.register(registerDTO);

        assertNotNull(user);
        assertEquals("moha@moha.com", user.getEmail());
        assertEquals("user", user.getUsername());
        assertEquals("encodedPassword", user.getPassword());
        assertEquals(Role.ROLE_USER, user.getRole());



    }
}
