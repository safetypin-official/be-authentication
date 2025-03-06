package com.safetypin.authentication.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordResetRequestTest {

    private final Validator validator;

    public PasswordResetRequestTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
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
}
