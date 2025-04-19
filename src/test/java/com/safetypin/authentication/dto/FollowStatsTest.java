package com.safetypin.authentication.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FollowStatsTest {

    @Test
    void testFollowStatsCreation() {
        // Arrange
        long followersCount = 10;
        long followingCount = 5;
        boolean isFollowing = true;

        // Act
        FollowStats stats = new FollowStats(followersCount, followingCount, isFollowing);

        // Assert
        assertEquals(followersCount, stats.getFollowersCount());
        assertEquals(followingCount, stats.getFollowingCount());
        assertTrue(stats.isFollowing());
    }

    @Test
    void testEmptyConstructor() {
        // Act
        FollowStats stats = new FollowStats();

        // Assert
        assertEquals(0, stats.getFollowersCount());
        assertEquals(0, stats.getFollowingCount());
        assertFalse(stats.isFollowing());
    }

    @Test
    void testBuilder() {
        // Act
        FollowStats stats = FollowStats.builder()
                .followersCount(15)
                .followingCount(7)
                .isFollowing(true)
                .build();

        // Assert
        assertEquals(15, stats.getFollowersCount());
        assertEquals(7, stats.getFollowingCount());
        assertTrue(stats.isFollowing());
    }

    @Test
    void testSetters() {
        // Arrange
        FollowStats stats = new FollowStats();

        // Act
        stats.setFollowersCount(25);
        stats.setFollowingCount(12);
        stats.setFollowing(true);

        // Assert
        assertEquals(25, stats.getFollowersCount());
        assertEquals(12, stats.getFollowingCount());
        assertTrue(stats.isFollowing());
    }
}