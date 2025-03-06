package com.safetypin.authentication.service;

import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findById(UUID Id) {
        return userRepository.findById(Id);
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
