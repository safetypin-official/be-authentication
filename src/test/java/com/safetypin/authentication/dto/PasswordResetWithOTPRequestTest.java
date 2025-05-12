package com.safetypin.authentication.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class PasswordResetWithOTPRequestTest {

    @Test
    void testGetSetEmail() {
        PasswordResetWithOTPRequest request = new PasswordResetWithOTPRequest();
        request.setEmail(" test@example.com ");
        assertEquals("test@example.com", request.getEmail());
    }

    @Test
    void testSetEmail_null() {
        PasswordResetWithOTPRequest request = new PasswordResetWithOTPRequest();
        request.setEmail(null);
        assertNull(request.getEmail());
    }

    @Test
    void testSetEmail_empty() {
        PasswordResetWithOTPRequest request = new PasswordResetWithOTPRequest();
        request.setEmail("");
        // Assuming @NotBlank will be validated elsewhere, focusing on trim
        assertEquals("", request.getEmail());
    }

    @Test
    void testSetEmail_whitespaceOnly() {
        PasswordResetWithOTPRequest request = new PasswordResetWithOTPRequest();
        request.setEmail("   ");
        // Assuming @NotBlank will be validated elsewhere, focusing on trim
        assertEquals("", request.getEmail());
    }

    @Test
    void testGetSetNewPassword() {
        PasswordResetWithOTPRequest request = new PasswordResetWithOTPRequest();
        request.setNewPassword(" newPassword123 ");
        // Assuming no trim on password, but if there was:
        // assertEquals("newPassword123", request.getNewPassword());
        assertEquals(" newPassword123 ", request.getNewPassword());
    }

    @Test
    void testGetSetResetToken() {
        PasswordResetWithOTPRequest request = new PasswordResetWithOTPRequest();
        request.setResetToken(" testToken ");
        assertEquals("testToken", request.getResetToken());
    }

    @Test
    void testSetResetToken_null() {
        PasswordResetWithOTPRequest request = new PasswordResetWithOTPRequest();
        request.setResetToken(null);
        assertNull(request.getResetToken());
    }

    @Test
    void testSetResetToken_empty() {
        PasswordResetWithOTPRequest request = new PasswordResetWithOTPRequest();
        request.setResetToken("");
        // Assuming @NotBlank will be validated elsewhere, focusing on trim
        assertEquals("", request.getResetToken());
    }

    @Test
    void testSetResetToken_whitespaceOnly() {
        PasswordResetWithOTPRequest request = new PasswordResetWithOTPRequest();
        request.setResetToken("   ");
        // Assuming @NotBlank will be validated elsewhere, focusing on trim
        assertEquals("", request.getResetToken());
    }
}
