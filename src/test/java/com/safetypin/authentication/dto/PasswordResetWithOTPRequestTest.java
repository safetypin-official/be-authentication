package com.safetypin.authentication.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PasswordResetWithOTPRequestTest {

    static Stream<Arguments> emailTestCases() {
        return Stream.of(
                Arguments.of(" test@example.com ", "test@example.com"),
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("   ", ""));
    }

    static Stream<Arguments> resetTokenTestCases() {
        return Stream.of(
                Arguments.of(" testToken ", "testToken"),
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("   ", ""));
    }

    @ParameterizedTest
    @MethodSource("emailTestCases")
    void testSetEmail(String input, String expected) {
        PasswordResetWithOTPRequest request = new PasswordResetWithOTPRequest();
        request.setEmail(input);
        if (expected == null) {
            assertNull(request.getEmail());
        } else {
            assertEquals(expected, request.getEmail());
        }
    }

    @Test
    void testGetSetNewPassword() {
        PasswordResetWithOTPRequest request = new PasswordResetWithOTPRequest();
        request.setNewPassword(" newPassword123 ");
        assertEquals(" newPassword123 ", request.getNewPassword());
    }

    @ParameterizedTest
    @MethodSource("resetTokenTestCases")
    void testSetResetToken(String input, String expected) {
        PasswordResetWithOTPRequest request = new PasswordResetWithOTPRequest();
        request.setResetToken(input);
        if (expected == null) {
            assertNull(request.getResetToken());
        } else {
            assertEquals(expected, request.getResetToken());
        }
    }
}
