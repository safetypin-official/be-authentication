package com.safetypin.authentication.dto;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PasswordResetRequestTest {

    private static Stream<Arguments> emailTestCases() {
        return Stream.of(
                Arguments.of(" test@example.com ", "test@example.com"),
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("   ", ""));
    }

    @ParameterizedTest
    @MethodSource("emailTestCases")
    void testEmailHandling(String inputEmail, String expectedResult) {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail(inputEmail);

        if (inputEmail == null) {
            assertNull(request.getEmail());
        } else {
            assertEquals(expectedResult, request.getEmail());
        }
    }
}
