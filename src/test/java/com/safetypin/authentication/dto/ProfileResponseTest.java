package com.safetypin.authentication.dto;

import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProfileResponseTest {

    @Test
    void testFromUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setRole(Role.REGISTERED_USER); // Assuming User.Role is an enum
        user.setVerified(true);
        user.setInstagram("user_instagram");
        user.setTwitter("user_twitter");
        user.setLine("user_line");
        user.setTiktok("user_tiktok");
        user.setDiscord("user_discord");
        user.setName("John Doe");
        user.setProfilePicture("profile_pic_url");
        user.setProfileBanner("profile_banner_url");

        // Act
        ProfileResponse response = ProfileResponse.fromUser(user); // Assert
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals(Role.REGISTERED_USER, response.getRole());
        assertTrue(response.isVerified());
        assertEquals("user_instagram", response.getInstagram());
        assertEquals("user_twitter", response.getTwitter());
        assertEquals("user_line", response.getLine());
        assertEquals("user_tiktok", response.getTiktok());
        assertEquals("user_discord", response.getDiscord());
        assertEquals("John Doe", response.getName());
        assertEquals("profile_pic_url", response.getProfilePicture());
        assertEquals("profile_banner_url", response.getProfileBanner());
    }

    @Test
    void testFromUserWithNullRole() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setRole(null); // Role is null
        user.setVerified(false);

        // Act
        ProfileResponse response = ProfileResponse.fromUser(user);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertNull(response.getRole());
        assertFalse(response.isVerified());
    }
}