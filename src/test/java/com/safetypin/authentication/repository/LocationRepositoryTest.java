package com.safetypin.authentication.repository;

import com.safetypin.authentication.model.Location;
import com.safetypin.authentication.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LocationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LocationRepository locationRepository;

    @Test
    void findByUser_ShouldReturnLocation() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        entityManager.persist(user);
        
        Location location = new Location();
        location.setLatitude(40.7128);
        location.setLongitude(-74.0060);
        location.setAccuracy(10.0);
        location.setUpdatedAt(LocalDateTime.now());
        location.setUser(user);
        entityManager.persist(location);
        entityManager.flush();
        
        // Act
        Optional<Location> result = locationRepository.findByUser(user);
        
        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getLatitude()).isEqualTo(40.7128);
        assertThat(result.get().getUser()).isEqualTo(user);
    }
    
    @Test
    void findByUser_Id_ShouldReturnLocation() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        entityManager.persist(user);
        
        Location location = new Location();
        location.setLatitude(40.7128);
        location.setLongitude(-74.0060);
        location.setUpdatedAt(LocalDateTime.now());
        location.setUser(user);
        entityManager.persist(location);
        entityManager.flush();
        
        // Act
        Optional<Location> result = locationRepository.findByUser_Id(user.getId());
        
        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getLatitude()).isEqualTo(40.7128);
    }
    
    @Test
    void findByUserIdIn_ShouldReturnLocations() {
        // Arrange
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setName("User One");
        entityManager.persist(user1);
        
        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setName("User Two");
        entityManager.persist(user2);
        
        Location location1 = new Location();
        location1.setLatitude(40.7128);
        location1.setLongitude(-74.0060);
        location1.setUpdatedAt(LocalDateTime.now());
        location1.setUser(user1);
        entityManager.persist(location1);
        
        Location location2 = new Location();
        location2.setLatitude(34.0522);
        location2.setLongitude(-118.2437);
        location2.setUpdatedAt(LocalDateTime.now());
        location2.setUser(user2);
        entityManager.persist(location2);
        entityManager.flush();
        
        // Act
        List<Location> result = locationRepository.findByUserIdIn(
                Arrays.asList(user1.getId(), user2.getId()));
        
        // Assert
        assertThat(result).hasSize(2);
    }
}