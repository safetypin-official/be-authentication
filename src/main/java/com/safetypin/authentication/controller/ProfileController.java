package com.safetypin.authentication.controller;

import com.safetypin.authentication.dto.AuthResponse;
import com.safetypin.authentication.dto.ProfileResponse;
import com.safetypin.authentication.dto.UpdateProfileRequest;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
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

    @PutMapping("/{id}")
    public ResponseEntity<AuthResponse> updateProfile(
            @PathVariable UUID id,
            @RequestBody UpdateProfileRequest request,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // Extract the token from the Authorization header
            String token = authHeader.replace("Bearer ", "");

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
}
