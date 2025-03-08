package com.safetypin.authentication.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testDefaultConstructorDefaults() {
        User user = new User();
        // Verify that default constructor sets all fields to their default values
        assertNull(user.getId(), "Default id should be null");
        assertNull(user.getEmail(), "Default email should be null");
        assertNull(user.getPassword(), "Default password should be null");
        assertNull(user.getName(), "Default name should be null");
        assertFalse(user.isVerified(), "Default isVerified should be false");
        assertNull(user.getRole(), "Default role should be null");
        assertNull(user.getBirthdate(), "Default birthdate should be null");
        assertNull(user.getProvider(), "Default provider should be null");
    }

    @Test
    void testSettersAndGetters() {
        User user = new User();
        UUID id = UUID.randomUUID();
        String email = "test@example.com";
        String password = "secret";
        String name = "Test User";
        boolean verified = true;
        Role role = Role.REGISTERED_USER;
        LocalDate birthdate = LocalDate.of(2000, 1, 1);
        String provider = "GOOGLE";

        user.setId(id);
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setVerified(verified);
        user.setRole(role);
        user.setBirthdate(birthdate);
        user.setProvider(provider);

        assertEquals(id, user.getId());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(name, user.getName());
        assertTrue(user.isVerified());
        assertEquals(role, user.getRole());
        assertEquals(birthdate, user.getBirthdate());
        assertEquals(provider, user.getProvider());
    }

    @Test
    void testParameterizedConstructor() {
        String email = "test2@example.com";
        String password = "password123";
        String name = "Another User";
        boolean verified = false;
        Role role = Role.MODERATOR;
        LocalDate birthdate = LocalDate.of(1995, 5, 15);
        String provider = "EMAIL";

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setVerified(verified);
        user.setRole(role);
        user.setBirthdate(birthdate);
        user.setProvider(provider);


        // id remains null until set (by the persistence layer)
        assertNull(user.getId(), "Id should be null when not set");
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(name, user.getName());
        assertEquals(verified, user.isVerified());
        assertEquals(role, user.getRole());
        assertEquals(birthdate, user.getBirthdate());
        assertEquals(provider, user.getProvider());
    }

    @Test
    void testGenerateUserResponse() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setRole(Role.REGISTERED_USER);

        var response = user.generateUserResponse();
        assertEquals("REGISTERED_USER", response.getRole());
    }

    @Test
    void testRoleEnumValues() {
        User userRegistered = new User();
        User userPremium = new User();
        User userModerator = new User();

        // Test REGISTERED_USER role
        userRegistered.setRole(Role.REGISTERED_USER);
        assertEquals(Role.REGISTERED_USER, userRegistered.getRole());
        assertEquals("REGISTERED_USER", userRegistered.getRole().name());

        // Test PREMIUM_USER role
        userPremium.setRole(Role.PREMIUM_USER);
        assertEquals(Role.PREMIUM_USER, userPremium.getRole());
        assertEquals("PREMIUM_USER", userPremium.getRole().name());

        // Test MODERATOR role
        userModerator.setRole(Role.MODERATOR);
        assertEquals(Role.MODERATOR, userModerator.getRole());
        assertEquals("MODERATOR", userModerator.getRole().name());
    }

    @Test
    void testUserResponseWithDifferentRoles() {
        // Test UserResponse generation with each role
        User registeredUser = new User();
        registeredUser.setRole(Role.REGISTERED_USER);
        assertEquals("REGISTERED_USER", registeredUser.generateUserResponse().getRole());

        User premiumUser = new User();
        premiumUser.setRole(Role.PREMIUM_USER);
        assertEquals("PREMIUM_USER", premiumUser.generateUserResponse().getRole());

        User moderatorUser = new User();
        moderatorUser.setRole(Role.MODERATOR);
        assertEquals("MODERATOR", moderatorUser.generateUserResponse().getRole());
    }

    @Test
    void testUserWithNullRole() {
        // Test case for null role
        User user = new User();
        assertNull(user.getRole());
        assertNull(user.generateUserResponse().getRole());
    }

    @Test
    void testCompleteUserResponseGeneration() {
        // Test UserResponse generation with all fields set
        User user = new User();
        UUID id = UUID.randomUUID();
        user.setId(id);
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setRole(Role.PREMIUM_USER);
        user.setVerified(true);
        user.setBirthdate(LocalDate.of(1990, 1, 1));
        user.setProvider("GOOGLE");

        var response = user.generateUserResponse();
        assertEquals(id, response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getName());
        assertEquals("PREMIUM_USER", response.getRole());
        assertTrue(response.isVerified());
        assertEquals(LocalDate.of(1990, 1, 1), response.getBirthdate());
        assertEquals("GOOGLE", response.getProvider());
    }

    @Test
    void testUserResponseWithNullFields() {
        // Test UserResponse generation with some null fields
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        // Role, birthdate, and provider are null

        var response = user.generateUserResponse();
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getName());
        assertNull(response.getRole());
        assertFalse(response.isVerified());
        assertNull(response.getBirthdate());
        assertNull(response.getProvider());
    }

    @Test
    void testVerificationMethodsSetter() {
        User user = new User();
        assertFalse(user.isVerified());

        user.setVerified(true);
        assertTrue(user.isVerified());

        user.setVerified(false);
        assertFalse(user.isVerified());
    }
}
