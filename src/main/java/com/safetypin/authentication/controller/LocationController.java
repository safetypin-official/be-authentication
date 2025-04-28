package com.safetypin.authentication.controller;

import com.safetypin.authentication.dto.ApiResponse;
import com.safetypin.authentication.dto.FriendLocationResponse;
import com.safetypin.authentication.dto.LocationRequest;
import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.service.JwtService;
import com.safetypin.authentication.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;
    private final JwtService jwtService;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String STATUS_SUCCESS = "success";

    @Autowired
    public LocationController(LocationService locationService, JwtService jwtService) {
        this.locationService = locationService;
        this.jwtService = jwtService;
    }

    /**
     * Extract user ID from authorization header
     */
    private UUID extractUserIdFromAuthHeader(String authHeader) {
        String token = authHeader.replace(BEARER_PREFIX, "");
        UserResponse user = jwtService.getUserFromJwtToken(token);
        return user.getId();
    }

    /**
     * Update the current user's location
     */
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @RequestBody LocationRequest locationRequest,
            @RequestHeader("Authorization") String authHeader) {
        
        UUID currentUserId = extractUserIdFromAuthHeader(authHeader);
        
        locationService.updateLocation(currentUserId, locationRequest);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(STATUS_SUCCESS)
                .message("Location updated successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get locations of friends (mutual followers)
     */
    @GetMapping("/friends")
    public ResponseEntity<ApiResponse<List<FriendLocationResponse>>> getFriendsLocations(
            @RequestHeader("Authorization") String authHeader) {
        
        UUID currentUserId = extractUserIdFromAuthHeader(authHeader);
        
        List<FriendLocationResponse> friendsLocations = locationService.getFriendsLocations(currentUserId);
        
        ApiResponse<List<FriendLocationResponse>> response = ApiResponse.<List<FriendLocationResponse>>builder()
                .status(STATUS_SUCCESS)
                .data(friendsLocations)
                .message("Friends' locations retrieved successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }
}