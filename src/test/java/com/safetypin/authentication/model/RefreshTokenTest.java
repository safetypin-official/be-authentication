package com.safetypin.authentication.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RefreshTokenTest {
    @Test
    void testRefreshToken() {
        RefreshToken refreshToken = new RefreshToken();
        assertNull(refreshToken.getToken());
        assertNull(refreshToken.getExpiryTime());
        assertNull(refreshToken.getUser());
    }

    @Test
    void testGettersAndSetters() {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setToken("sample-token");
        assertEquals("sample-token", refreshToken.getToken());

        Instant expiry = Instant.now().plusSeconds(3600);
        refreshToken.setExpiryTime(expiry);
        assertEquals(expiry, refreshToken.getExpiryTime());

        User user = new User();
        refreshToken.setUser(user);
        assertEquals(user, refreshToken.getUser());
    }
}
