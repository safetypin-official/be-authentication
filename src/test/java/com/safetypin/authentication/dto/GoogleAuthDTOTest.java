package com.safetypin.authentication.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GoogleAuthDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testAllArgsConstructor() {
        GoogleAuthDTO dto = new GoogleAuthDTO();
        assertNotNull(dto);
    }

    @Test
    void testSetterAndGetterMethods() {
        // Create DTO
        GoogleAuthDTO dto = new GoogleAuthDTO();

        // Set values
        dto.setIdToken("test-id-token");
        dto.setServerAuthCode("test-server-auth-code");

        // Verify getters
        assertEquals("test-id-token", dto.getIdToken());
        assertEquals("test-server-auth-code", dto.getServerAuthCode());
    }

    @Test
    void testValidation_AllFieldsValid() {
        // Create DTO with valid fields
        GoogleAuthDTO dto = new GoogleAuthDTO();
        dto.setIdToken("valid-id-token");
        dto.setServerAuthCode("valid-server-auth-code");

        // Validate
        Set<ConstraintViolation<GoogleAuthDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "No violations expected for valid input");
    }

    @Test
    void testValidation_IdTokenBlank() {
        // Create DTO with blank ID token
        GoogleAuthDTO dto = new GoogleAuthDTO();
        dto.setIdToken("");
        dto.setServerAuthCode("valid-server-auth-code");

        // Validate
        Set<ConstraintViolation<GoogleAuthDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Violations expected for blank ID token");

        // Check specific violation
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("idToken")),
                "Violation should be on idToken field"
        );
    }

    @Test
    void testValidation_ServerAuthCodeBlank() {
        // Create DTO with blank server auth code
        GoogleAuthDTO dto = new GoogleAuthDTO();
        dto.setIdToken("valid-id-token");
        dto.setServerAuthCode("");

        // Validate
        Set<ConstraintViolation<GoogleAuthDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Violations expected for blank server auth code");

        // Check specific violation
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("serverAuthCode")),
                "Violation should be on serverAuthCode field"
        );
    }

    @Test
    void testValidation_BothFieldsBlank() {
        // Create DTO with both fields blank
        GoogleAuthDTO dto = new GoogleAuthDTO();
        dto.setIdToken("");
        dto.setServerAuthCode("");

        // Validate
        Set<ConstraintViolation<GoogleAuthDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Violations expected when both fields are blank");
        assertEquals(2, violations.size(), "Should have violations for both fields");
    }

    @Test
    void testEqualsAndHashCode() {
        // Create two identical DTOs
        GoogleAuthDTO dto1 = new GoogleAuthDTO();
        dto1.setIdToken("test-token");
        dto1.setServerAuthCode("test-code");

        GoogleAuthDTO dto2 = new GoogleAuthDTO();
        dto2.setIdToken("test-token");
        dto2.setServerAuthCode("test-code");

        GoogleAuthDTO dto3 = new GoogleAuthDTO();
        dto3.setIdToken("different-token");
        dto3.setServerAuthCode("different-code");

        // Test equals
        assertEquals(dto1, dto2, "Identical DTOs should be equal");
        assertNotEquals(dto1, dto3, "Different DTOs should not be equal");
        assertNotEquals(null, dto1, "Should not be equal to null");
        assertNotEquals(dto1, new Object(), "Should not be equal to different object type");

        // Test hashCode
        assertEquals(dto1.hashCode(), dto2.hashCode(), "Identical DTOs should have same hashCode");
        assertNotEquals(dto1.hashCode(), dto3.hashCode(), "Different DTOs should have different hashCodes");
    }

    @Test
    void testToString() {
        // Create DTO
        GoogleAuthDTO dto = new GoogleAuthDTO();
        dto.setIdToken("test-token");
        dto.setServerAuthCode("test-code");

        // Check toString
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("idToken"));
        assertTrue(toString.contains("serverAuthCode"));
        assertTrue(toString.contains("test-token"));
        assertTrue(toString.contains("test-code"));
    }
}