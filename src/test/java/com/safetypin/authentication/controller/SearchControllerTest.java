package com.safetypin.authentication.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.service.ProfileService;
import com.safetypin.authentication.service.UserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchController Unit Tests")
class SearchControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private SearchController searchController;

    private User user1;
    private User user2;
    private User user3;
    private List<User> allUsers;
    private List<User> johnUsers;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setName("John Doe");
        user1.setEmail("john.doe@example.com");
        user1.setRole(Role.REGISTERED_USER);
        user1.setVerified(true);

        user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setName("Jane Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setRole(Role.REGISTERED_USER);
        user2.setVerified(false);

        user3 = new User();
        user3.setId(UUID.randomUUID());
        user3.setName("John Richard");
        user3.setEmail("john.richard@example.com");
        user3.setRole(Role.MODERATOR); // Different role
        user3.setVerified(true);

        allUsers = Arrays.asList(user1, user2, user3);
        johnUsers = Arrays.asList(user1, user3);
    }

    @Nested
    @DisplayName("searchUsersByName Method Tests")
    class SearchUsersByNameTests {

        @Test
        @DisplayName("Search with query 'john' returns matching users")
        void searchUsersByName_withQuery_returnsMatchingUsers() {
            // Arrange
            String query = "john";
            when(userService.findUsersByNameContaining(query)).thenReturn(johnUsers);

            // Act
            ResponseEntity<List<UserResponse>> response = searchController.searchUsersByName(query);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            List<UserResponse> resultUsers = response.getBody();
            assertThat(resultUsers, hasSize(2));
            // Assert based on UserResponse fields (assuming UserResponse has these getters)
            assertThat(resultUsers.get(0).getId(), is(user1.getId()));
            assertThat(resultUsers.get(0).getName(), is("John Doe"));
            // Assuming UserResponse.getRole() returns String
            assertThat(resultUsers.get(0).getRole(), is(Role.REGISTERED_USER.name()));
            assertThat(resultUsers.get(0).isVerified(), is(true));
            assertThat(resultUsers.get(1).getId(), is(user3.getId()));
            assertThat(resultUsers.get(1).getName(), is("John Richard"));
            // Assuming UserResponse.getRole() returns String
            assertThat(resultUsers.get(1).getRole(), is(Role.MODERATOR.name()));
            assertThat(resultUsers.get(1).isVerified(), is(true));

            verify(userService, times(1)).findUsersByNameContaining(query);
            verify(userService, never()).findAllUsers();
        }

        @Test
        @DisplayName("Search with null query parameter returns all users")
        void searchUsersByName_nullQuery_returnsAllUsers() {
            // Arrange
            when(userService.findAllUsers()).thenReturn(allUsers);

            // Act
            ResponseEntity<List<UserResponse>> response = searchController.searchUsersByName(null);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            List<UserResponse> resultUsers = response.getBody();
            assertThat(resultUsers, hasSize(3));
            assertThat(resultUsers.get(0).getName(), is("John Doe"));
            assertThat(resultUsers.get(1).getName(), is("Jane Smith"));
            assertThat(resultUsers.get(2).getName(), is("John Richard"));
            // Optionally assert roles/verified status if needed
            assertThat(resultUsers.get(0).getRole(), is(Role.REGISTERED_USER.name()));
            assertThat(resultUsers.get(1).getRole(), is(Role.REGISTERED_USER.name()));
            assertThat(resultUsers.get(2).getRole(), is(Role.MODERATOR.name()));

            verify(userService, times(1)).findAllUsers();
            verify(userService, never()).findUsersByNameContaining(anyString());
        }

        @Test
        @DisplayName("Search with empty query parameter returns all users")
        void searchUsersByName_emptyQuery_returnsAllUsers() {
            // Arrange
            String query = "";
            when(userService.findAllUsers()).thenReturn(allUsers);

            // Act
            ResponseEntity<List<UserResponse>> response = searchController.searchUsersByName(query);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            List<UserResponse> resultUsers = response.getBody();
            assertThat(resultUsers, hasSize(3));
            assertThat(resultUsers.get(0).getName(), is("John Doe"));
            assertThat(resultUsers.get(1).getName(), is("Jane Smith"));
            assertThat(resultUsers.get(2).getName(), is("John Richard"));
            assertThat(resultUsers.get(0).getRole(), is(Role.REGISTERED_USER.name()));
            assertThat(resultUsers.get(1).getRole(), is(Role.REGISTERED_USER.name()));
            assertThat(resultUsers.get(2).getRole(), is(Role.MODERATOR.name()));

            verify(userService, times(1)).findAllUsers();
            verify(userService, never()).findUsersByNameContaining(anyString());
        }

        @Test
        @DisplayName("Search with whitespace query parameter returns all users")
        void searchUsersByName_whitespaceQuery_returnsAllUsers() {
            // Arrange
            String query = "   ";
            when(userService.findAllUsers()).thenReturn(allUsers);

            // Act
            ResponseEntity<List<UserResponse>> response = searchController.searchUsersByName(query);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            List<UserResponse> resultUsers = response.getBody();
            assertThat(resultUsers, hasSize(3));
            assertThat(resultUsers.get(0).getName(), is("John Doe"));
            assertThat(resultUsers.get(1).getName(), is("Jane Smith"));
            assertThat(resultUsers.get(2).getName(), is("John Richard"));
            assertThat(resultUsers.get(0).getRole(), is(Role.REGISTERED_USER.name()));
            assertThat(resultUsers.get(1).getRole(), is(Role.REGISTERED_USER.name()));
            assertThat(resultUsers.get(2).getRole(), is(Role.MODERATOR.name()));

            verify(userService, times(1)).findAllUsers();
            verify(userService, never()).findUsersByNameContaining(anyString());
        }

        @Test
        @DisplayName("Search with query yielding no results returns empty list")
        void searchUsersByName_noResults_returnsEmptyList() {
            // Arrange
            String query = "nonexistent";
            when(userService.findUsersByNameContaining(query)).thenReturn(Collections.emptyList());

            // Act
            ResponseEntity<List<UserResponse>> response = searchController.searchUsersByName(query);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            List<UserResponse> resultUsers = response.getBody();
            assertThat(resultUsers, hasSize(0));

            verify(userService, times(1)).findUsersByNameContaining(query);
            verify(userService, never()).findAllUsers();
        }
    }
}
