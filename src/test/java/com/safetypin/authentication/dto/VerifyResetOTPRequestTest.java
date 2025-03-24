package com.safetypin.authentication.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VerifyResetOTPRequestTest {

    @Test
    void testGettersAndSetters() {
        // Arrange
        VerifyResetOTPRequest request = new VerifyResetOTPRequest();
        String email = "test@example.com";
        String otp = "123456";

        // Act
        request.setEmail(email);
        request.setOtp(otp);

        // Assert
        assertEquals(email, request.getEmail());
        assertEquals(otp, request.getOtp());
    }
}
