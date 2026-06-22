package com.demo.controllers;

import com.demo.dto.RegisterDTO;
import com.demo.model.User;
import com.demo.repositories.UserRepository;
import com.demo.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
public class AuthController {
    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/login")
    public String login(){
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model){
        model.addAttribute("registerDTO", new RegisterDTO());

        return "auth/register";
    }

    @PostMapping("register")
    public String register(@ModelAttribute RegisterDTO user, RedirectAttributes redirectAttributes){
        try {
            userService.register(user);
            User userRegistrado = userRepository.findByEmail(user.getEmail().toLowerCase()).orElseThrow();
            redirectAttributes.addFlashAttribute("message", "Cuenta creada correctamente, inicia sesión.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}
