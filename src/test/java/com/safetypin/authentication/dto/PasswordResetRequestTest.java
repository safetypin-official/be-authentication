package com.safetypin.authentication.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

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
