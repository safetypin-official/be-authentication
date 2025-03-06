package com.safetypin.authentication.repository;

import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Create and save users with different roles
        User registeredUser = new User();
        registeredUser.setEmail("registered@example.com");
        registeredUser.setPassword("password");
        registeredUser.setName("Registered User");
        registeredUser.setRole(Role.REGISTERED_USER);
        userRepository.save(registeredUser);
        
        User premiumUser = new User();
        premiumUser.setEmail("premium@example.com");
        premiumUser.setPassword("password");
        premiumUser.setName("Premium User");
        premiumUser.setRole(Role.PREMIUM_USER);
        userRepository.save(premiumUser);
        
        // No moderator user yet - we'll test for missing role
    }

    @Test
    void testFindByEmailWhenUserExists() {
        // Retrieve the user by email
        User foundUser = userRepository.findByEmail("registered@example.com");
        assertNotNull(foundUser, "Expected to find a user with the given email");
        assertEquals("registered@example.com", foundUser.getEmail());
        assertEquals("Registered User", foundUser.getName());
        assertEquals(Role.REGISTERED_USER, foundUser.getRole());
    }

    @Test
    void testFindByEmailWhenUserDoesNotExist() {
        // Attempt to find a user that doesn't exist
        User foundUser = userRepository.findByEmail("nonexistent@example.com");
        assertNull(foundUser, "Expected no user to be found for a non-existent email");
    }
    
    @Test
    void testFindByRolesExisting() {
        // Test finding users by different existing roles
        User registeredUser = userRepository.findByRole(Role.REGISTERED_USER);
        assertNotNull(registeredUser);
        assertEquals("registered@example.com", registeredUser.getEmail());
        
        User premiumUser = userRepository.findByRole(Role.PREMIUM_USER);
        assertNotNull(premiumUser);
        assertEquals("premium@example.com", premiumUser.getEmail());
    }
    
    @Test
    void testFindByRoleNonExistent() {
        // Test finding by role that no user has
        User moderator = userRepository.findByRole(Role.MODERATOR);
        assertNull(moderator, "No user should be found with MODERATOR role");
    }
    
    @Test
    void testFindAll() {
        // Test finding all users
        List<User> users = userRepository.findAll();
        assertEquals(2, users.size());
    }
}
