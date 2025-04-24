package com.safetypin.authentication.controller;

import com.safetypin.authentication.dto.AuthResponse;
import com.safetypin.authentication.dto.PostedByData;
import com.safetypin.authentication.dto.ProfileResponse;
import com.safetypin.authentication.dto.UpdateProfileRequest;
import com.safetypin.authentication.dto.UserPostResponse;
import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.service.JwtService;
import com.safetypin.authentication.service.ProfileService;
import com.safetypin.authentication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    private final JwtService jwtService;

    @Autowired
    public ProfileController(ProfileService profileService, JwtService jwtService) {
        this.profileService = profileService;
        this.jwtService = jwtService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthResponse> getProfile(@PathVariable UUID id) {
        try {
            ProfileResponse profile = profileService.getProfile(id);
            return ResponseEntity.ok(new AuthResponse(true, "Profile retrieved successfully", profile));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AuthResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Error retrieving profile: " + e.getMessage(), null));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getMyProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract the token from the Authorization header
            String token = authHeader.replace("Bearer ", "");

            // Extract user ID from JWT token
            UserResponse user = jwtService.getUserFromJwtToken(token);

            UUID id = user.getId();

            ProfileResponse profile = profileService.getProfile(id);
            return ResponseEntity.ok(new AuthResponse(true, "Profile retrieved successfully", profile));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AuthResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Error retrieving profile: " + e.getMessage(), null));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<AuthResponse> updateMyProfile(
            @RequestBody UpdateProfileRequest request,
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.replace("Bearer ", "");

            UserResponse user = jwtService.getUserFromJwtToken(token);

            UUID id = user.getId();

            ProfileResponse updatedProfile = profileService.updateProfile(id, request, token);
            return ResponseEntity.ok(new AuthResponse(true, "Profile updated successfully", updatedProfile));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, e.getMessage(), null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AuthResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Error updating profile: " + e.getMessage(), null));
        }
    }

    @PostMapping("/batch")
    public Map<UUID, PostedByData> getUsersBatch(@RequestBody List<UUID> userIds) {
        return profileService.getUsersBatch(userIds);
    }

    // DEPRECIATED (REPLACED WITH /batch)
    @GetMapping
    public ResponseEntity<AuthResponse> getAllProfiles() {
        try {
            List<UserPostResponse> profiles = profileService.getAllProfiles();
            return ResponseEntity.ok(new AuthResponse(true, "All profiles retrieved successfully", profiles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Error retrieving profiles: " + e.getMessage(), null));
        }
    }
}
