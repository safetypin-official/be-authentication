package com.safetypin.authentication.repository;

import com.safetypin.authentication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Create and save a User entity
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setName("Test User");
        user.setRole("USER");
        userRepository.save(user);
    }

    @Test
    void testFindByEmailWhenUserExists() {
        // Retrieve the user by email
        User foundUser = userRepository.findByEmail("test@example.com");
        assertNotNull(foundUser, "Expected to find a user with the given email");
        assertEquals("test@example.com", foundUser.getEmail());
        assertEquals("Test User", foundUser.getName());
    }

    @Test
    void testFindByEmailWhenUserDoesNotExist() {
        // Attempt to find a user that doesn't exist
        User foundUser = userRepository.findByEmail("nonexistent@example.com");
        assertNull(foundUser, "Expected no user to be found for a non-existent email");
    }

}
