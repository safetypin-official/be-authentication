package com.safetypin.authentication.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class DtoTest {

    private final Validator validator;

    public DtoTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void testErrorResponseConstructor() {
        ErrorResponse errorResponse = new ErrorResponse(404, "Resource not found");

        assertThat(errorResponse.getStatus()).isEqualTo(404);
        assertThat(errorResponse.getMessage()).isEqualTo("Resource not found");
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void testPasswordResetRequestValid() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("user@example.com");

        Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void testPasswordResetRequestInvalidEmail() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("invalid-email");

        Set<ConstraintViolation<PasswordResetRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testRegistrationRequestValid() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("user@example.com");
        request.setPassword("securePassword");
        request.setName("John Doe");
        request.setBirthdate(LocalDate.of(1995, 5, 10));

        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void testRegistrationRequestMissingFields() {
        RegistrationRequest request = new RegistrationRequest(); // Missing required fields

        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSize(4); // Email, password, name, and birthdate should all be invalid
    }

    @Test
    void testSocialLoginRequestValid() {
        SocialLoginRequest request = new SocialLoginRequest();
        request.setProvider("GOOGLE");
        request.setSocialToken("validToken");
        request.setEmail("socialuser@example.com");
        request.setName("Social User");
        request.setBirthdate(LocalDate.of(2000, 1, 1));
        request.setSocialId("123456789");

        Set<ConstraintViolation<SocialLoginRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void testSocialLoginRequestMissingFields() {
        SocialLoginRequest request = new SocialLoginRequest(); // Missing required fields

        Set<ConstraintViolation<SocialLoginRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSize(6); // All fields should be invalid
    }
}
