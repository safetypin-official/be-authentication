package com.safetypin.authentication.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LocationTest {

    @Test
    void updateTimestamp_ShouldSetCurrentTime() {
        // Arrange
        Location location = new Location();
        
        // Act
        location.updateTimestamp();
        
        // Assert
        assertThat(location.getUpdatedAt()).isNotNull();
        assertThat(location.getUpdatedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
        assertThat(location.getUpdatedAt()).isAfter(LocalDateTime.now().minusMinutes(1));
    }
    
    @Test
    void testGettersAndSetters() {
        // Arrange
        Location location = new Location();
        User user = new User();
        user.setName("Test User");
        LocalDateTime now = LocalDateTime.now();
        
        // Act
        location.setId(1L);
        location.setLatitude(40.7128);
        location.setLongitude(-74.0060);
        location.setAccuracy(10.0);
        location.setUpdatedAt(now);
        location.setUser(user);
        
        // Assert
        assertThat(location.getId()).isEqualTo(1L);
        assertThat(location.getLatitude()).isEqualTo(40.7128);
        assertThat(location.getLongitude()).isEqualTo(-74.0060);
        assertThat(location.getAccuracy()).isEqualTo(10.0);
        assertThat(location.getUpdatedAt()).isEqualTo(now);
        assertThat(location.getUser()).isEqualTo(user);
    }
}