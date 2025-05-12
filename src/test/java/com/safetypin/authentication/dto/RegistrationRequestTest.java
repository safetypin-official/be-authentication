package com.safetypin.authentication.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class RegistrationRequestTest {

    @Test
    void testGettersAndSetters() {
        // Arrange
        RegistrationRequest request = new RegistrationRequest();
        String email = "test@example.com";
        String password = "Password123";
        String name = "Test User";
        LocalDate birthdate = LocalDate.of(1990, 1, 1);

        // Act
        request.setEmail(email);
        request.setPassword(password);
        request.setName(name);
        request.setBirthdate(birthdate);

        // Assert
        assertEquals(email, request.getEmail());
        assertEquals(password, request.getPassword());
        assertEquals(name, request.getName());
        assertEquals(birthdate, request.getBirthdate());
    }

    @Test
    void testGetSetEmail() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail(" test@example.com ");
        assertEquals("test@example.com", request.getEmail());
    }

    @Test
    void testSetEmail_null() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail(null);
        assertNull(request.getEmail());
    }

    @Test
    void testSetEmail_empty() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("");
        // Assuming @NotBlank will be validated elsewhere, focusing on trim
        assertEquals("", request.getEmail());
    }

    @Test
    void testSetEmail_whitespaceOnly() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("   ");
        // Assuming @NotBlank will be validated elsewhere, focusing on trim
        assertEquals("", request.getEmail());
    }

    @Test
    void testGetSetPassword() {
        RegistrationRequest request = new RegistrationRequest();
        request.setPassword("password123");
        assertEquals("password123", request.getPassword());
    }

    @Test
    void testGetSetName() {
        RegistrationRequest request = new RegistrationRequest();
        request.setName(" Test Name ");
        assertEquals("Test Name", request.getName());
    }

    @Test
    void testSetName_null() {
        RegistrationRequest request = new RegistrationRequest();
        request.setName(null);
        assertNull(request.getName());
    }

    @Test
    void testSetName_empty() {
        RegistrationRequest request = new RegistrationRequest();
        request.setName("");
        // Assuming @NotBlank will be validated elsewhere, focusing on trim
        assertEquals("", request.getName());
    }

    @Test
    void testSetName_whitespaceOnly() {
        RegistrationRequest request = new RegistrationRequest();
        request.setName("   ");
        // Assuming @NotBlank will be validated elsewhere, focusing on trim
        assertEquals("", request.getName());
    }

    @Test
    void testGetSetBirthdate() {
        RegistrationRequest request = new RegistrationRequest();
        LocalDate birthdate = LocalDate.now();
        request.setBirthdate(birthdate);
        assertEquals(birthdate, request.getBirthdate());
    }
}
