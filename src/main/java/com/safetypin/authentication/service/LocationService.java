package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.FriendLocationResponse;
import com.safetypin.authentication.dto.LocationRequest;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.model.Location;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.FollowRepository;
import com.safetypin.authentication.repository.LocationRepository;
import com.safetypin.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository, 
                          UserRepository userRepository,
                          FollowRepository followRepository) {
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    /**
     * Update a user's location
     */
    public void updateLocation(UUID userId, LocationRequest locationRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Location location = locationRepository.findByUser(user)
                .orElse(new Location());

        location.setLatitude(locationRequest.getLatitude());
        location.setLongitude(locationRequest.getLongitude());
        location.setAccuracy(locationRequest.getAccuracy());
        location.setUpdatedAt(LocalDateTime.now());
        location.setUser(user);

        locationRepository.save(location);
    }

    /**
     * Get locations of mutual followers (friends)
     */
    public List<FriendLocationResponse> getFriendsLocations(UUID userId) {
        // Find mutual followers (users who follow each other)
        List<UUID> mutualFollowers = getMutualFollowers(userId);
        
        // Get locations for these mutual followers
        List<Location> friendLocations = locationRepository.findByUserIdIn(mutualFollowers);
        
        // Convert to DTOs with required information
        return friendLocations.stream()
                .map(this::convertToFriendLocationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find users who have a mutual follow relationship with the given user
     */
    private List<UUID> getMutualFollowers(UUID userId) {
        // Get users the current user follows
        List<UUID> following = followRepository.findFollowingIds(userId);
        
        // Get users who follow the current user
        List<UUID> followers = followRepository.findFollowerIds(userId);
        
        // Find the intersection (mutual followers)
        return followers.stream()
                .filter(following::contains)
                .collect(Collectors.toList());
    }

    /**
     * Convert Location entity to FriendLocationResponse DTO
     */
    private FriendLocationResponse convertToFriendLocationResponse(Location location) {
        User user = location.getUser();
        return FriendLocationResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .profilePicture(user.getProfilePicture())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .updatedAt(location.getUpdatedAt())
                .build();
    }
}