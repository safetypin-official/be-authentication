package com.safetypin.authentication.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PasswordResetRequestTest {

    @Test
    void testGettersAndSetters() {
        // Arrange
        PasswordResetRequest request = new PasswordResetRequest();
        String email = "test@example.com";

        // Act
        request.setEmail(email);

        // Assert
        assertEquals(email, request.getEmail());
    }
}
