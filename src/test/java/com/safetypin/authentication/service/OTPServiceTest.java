package com.safetypin.authentication.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class OTPServiceTest {

    @Test
    void testGenerateOTP() {
        OTPService otpService = new OTPService();
        String email = "user@example.com";
        String otp = otpService.generateOTP(email);
        assertNotNull(otp, "OTP should not be null");
        assertEquals(6, otp.length(), "OTP should be 6 characters long");
        assertTrue(otp.matches("\\d{6}"), "OTP should consist of 6 digits");
    }

    @Test
    void testVerifyOTPSuccess() {
        OTPService otpService = new OTPService();
        String email = "user@example.com";
        String otp = otpService.generateOTP(email);
        // Immediately verify the generated OTP; it should succeed.
        boolean result = otpService.verifyOTP(email, otp);
        assertTrue(result, "The OTP should verify successfully");
    }

    @Test
    void testVerifyOTPWrongOtp() {
        OTPService otpService = new OTPService();
        String email = "user@example.com";
        otpService.generateOTP(email);
        // Try verifying with an incorrect OTP.
        boolean result = otpService.verifyOTP(email, "000000");
        assertFalse(result, "Verification should fail for an incorrect OTP");
    }

    @Test
    void testVerifyOTPExpired() throws Exception {
        OTPService otpService = new OTPService();
        String email = "user@example.com";
        String otp = otpService.generateOTP(email);

        // Access the private otpStorage field via reflection.
        java.lang.reflect.Field otpStorageField = OTPService.class.getDeclaredField("otpStorage");
        otpStorageField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, Object> otpStorage = (ConcurrentHashMap<String, Object>) otpStorageField.get(otpService);

        // Retrieve the current OTPDetails instance.
        Object oldOtpDetails = otpStorage.get(email);
        assertNotNull(oldOtpDetails, "OTPDetails instance should exist");

        // Use reflection to get the private constructor of OTPDetails.
        Class<?> otpDetailsClass = oldOtpDetails.getClass();
        Constructor<?> constructor = otpDetailsClass.getDeclaredConstructor(String.class, LocalDateTime.class);
        constructor.setAccessible(true);
        // Create a new OTPDetails instance with an expired time (3 minutes ago).
        Object expiredOtpDetails = constructor.newInstance(otp, LocalDateTime.now().minusMinutes(3));
        // Replace the old OTPDetails with the expired one.
        otpStorage.put(email, expiredOtpDetails);

        // Now verification should fail because the OTP is expired.
        boolean result = otpService.verifyOTP(email, otp);
        assertFalse(result, "The OTP should be expired and verification should fail");
    }

    @Test
    void testVerifyOTPWhenNotGenerated() {
        OTPService otpService = new OTPService();
        // No OTP was generated for this email, so verification should return false.
        boolean result = otpService.verifyOTP("nonexistent@example.com", "123456");
        assertFalse(result, "Verification should fail when no OTP is generated for the given email");
    }
}
