package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.FriendLocationResponse;
import com.safetypin.authentication.dto.LocationRequest;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.model.Location;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.FollowRepository;
import com.safetypin.authentication.repository.LocationRepository;
import com.safetypin.authentication.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private LocationService locationService;

    @Test
    public void updateLocation_WhenUserExistsAndLocationExists_ShouldUpdateLocation() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        
        Location location = new Location();
        
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setLatitude(40.7128);
        locationRequest.setLongitude(-74.0060);
        locationRequest.setAccuracy(10.0);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(locationRepository.findByUser(user)).thenReturn(Optional.of(location));
        
        // Act
        locationService.updateLocation(userId, locationRequest);
        
        // Assert
        verify(locationRepository).save(location);
        assertThat(location.getLatitude()).isEqualTo(40.7128);
        assertThat(location.getLongitude()).isEqualTo(-74.0060);
        assertThat(location.getAccuracy()).isEqualTo(10.0);
        assertThat(location.getUser()).isEqualTo(user);
    }

    @Test
    public void updateLocation_WhenUserExistsAndLocationDoesNotExist_ShouldCreateLocation() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setLatitude(40.7128);
        locationRequest.setLongitude(-74.0060);
        locationRequest.setAccuracy(10.0);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(locationRepository.findByUser(user)).thenReturn(Optional.empty());
        
        // Act
        locationService.updateLocation(userId, locationRequest);
        
        // Assert
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    public void updateLocation_WhenUserDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setLatitude(40.7128);
        locationRequest.setLongitude(-74.0060);
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            locationService.updateLocation(userId, locationRequest);
        });
        
        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    public void getFriendsLocations_WhenMutualFollowersExist_ShouldReturnLocations() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID friend1Id = UUID.randomUUID();
        
        User friend1 = new User();
        friend1.setId(friend1Id);
        friend1.setName("Friend One");
        friend1.setProfilePicture("friend1.jpg");
        
        Location friend1Location = new Location();
        friend1Location.setLatitude(34.0522);
        friend1Location.setLongitude(-118.2437);
        friend1Location.setUpdatedAt(LocalDateTime.now());
        friend1Location.setUser(friend1);
        
        List<UUID> following = Arrays.asList(friend1Id);
        List<UUID> followers = Arrays.asList(friend1Id);
        
        when(followRepository.findFollowingIds(userId)).thenReturn(following);
        when(followRepository.findFollowerIds(userId)).thenReturn(followers);
        when(locationRepository.findByUserIdIn(Arrays.asList(friend1Id)))
            .thenReturn(Arrays.asList(friend1Location));
        
        // Act
        List<FriendLocationResponse> result = locationService.getFriendsLocations(userId);
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(friend1Id);
        assertThat(result.get(0).getName()).isEqualTo("Friend One");
        assertThat(result.get(0).getProfilePicture()).isEqualTo("friend1.jpg");
        assertThat(result.get(0).getLatitude()).isEqualTo(34.0522);
        assertThat(result.get(0).getLongitude()).isEqualTo(-118.2437);
    }

    @Test
    public void getFriendsLocations_WhenNoMutualFollowers_ShouldReturnEmptyList() {
        // Arrange
        UUID userId = UUID.randomUUID();
        
        when(followRepository.findFollowingIds(userId)).thenReturn(Collections.emptyList());
        when(followRepository.findFollowerIds(userId)).thenReturn(Collections.emptyList());
        when(locationRepository.findByUserIdIn(Collections.emptyList()))
            .thenReturn(Collections.emptyList());
        
        // Act
        List<FriendLocationResponse> result = locationService.getFriendsLocations(userId);
        
        // Assert
        assertThat(result).isEmpty();
    }
    
    @Test
    public void getFriendsLocations_WhenFollowingButNotFollowedBack_ShouldNotIncludeInResults() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();
        UUID nonMutualId = UUID.randomUUID();
        
        List<UUID> following = Arrays.asList(friendId, nonMutualId);
        List<UUID> followers = Arrays.asList(friendId);
        
        when(followRepository.findFollowingIds(userId)).thenReturn(following);
        when(followRepository.findFollowerIds(userId)).thenReturn(followers);
        when(locationRepository.findByUserIdIn(Arrays.asList(friendId)))
            .thenReturn(Collections.emptyList());
        
        // Act
        List<FriendLocationResponse> result = locationService.getFriendsLocations(userId);
        
        // Assert
        assertThat(result).isEmpty();
        verify(locationRepository).findByUserIdIn(Arrays.asList(friendId));
    }
}