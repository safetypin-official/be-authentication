package com.safetypin.authentication.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.time.LocalDate;
import java.util.Set;

class RegistrationRequestTest {

    private final Validator validator;

    public RegistrationRequestTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
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
        assertThat(violations)
                .isNotEmpty()
                .hasSize(4); // Email, password, name, and birthdate should all be invalid
    }
}
