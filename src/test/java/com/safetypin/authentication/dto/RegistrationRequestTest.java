package com.safetypin.authentication.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

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
}
