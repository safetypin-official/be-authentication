package com.safetypin.authentication.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.safetypin.authentication.dto.ApiResponse;
import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.exception.UnauthorizedAccessException;
import com.safetypin.authentication.service.JwtService;
import com.safetypin.authentication.service.UserAdminService;

@RestController
@RequestMapping("/api/admin/users")
public class UserAdminController {

    private final UserAdminService userAdminService;
    private final JwtService jwtService;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String STATUS_SUCCESS = "success";
    private static final String STATUS_ERROR = "error";

    @Autowired
    public UserAdminController(UserAdminService userAdminService, JwtService jwtService) {
        this.userAdminService = userAdminService;
        this.jwtService = jwtService;
    }

    /**
     * Delete a user account
     * This endpoint can only be accessed by users with MODERATOR role
     * 
     * @param userId     ID of the user to delete
     * @param authHeader Authorization header with JWT token
     * @return Response with success or error message
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable UUID userId,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // Extract token from auth header
            String token = authHeader.replace(BEARER_PREFIX, "");

            // Get moderator info from JWT token
            UserResponse moderator = jwtService.getUserFromJwtToken(token);
            UUID moderatorId = moderator.getId();

            // Delete the user
            userAdminService.deleteUser(moderatorId, userId);

            // Return success response
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status(STATUS_SUCCESS)
                    .message("User deleted successfully")
                    .build());
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<String>builder()
                            .status(STATUS_ERROR)
                            .message(e.getMessage())
                            .build());
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<String>builder()
                            .status(STATUS_ERROR)
                            .message(e.getMessage())
                            .build());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<String>builder()
                            .status(STATUS_ERROR)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<String>builder()
                            .status(STATUS_ERROR)
                            .message("Error deleting user: " + e.getMessage())
                            .build());
        }
    }
}
