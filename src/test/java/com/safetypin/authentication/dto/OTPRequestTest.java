package com.safetypin.authentication.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class OTPRequestTest {

    @Test
    void testGetSetEmail() {
        OTPRequest request = new OTPRequest();
        request.setEmail(" test@example.com ");
        assertEquals("test@example.com", request.getEmail());
    }

    @Test
    void testSetEmail_null() {
        OTPRequest request = new OTPRequest();
        request.setEmail(null);
        assertNull(request.getEmail());
    }

    @Test
    void testSetEmail_empty() {
        OTPRequest request = new OTPRequest();
        request.setEmail("");
        assertEquals("", request.getEmail());
    }

    @Test
    void testSetEmail_whitespaceOnly() {
        OTPRequest request = new OTPRequest();
        request.setEmail("   ");
        assertEquals("", request.getEmail());
    }

    @Test
    void testGetSetOtp() {
        OTPRequest request = new OTPRequest();
        request.setOtp(" 123456 ");
        assertEquals("123456", request.getOtp());
    }

    @Test
    void testSetOtp_null() {
        OTPRequest request = new OTPRequest();
        request.setOtp(null);
        assertNull(request.getOtp());
    }

    @Test
    void testSetOtp_empty() {
        OTPRequest request = new OTPRequest();
        request.setOtp("");
        assertEquals("", request.getOtp());
    }

    @Test
    void testSetOtp_whitespaceOnly() {
        OTPRequest request = new OTPRequest();
        request.setOtp("   ");
        assertEquals("", request.getOtp());
    }
}
