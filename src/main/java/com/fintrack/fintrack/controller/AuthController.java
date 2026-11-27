package com.fintrack.fintrack.controller;

import com.fintrack.fintrack.dto.RegistrationForm;
import com.fintrack.fintrack.repository.UserRepository;
import com.fintrack.fintrack.service.UserService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;

    public AuthController(
            UserService userService,
            UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("form") RegistrationForm form,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "register";
        }

        String email = form.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            result.rejectValue(
                    "email",
                    "duplicate",
                    "Email sudah digunakan. Silakan gunakan email lain."
            );
            return "register";
        }

        userService.register(form);

        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

}
