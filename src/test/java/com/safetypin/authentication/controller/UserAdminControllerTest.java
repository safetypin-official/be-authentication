package com.safetypin.authentication.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.exception.UnauthorizedAccessException;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.service.JwtService;
import com.safetypin.authentication.service.UserAdminService;

@WebMvcTest(UserAdminController.class)
@Import({ UserAdminControllerTestConfig.class })
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAdminService userAdminService;

    @Autowired
    private JwtService jwtService;

    private UUID moderatorId;
    private UUID targetUserId;
    private UserResponse moderatorResponse;
    private static final String BEARER_PREFIX = "Bearer ";

    @BeforeEach
    void setUp() {
        // Reset mocks to clear any previous interactions
        reset(userAdminService, jwtService);

        moderatorId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();

        // Use builder pattern for UserResponse
        moderatorResponse = UserResponse.builder()
                .id(moderatorId)
                .name("Test Moderator")
                .role(Role.MODERATOR.name())
                .isVerified(true)
                .build();
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void deleteUser_Success() throws Exception {
        // Mock JWT validation
        when(jwtService.getUserFromJwtToken(anyString())).thenReturn(moderatorResponse);

        // Mock successful deletion
        doNothing().when(userAdminService).deleteUser(any(), any());

        // Perform DELETE request
        mockMvc.perform(delete("/api/admin/users/{userId}", targetUserId)
                .header("Authorization", BEARER_PREFIX + "valid-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("User deleted successfully"));

        // Verify service was called
        verify(userAdminService).deleteUser(any(), any());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void deleteUser_InvalidCredentials() throws Exception {
        // Mock JWT validation failure
        when(jwtService.getUserFromJwtToken(anyString()))
                .thenThrow(new InvalidCredentialsException("Invalid token"));

        // Perform DELETE request
        mockMvc.perform(delete("/api/admin/users/{userId}", targetUserId)
                .header("Authorization", BEARER_PREFIX + "invalid-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Invalid token"));

        // Verify service was never called
        verify(userAdminService, never()).deleteUser(any(), any());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void deleteUser_UnauthorizedAccess() throws Exception {
        // Mock JWT validation
        when(jwtService.getUserFromJwtToken(anyString())).thenReturn(moderatorResponse);

        // Mock unauthorized access
        doThrow(new UnauthorizedAccessException("Only moderators can delete user accounts"))
                .when(userAdminService).deleteUser(any(), any());

        // Perform DELETE request
        mockMvc.perform(delete("/api/admin/users/{userId}", targetUserId)
                .header("Authorization", BEARER_PREFIX + "valid-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Only moderators can delete user accounts"));
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void deleteUser_UserNotFound() throws Exception {
        // Mock JWT validation
        when(jwtService.getUserFromJwtToken(anyString())).thenReturn(moderatorResponse);

        // Mock resource not found
        doThrow(new ResourceNotFoundException("User not found with id " + targetUserId))
                .when(userAdminService).deleteUser(any(), any());

        // Perform DELETE request
        mockMvc.perform(delete("/api/admin/users/{userId}", targetUserId)
                .header("Authorization", BEARER_PREFIX + "valid-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("User not found with id " + targetUserId));
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void deleteUser_InternalServerError() throws Exception {
        // Mock JWT validation
        when(jwtService.getUserFromJwtToken(anyString())).thenReturn(moderatorResponse);

        // Mock internal server error
        doThrow(new RuntimeException("Database connection error"))
                .when(userAdminService).deleteUser(any(), any());

        // Perform DELETE request
        mockMvc.perform(delete("/api/admin/users/{userId}", targetUserId)
                .header("Authorization", BEARER_PREFIX + "valid-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Error deleting user: Database connection error"));
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void deleteUser_ShouldCallDeleteUserWithCorrectParams() throws Exception {
        // Mock JWT validation
        when(jwtService.getUserFromJwtToken(anyString())).thenReturn(moderatorResponse);

        // Mock successful deletion
        doNothing().when(userAdminService).deleteUser(any(), any());

        // Perform DELETE request
        mockMvc.perform(delete("/api/admin/users/{userId}", targetUserId)
                .header("Authorization", BEARER_PREFIX + "valid-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify userAdminService.deleteUser is called with the correct parameters
        verify(userAdminService).deleteUser(moderatorId, targetUserId);
    }
}
