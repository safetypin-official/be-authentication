package com.safetypin.authentication.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordResetWithOTPRequestTest {

    @Test
    void testGettersAndSetters() {
        // Arrange
        PasswordResetWithOTPRequest request = new PasswordResetWithOTPRequest();
        String email = "test@example.com";
        String newPassword = "newSecurePassword";

        // Act
        request.setEmail(email);
        request.setNewPassword(newPassword);

        // Assert
        assertEquals(email, request.getEmail());
        assertEquals(newPassword, request.getNewPassword());
    }
}
