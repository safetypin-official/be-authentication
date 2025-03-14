package com.safetypin.authentication.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SocialLoginRequestTest {

    @Test
    void testGettersAndSetters() {
        // Arrange
        SocialLoginRequest request = new SocialLoginRequest();
        String provider = "GOOGLE";
        String socialToken = "token123";
        String email = "test@example.com";
        String name = "Test User";
        LocalDate birthdate = LocalDate.of(1990, 1, 1);
        String socialId = "social123";

        // Act
        request.setProvider(provider);
        request.setSocialToken(socialToken);
        request.setEmail(email);
        request.setName(name);
        request.setBirthdate(birthdate);
        request.setSocialId(socialId);

        // Assert
        assertEquals(provider, request.getProvider());
        assertEquals(socialToken, request.getSocialToken());
        assertEquals(email, request.getEmail());
        assertEquals(name, request.getName());
        assertEquals(birthdate, request.getBirthdate());
        assertEquals(socialId, request.getSocialId());
    }
}
