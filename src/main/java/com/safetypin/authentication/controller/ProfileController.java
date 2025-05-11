package com.safetypin.authentication.controller;

import com.safetypin.authentication.dto.*;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.service.JwtService;
import com.safetypin.authentication.service.ProfileService;
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
    public ResponseEntity<AuthResponse> getProfile(@PathVariable UUID id, @RequestHeader("Authorization") String authHeader) {
        UUID viewerId;
        try {
            // Extract viewer ID from JWT token
            UserResponse viewer = parseUserResponseFromAuthHeader(authHeader);
            viewerId = viewer.getId();
        } catch (Exception e) { viewerId = null; /* If token is not present or invalid, viewerId remains null */ }

        try {
            ProfileResponse profile = profileService.getProfile(id, viewerId);
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
            // Extract user ID from JWT token
            UserResponse user = parseUserResponseFromAuthHeader(authHeader);
            UUID id = user.getId();
            
            ProfileResponse profile = profileService.getProfile(id, id);
            return ResponseEntity.ok(new AuthResponse(true, "Profile retrieved successfully", profile));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, e.getMessage(), null));
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
            UserResponse user = parseUserResponseFromAuthHeader(authHeader);
            UUID id = user.getId();

            ProfileResponse updatedProfile = profileService.updateProfile(id, request);
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

    @GetMapping("/me/views")
    public ResponseEntity<AuthResponse> getProfileViews(@RequestHeader("Authorization") String authHeader) {
        try {
            UserResponse user = parseUserResponseFromAuthHeader(authHeader);
            UUID userId = user.getId();

            List<ProfileViewDTO> profileViews = profileService.getProfileViews(userId);
            return ResponseEntity.ok(new AuthResponse(true, "Profile views retrieved successfully", profileViews));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, e.getMessage(), null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AuthResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(false, "Error retrieving profile views: " + e.getMessage(), null));
        }
    }

    @PostMapping("/batch")
    public Map<UUID, PostedByData> getUsersBatch(@RequestBody List<UUID> userIds) {
        return profileService.getUsersBatch(userIds);
    }

    private UserResponse parseUserResponseFromAuthHeader(String authHeader) throws InvalidCredentialsException {
        String token = authHeader.replace("Bearer ", "");
        try {
            return jwtService.getUserFromJwtToken(token);
        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid token");
        }
    }
}
