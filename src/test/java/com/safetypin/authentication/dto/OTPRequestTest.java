package com.safetypin.authentication.dto;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OTPRequestTest {

    static Stream<Arguments> emailTestCases() {
        return Stream.of(
                Arguments.of(" test@example.com "),
                Arguments.of(""),
                Arguments.of("   "));
    }

    static Stream<Arguments> otpTestCases() {
        return Stream.of(
                Arguments.of(" 123456 "),
                Arguments.of(""),
                Arguments.of("   "));
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("emailTestCases")
    void testSetEmail(String input) {
        OTPRequest request = new OTPRequest();
        request.setEmail(input);

        if (input == null) {
            assertNull(request.getEmail());
        } else if (input.trim().isEmpty()) {
            assertEquals("", request.getEmail());
        } else {
            assertEquals(input.trim(), request.getEmail());
        }
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("otpTestCases")
    void testSetOtp(String input) {
        OTPRequest request = new OTPRequest();
        request.setOtp(input);

        if (input == null) {
            assertNull(request.getOtp());
        } else if (input.trim().isEmpty()) {
            assertEquals("", request.getOtp());
        } else {
            assertEquals(input.trim(), request.getOtp());
        }
    }
}
