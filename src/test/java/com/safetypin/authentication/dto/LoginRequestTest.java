package com.safetypin.authentication.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class LoginRequestTest {

    @Test
    void testGetSetEmail() {
        LoginRequest request = new LoginRequest();
        request.setEmail(" test@example.com ");
        assertEquals("test@example.com", request.getEmail());
    }

    @Test
    void testSetEmail_null() {
        LoginRequest request = new LoginRequest();
        request.setEmail(null);
        assertNull(request.getEmail());
    }

    @Test
    void testGetSetPassword() {
        LoginRequest request = new LoginRequest();
        request.setPassword("password123");
        assertEquals("password123", request.getPassword());
    }

    @Test
    void testSetEmail_empty() {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        assertEquals("", request.getEmail());
    }

    @Test
    void testSetEmail_whitespaceOnly() {
        LoginRequest request = new LoginRequest();
        request.setEmail("   ");
        assertEquals("", request.getEmail());
    }
}
