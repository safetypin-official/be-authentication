package com.safetypin.authentication.service;

import com.safetypin.authentication.exception.OTPException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OTPServiceTest {
    @InjectMocks
    private OTPService otpService;

    @Mock
    private EmailService emailService;

    /**
     * Helper method to generate an OTP different from the input
     */
    private String generateDifferentOTP(String originalOTP) {
        if (originalOTP.equals("000000")) {
            return "000001";
        } else {
            return "000000";
        }
    }

    @Test
    void testGenerateOTP() {
        when(emailService.sendOTPMail(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(true));

        String email = "user@example.com";
        String generatedOTP = otpService.generateOTP(email);

        assertNotNull(generatedOTP, "Generated OTP should not be null");
        assertEquals(6, generatedOTP.length(), "OTP should be 6 digits long");
    }

    @Test
    void testVerifyOTPWrongOtp() {
        when(emailService.sendOTPMail(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(true));

        String email = "user@example.com";
        String generatedOTP = otpService.generateOTP(email);

        // Generate a different OTP guaranteed to be different from the one generated
        String wrongOTP = generateDifferentOTP(generatedOTP);

        boolean result = otpService.verifyOTP(email, wrongOTP);
        assertFalse(result, "Verification should fail for an incorrect OTP");
    }

    @Test
    void testMultipleOTPGenerations() {
        when(emailService.sendOTPMail(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(true));

        String email = "user@example.com";
        String firstOTP = otpService.generateOTP(email);
        String secondOTP = otpService.generateOTP(email);

        assertNotEquals(firstOTP, secondOTP, "Generated OTPs should be different");
    }

    @Test
    void testVerifyOTPAfterSecondGeneration() {
        when(emailService.sendOTPMail(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(true));

        String email = "user@example.com";
        String firstOTP = otpService.generateOTP(email);
        String secondOTP = otpService.generateOTP(email);

        boolean result = otpService.verifyOTP(email, firstOTP);
        assertFalse(result, "First OTP should not verify after second generation");

        boolean secondResult = otpService.verifyOTP(email, secondOTP);
        assertTrue(secondResult, "Latest OTP should verify successfully");
    }

    @Test
    void testVerifyOTPMultipleTimes() {
        when(emailService.sendOTPMail(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(true));

        String email = "user@example.com";
        String otp = otpService.generateOTP(email);

        boolean firstTry = otpService.verifyOTP(email, otp);
        assertTrue(firstTry, "First verification should succeed");

        boolean secondTry = otpService.verifyOTP(email, otp);
        assertFalse(secondTry, "Second verification should fail as OTP should be consumed");
    }

    @Test
    void testNullParameters() {
        assertThrows(NullPointerException.class, () -> {
            otpService.verifyOTP(null, "123456");
        }, "Should throw exception when email is null");

        assertThrows(NullPointerException.class, () -> {
            otpService.verifyOTP("user@example.com", null);
        }, "Should throw exception when OTP is null");
    }

    @Test
    void testOTPExpiration() throws Exception {
        when(emailService.sendOTPMail(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(true));

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
        // No OTP was generated for this email, so verification should return false.
        boolean result = otpService.verifyOTP("nonexistent@example.com", "123456");
        assertFalse(result, "Verification should fail when no OTP is generated for the given email");
    }

    @Test
    void testGenerateOTPEmailServiceReturnsFalse() {
        // Mock email service to return false
        when(emailService.sendOTPMail(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(false));

        String email = "user@example.com";

        // Verify that OTPException is thrown
        OTPException exception = assertThrows(OTPException.class, () -> {
            otpService.generateOTP(email);
        }, "Should throw OTPException when email service returns false");

        assertEquals("Failed to send OTP", exception.getMessage());
    }

    @Test
    void testGenerateOTPInterruptedException() {
        // Mock email service to throw InterruptedException
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        future.completeExceptionally(new InterruptedException("Test interrupted"));
        when(emailService.sendOTPMail(anyString(), anyString())).thenReturn(future);

        String email = "user@example.com";

        // Verify that OTPException is thrown
        OTPException exception = assertThrows(OTPException.class, () -> {
            otpService.generateOTP(email);
        }, "Should throw OTPException when InterruptedException occurs");

        assertTrue(exception.getMessage().contains("Failed to send OTP"));

        // Verify that thread was interrupted
        assertTrue(Thread.currentThread().isInterrupted(), "Thread should be interrupted");

        // Clear the interrupted status for other tests
        Thread.interrupted();
    }

    @Test
    void testGenerateOTPExecutionException() {
        // Mock email service to throw ExecutionException
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        future.completeExceptionally(new ExecutionException("Test execution failed", new RuntimeException("Email service error")));
        when(emailService.sendOTPMail(anyString(), anyString())).thenReturn(future);

        String email = "user@example.com";

        // Verify that OTPException is thrown
        OTPException exception = assertThrows(OTPException.class, () -> {
            otpService.generateOTP(email);
        }, "Should throw OTPException when ExecutionException occurs");

        assertTrue(exception.getMessage().contains("Failed to send OTP"));
    }

    @Test
    void testGenerateResetToken() {
        String email = "test@example.com";
        String resetToken = otpService.generateResetToken(email);

        assertNotNull(resetToken, "Generated reset token should not be null");
        assertFalse(resetToken.isEmpty(), "Reset token should not be empty");
    }

    @Test
    void testVerifyResetToken_Success() throws Exception {
        String email = "test@example.com";
        String resetToken = otpService.generateResetToken(email);

        boolean isValid = otpService.verifyResetToken(resetToken, email);
        assertTrue(isValid, "Reset token should be valid");

        // Verify that the token has been removed after verification
        Field resetTokenStorageField = OTPService.class.getDeclaredField("resetTokenStorage");
        resetTokenStorageField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, Object> resetTokenStorage =
                (ConcurrentHashMap<String, Object>) resetTokenStorageField.get(otpService);

        assertFalse(resetTokenStorage.containsKey(resetToken), "Token should be removed after verification");
    }

    @Test
    void testVerifyResetToken_WrongEmail() {
        String email = "test@example.com";
        String resetToken = otpService.generateResetToken(email);

        boolean isValid = otpService.verifyResetToken(resetToken, "wrong@example.com");
        assertFalse(isValid, "Reset token should not be valid for different email");
    }

    @Test
    void testVerifyResetToken_Expired() throws Exception {
        String email = "test@example.com";
        String resetToken = otpService.generateResetToken(email);

        // Access the private resetTokenStorage field via reflection
        Field resetTokenStorageField = OTPService.class.getDeclaredField("resetTokenStorage");
        resetTokenStorageField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, Object> resetTokenStorage =
                (ConcurrentHashMap<String, Object>) resetTokenStorageField.get(otpService);

        // Get the ResetTokenDetails class
        Object tokenDetails = resetTokenStorage.get(resetToken);
        assertNotNull(tokenDetails, "Token details should exist");

        // Find the class and constructor of ResetTokenDetails
        Class<?> resetTokenDetailsClass = tokenDetails.getClass();
        Constructor<?> constructor = resetTokenDetailsClass.getDeclaredConstructor(String.class, LocalDateTime.class);
        constructor.setAccessible(true);

        // Create a new ResetTokenDetails instance with an expired time (4 minutes ago)
        Object expiredTokenDetails = constructor.newInstance(email, LocalDateTime.now().minusMinutes(4));

        // Replace the original token details with the expired one
        resetTokenStorage.put(resetToken, expiredTokenDetails);

        // Verify that the token is now expired
        boolean isValid = otpService.verifyResetToken(resetToken, email);
        assertFalse(isValid, "Reset token should be expired");
    }

    @Test
    void testVerifyResetToken_NotFound() {
        String nonExistentToken = "non-existent-token";
        String email = "test@example.com";

        boolean isValid = otpService.verifyResetToken(nonExistentToken, email);
        assertFalse(isValid, "Verification should fail for non-existent token");
    }
}