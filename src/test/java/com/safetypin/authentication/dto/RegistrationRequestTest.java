package com.safetypin.authentication.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RegistrationRequestTest {

    @Test
    void testGettersAndSetters() {
        // Arrange
        RegistrationRequest request = new RegistrationRequest();
        String email = "test@example.com";
        String password = "Password123";
        String name = "Test User";
        LocalDate birthdate = LocalDate.of(1990, 1, 1);

        // Act
        request.setEmail(email);
        request.setPassword(password);
        request.setName(name);
        request.setBirthdate(birthdate);

        // Assert
        assertEquals(email, request.getEmail());
        assertEquals(password, request.getPassword());
        assertEquals(name, request.getName());
        assertEquals(birthdate, request.getBirthdate());
    }

    @ParameterizedTest
    @MethodSource("emailProvider")
    void testSetAndGetEmail(String input, String expected) {
        RegistrationRequest request = new RegistrationRequest();
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
                Arguments.of("", ""));
    }

    @Test
    void testSetEmail_whitespaceOnly() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("   ");
        // Assuming @NotBlank will be validated elsewhere, focusing on trim
        assertEquals("", request.getEmail());
    }

    @Test
    void testGetSetPassword() {
        RegistrationRequest request = new RegistrationRequest();
        request.setPassword("password123");
        assertEquals("password123", request.getPassword());
    }

    @ParameterizedTest
    @MethodSource("nameProvider")
    void testSetAndGetName(String input, String expected) {
        RegistrationRequest request = new RegistrationRequest();
        request.setName(input);

        if (expected == null) {
            assertNull(request.getName(), "Expected null for input: " + input);
        } else {
            assertEquals(expected, request.getName(), "Trimming didn’t work for input: " + input);
        }
    }

    static Stream<Arguments> nameProvider() {
        return Stream.of(
                // input , expected
                Arguments.of(" Test Name ", "Test Name"),
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("   ", "") // whitespace-only → empty
        );
    }

    @Test
    void testGetSetBirthdate() {
        RegistrationRequest request = new RegistrationRequest();
        LocalDate birthdate = LocalDate.now();
        request.setBirthdate(birthdate);
        assertEquals(birthdate, request.getBirthdate());
    }
}
