package com.demo.services;

import com.demo.dto.RegisterDTO;
import com.demo.model.User;
import com.demo.model.enums.Role;
import com.demo.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario con el correo (" + email + ") no encontrado"));
    }

    public User register(RegisterDTO registerDTO){
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya existe");
        } else if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new RuntimeException("El correo electrónico ya existe");
        } else if (!registerDTO.getPassword().equals(registerDTO.getPasswordConfirmed())) {
            throw new RuntimeException("Las contraseñas no coinciden");
        }else if(registerDTO.getPassword().length() < 6) {
            throw new RuntimeException("La contraseña debe tener al menos 6 caracteres");
        } else if (!registerDTO.getAcceptRGPD()) {
            throw new RuntimeException("Debes aceptar la política de privacidad");
        }

        User user = new User();
        user.setName(registerDTO.getName());
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRole(Role.ROLE_USER);
        return userRepository.save(user);
    }
}
