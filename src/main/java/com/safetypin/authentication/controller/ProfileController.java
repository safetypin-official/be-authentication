package com.safetypin.authentication.controller;

import com.safetypin.authentication.constants.ApiConstants;
import com.safetypin.authentication.dto.*;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.service.ProfileService;
import com.safetypin.authentication.util.JwtUtils;
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
    private final JwtUtils jwtUtils;

    @Autowired
    public ProfileController(ProfileService profileService, JwtUtils jwtUtils) {
        this.profileService = profileService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@PathVariable UUID id, @RequestHeader("Authorization") String authHeader) {
        UUID viewerId = null;
        UserResponse viewer = jwtUtils.parseUserFromAuthHeaderSafe(authHeader);
        if (viewer != null) {
            viewerId = viewer.getId();
        }

        try {
            ProfileResponse profile = profileService.getProfile(id, viewerId);
            return ResponseEntity.ok(ApiResponse.success(ApiConstants.MSG_PROFILE_RETRIEVED, profile));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving profile: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            UserResponse user = jwtUtils.parseUserFromAuthHeader(authHeader);
            UUID id = user.getId();
            
            ProfileResponse profile = profileService.getProfile(id, id);
            return ResponseEntity.ok(ApiResponse.success(ApiConstants.MSG_PROFILE_RETRIEVED, profile));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving profile: " + e.getMessage()));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateMyProfile(
            @RequestBody UpdateProfileRequest request,
            @RequestHeader("Authorization") String authHeader) {

        try { 
            UserResponse user = jwtUtils.parseUserFromAuthHeader(authHeader);
            UUID id = user.getId();

            ProfileResponse updatedProfile = profileService.updateProfile(id, request);
            return ResponseEntity.ok(ApiResponse.success(ApiConstants.MSG_PROFILE_UPDATED, updatedProfile));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating profile: " + e.getMessage()));
        }
    }

    @GetMapping("/me/views")
    public ResponseEntity<ApiResponse<List<ProfileViewDTO>>> getProfileViews(@RequestHeader("Authorization") String authHeader) {
        try {
            UserResponse user = jwtUtils.parseUserFromAuthHeader(authHeader);
            UUID userId = user.getId();

            List<ProfileViewDTO> profileViews = profileService.getProfileViews(userId);
            return ResponseEntity.ok(ApiResponse.success(ApiConstants.MSG_PROFILE_VIEWS, profileViews));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving profile views: " + e.getMessage()));
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Map<UUID, PostedByData>>> getUsersBatch(@RequestBody List<UUID> userIds) {
        Map<UUID, PostedByData> batchData = profileService.getUsersBatch(userIds);
        return ResponseEntity.ok(ApiResponse.success(ApiConstants.MSG_USERS_BATCH, batchData));
    }
}
