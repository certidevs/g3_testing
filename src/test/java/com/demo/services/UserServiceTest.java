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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    void registerOK() {
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("moha@moha.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("Pass1wrd@")).thenReturn("encodedPassword");

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("user");
        registerDTO.setEmail("moha@moha.com");
        registerDTO.setPassword("Pass1wrd@");
        registerDTO.setPasswordConfirmed("Pass1wrd@");
        registerDTO.setAcceptRGPD(true);

        User user = userService.register(registerDTO);

        assertNotNull(user);
        assertEquals("moha@moha.com", user.getEmail());
        assertEquals("user", user.getUsername());
        assertEquals("encodedPassword", user.getPassword());
        assertEquals(Role.ROLE_USER, user.getRole());

        verify(userRepository).existsByUsername("user");
        verify(userRepository).existsByEmail("moha@moha.com");
        verify(passwordEncoder).encode("Pass1wrd@");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loadUserByUsernameOK() {
        User user = new User();
        user.setUsername("user");
        user.setEmail("moha@moha.com");
        user.setRole(Role.ROLE_USER);

        when(userRepository.findByEmail("moha@moha.com")).thenReturn(Optional.of(user));

        UserDetails userDB = userService.loadUserByUsername("moha@moha.com");

        assertNotNull(userDB);
        assertEquals("user", userDB.getUsername());
        verify(userRepository).findByEmail("moha@moha.com");
    }

    @Test
    void loadUserByUsernameException() {
        when(userRepository.findByEmail("ko@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("ko@test.com"));

        verify(userRepository).findByEmail("ko@test.com");
    }

    @Test
    void registerUsernameNotAvailable() {
        when(userRepository.existsByUsername("ocupado")).thenReturn(true);

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("ocupado");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.register(registerDTO));

        assertEquals("El nombre de usuario ya existe", exception.getMessage());

        verify(userRepository).existsByUsername("ocupado");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void registerEmailNotAvailable() {
        when(userRepository.existsByUsername("libre")).thenReturn(false);
        when(userRepository.existsByEmail("ocupado@moha.com")).thenReturn(true);

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("libre");
        registerDTO.setEmail("ocupado@moha.com");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.register(registerDTO));

        assertEquals("El correo electrónico ya existe", exception.getMessage());

        verify(userRepository).existsByUsername("libre");
        verify(userRepository).existsByEmail("ocupado@moha.com");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void registerPasswordNotMatch() {
        when(userRepository.existsByUsername("libre")).thenReturn(false);
        when(userRepository.existsByEmail("libre@moha.com")).thenReturn(false);

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("libre");
        registerDTO.setEmail("libre@moha.com");
        registerDTO.setPassword("123456");
        registerDTO.setPasswordConfirmed("654321");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.register(registerDTO));

        assertEquals("Las contraseñas no coinciden", exception.getMessage());

        verify(userRepository).existsByUsername("libre");
        verify(userRepository).existsByEmail("libre@moha.com");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void registerRgpdNotAccepted() {
        when(userRepository.existsByUsername("libre")).thenReturn(false);
        when(userRepository.existsByEmail("libre@moha.com")).thenReturn(false);

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("libre");
        registerDTO.setEmail("libre@moha.com");
        registerDTO.setPassword("Pass1wrd@");
        registerDTO.setPasswordConfirmed("Pass1wrd@");
        registerDTO.setAcceptRGPD(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.register(registerDTO));

        assertEquals("Debes aceptar la política de privacidad", exception.getMessage());

        verify(userRepository).existsByUsername("libre");
        verify(userRepository).existsByEmail("libre@moha.com");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }
}