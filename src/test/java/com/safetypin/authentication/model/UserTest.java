package com.safetypin.authentication.model;

import com.safetypin.authentication.dto.UserResponse;
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
        String role = "ADMIN";
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
        String role = "USER";
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
        // Setup
        User user = new User();
        UUID id = UUID.randomUUID();
        String email = "test@example.com";
        String password = "secret";  // This shouldn't be in the response
        String name = "Test User";
        boolean verified = true;
        String role = "ADMIN";
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

        // Execute
        UserResponse response = user.generateUserResponse();

        // Verify
        assertEquals(id, response.getId(), "ID should match");
        assertEquals(email, response.getEmail(), "Email should match");
        assertEquals(name, response.getName(), "Name should match");
        assertEquals(verified, response.isVerified(), "Verification status should match");
        assertEquals(role, response.getRole(), "Role should match");
        assertEquals(birthdate, response.getBirthdate(), "Birthdate should match");
        assertEquals(provider, response.getProvider(), "Provider should match");
    }
}
