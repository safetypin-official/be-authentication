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
import com.safetypin.authentication.model.User; // Ensure User is imported
import com.safetypin.authentication.repository.UserRepository;
import com.safetypin.authentication.service.JwtService;
import com.safetypin.authentication.service.UserAdminService;

@WebMvcTest(UserAdminController.class)
@Import(UserAdminControllerTestConfig.class)
class UserAdminControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserAdminService userAdminService;

        @Autowired
        private JwtService jwtService;

        @Autowired
        private UserRepository userRepository; // Add UserRepository

        private UUID moderatorId;
        private UUID targetUserId;
        private UserResponse moderatorResponse;
        private static final String BEARER_PREFIX = "Bearer ";

        @BeforeEach
        void setUp() {
                // Reset mocks to clear any previous interactions
                reset(userAdminService, jwtService, userRepository);

                moderatorId = UUID.randomUUID();
                targetUserId = UUID.randomUUID();

                moderatorResponse = UserResponse.builder()
                                .id(moderatorId)
                                .name("Test Moderator")
                                .role(Role.MODERATOR.name())
                                .isVerified(true)
                                .build();

                // Default mock for userRepository to return a valid moderator
                User moderator = new User();
                moderator.setId(moderatorId);
                moderator.setRole(Role.MODERATOR);
                when(userRepository.findById(moderatorId)).thenReturn(java.util.Optional.of(moderator));
                // Default mock for jwtService
                when(jwtService.getUserFromJwtToken(anyString())).thenReturn(moderatorResponse);
        }

        @Test
        @WithMockUser(roles = "MODERATOR")
        void deleteUser_Success() throws Exception {
                // UserAdminService is now a real partial mock, or we mock its behavior directly
                // if needed
                // For success, we expect deleteUser to be called and not throw an exception.
                // The actual UserAdminService.deleteUser will be called.
                // Ensure UserAdminService is configured to allow this call through if it's a
                // mock.
                // If UserAdminService is a @MockBean, we need to define its behavior.
                doNothing().when(userAdminService).deleteUser(moderatorId, targetUserId);

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
                // Override default jwtService mock for this specific test
                when(jwtService.getUserFromJwtToken(anyString()))
                                .thenThrow(new InvalidCredentialsException("Invalid token"));

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
                // Override default userRepository mock for this test to simulate a
                // non-moderator
                User nonModerator = new User();
                nonModerator.setId(moderatorId);
                nonModerator.setRole(Role.REGISTERED_USER);
                when(userRepository.findById(moderatorId)).thenReturn(java.util.Optional.of(nonModerator));

                // UserAdminService will throw UnauthorizedAccessException due to the
                // userRepository mock
                // We are testing the controller's handling of this exception thrown by the
                // *actual* service method call
                // So, we mock the service method itself to throw the exception to isolate
                // controller logic if service is complex
                // However, given the previous logic, the service *should* throw it based on
                // userRepository mock.
                // Let's rely on the service throwing it. If UserAdminService is a @MockBean,
                // its actual code won't run.
                // If UserAdminService is a @SpyBean or a real bean, its code will run.
                // Given it's @Autowired and UserAdminControllerTestConfig provides a mock, we
                // need to tell the mock what to do.
                doThrow(new UnauthorizedAccessException("Only moderators can delete user accounts"))
                                .when(userAdminService).deleteUser(moderatorId, targetUserId);

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
                // userRepository mock from setUp is fine (returns a moderator)
                // jwtService mock from setUp is fine
                doThrow(new ResourceNotFoundException("User not found with id " + targetUserId))
                                .when(userAdminService).deleteUser(moderatorId, targetUserId);

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
                // userRepository mock from setUp is fine (returns a moderator)
                // jwtService mock from setUp is fine
                doThrow(new RuntimeException("Database connection error"))
                                .when(userAdminService).deleteUser(moderatorId, targetUserId);

                mockMvc.perform(delete("/api/admin/users/{userId}", targetUserId)
                                .header("Authorization", BEARER_PREFIX + "valid-token")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message")
                                                .value("Error deleting user: Database connection error"));
        }

        @Test
        @WithMockUser(roles = "MODERATOR")
        void deleteUser_ShouldCallDeleteUserWithCorrectParams() throws Exception {
                // userRepository mock from setUp is fine (returns a moderator)
                // jwtService mock from setUp is fine
                doNothing().when(userAdminService).deleteUser(moderatorId, targetUserId);

                mockMvc.perform(delete("/api/admin/users/{userId}", targetUserId)
                                .header("Authorization", BEARER_PREFIX + "valid-token")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());

                // Verify userAdminService.deleteUser is called with the correct parameters
                verify(userAdminService).deleteUser(moderatorId, targetUserId);
        }
}
