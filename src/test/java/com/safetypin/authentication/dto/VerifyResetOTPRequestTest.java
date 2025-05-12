package com.safetypin.authentication.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VerifyResetOTPRequestTest {

    @ParameterizedTest
    @MethodSource("emailProvider")
    void testSetAndGetEmail(String input, String expected) {
        VerifyResetOTPRequest request = new VerifyResetOTPRequest();
        request.setEmail(input);

        if (expected == null) {
            assertNull(request.getEmail(), "Expected null for input: " + input);
        } else {
            assertEquals(expected, request.getEmail(), "Trimming didn’t work for input: " + input);
        }
    }

    static Stream<Arguments> emailProvider() {
        return Stream.of(
                // input , expected
                Arguments.of(" test@example.com ", "test@example.com"),
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("   ", ""));
    }

    @ParameterizedTest
    @MethodSource("otpProvider")
    void testSetAndGetOtp(String input, String expected) {
        VerifyResetOTPRequest request = new VerifyResetOTPRequest();
        request.setOtp(input);

        if (expected == null) {
            assertNull(request.getOtp(), "Expected null for input: " + input);
        } else {
            assertEquals(expected, request.getOtp(), "Trimming didn’t work for input: " + input);
        }
    }

    static Stream<Arguments> otpProvider() {
        return Stream.of(
                // input , expected
                Arguments.of(" 123456 ", "123456"),
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("   ", ""));
    }
}
