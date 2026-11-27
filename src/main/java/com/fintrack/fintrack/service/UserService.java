package com.fintrack.fintrack.service;

import com.fintrack.fintrack.dto.RegistrationForm;
import com.fintrack.fintrack.model.User;
import com.fintrack.fintrack.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegistrationForm form) {

        User user = new User();

        user.setName(form.getName().trim());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(form.getPassword()));

        return userRepository.save(user);
    }
}
