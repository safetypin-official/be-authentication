package com.safetypin.authentication.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LoginRequestTest {

    private static Stream<Arguments> emailTestCases() {
        return Stream.of(
                Arguments.of(" test@example.com ", "test@example.com"),
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("   ", ""));
    }

    @ParameterizedTest
    @MethodSource("emailTestCases")
    void testEmailHandling(String input, String expected) {
        LoginRequest request = new LoginRequest();
        request.setEmail(input);
        if (expected == null) {
            assertNull(request.getEmail());
        } else {
            assertEquals(expected, request.getEmail());
        }
    }

    @Test
    void testGetSetPassword() {
        LoginRequest request = new LoginRequest();
        request.setPassword("password123");
        assertEquals("password123", request.getPassword());
    }
}
