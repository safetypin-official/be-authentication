package com.safetypin.authentication.service;

import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public List<User> findUsersByNameContaining(String query) {
        return userRepository.findByNameContainingIgnoreCase(query);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Find all users by their IDs
     *
     * @param ids List of user IDs
     * @return List of users
     */
    public List<User> findAllById(List<UUID> ids) {
        return userRepository.findAllById(ids);
    }
}
