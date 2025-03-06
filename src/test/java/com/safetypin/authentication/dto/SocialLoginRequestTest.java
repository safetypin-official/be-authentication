package com.safetypin.authentication.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SocialLoginRequestTest {

    private final Validator validator;

    public SocialLoginRequestTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
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
        assertThat(violations)
                .isNotEmpty()
                .hasSize(6); // All fields should be invalid
    }
}
