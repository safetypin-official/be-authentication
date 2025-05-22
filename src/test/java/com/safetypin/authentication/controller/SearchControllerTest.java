package com.safetypin.authentication.controller;

import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.service.FollowService;
import com.safetypin.authentication.service.ProfileService;
import com.safetypin.authentication.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchController Unit Tests")
class SearchControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ProfileService profileService;

    @Mock
    private FollowService followService;

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
        johnUsers = Arrays.asList(user1, user3); // Mock followService to return follower counts
        // Order users by followers count: user1 > user2 > user3 to maintain the
        // expected order in tests
        lenient().when(followService.getFollowersCount(user1.getId())).thenReturn(30L);
        lenient().when(followService.getFollowersCount(user2.getId())).thenReturn(20L);
        lenient().when(followService.getFollowersCount(user3.getId())).thenReturn(10L);
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
            ResponseEntity<Page<UserResponse>> paginatedResponse = searchController.searchUsersByName(query, 0, 10);
            Page<UserResponse> page = paginatedResponse.getBody();
            List<UserResponse> users = page != null ? page.getContent() : Collections.emptyList();

            ResponseEntity<List<UserResponse>> response = ResponseEntity
                    .status(paginatedResponse.getStatusCode())
                    .headers(paginatedResponse.getHeaders())
                    .body(users);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            List<UserResponse> resultUsers = response.getBody();
            assertThat(resultUsers, hasSize(2)); // Assert based on UserResponse fields (assuming UserResponse has these
            // getters)
            assertThat(resultUsers.get(0).getId(), is(user1.getId()));
            assertThat(resultUsers.get(0).getName(), is("John Doe"));
            // Using enum comparison instead of string
            assertThat(resultUsers.get(0).getRole(), is(Role.REGISTERED_USER));
            assertThat(resultUsers.get(0).isVerified(), is(true));
            assertThat(resultUsers.get(1).getId(), is(user3.getId()));
            assertThat(resultUsers.get(1).getName(), is("John Richard"));
            // Using enum comparison instead of string
            assertThat(resultUsers.get(1).getRole(), is(Role.MODERATOR));
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
            ResponseEntity<Page<UserResponse>> paginatedResponse = searchController.searchUsersByName(null, 0, 10);
            Page<UserResponse> page = paginatedResponse.getBody();
            List<UserResponse> users = page != null ? page.getContent() : Collections.emptyList();

            ResponseEntity<List<UserResponse>> response = ResponseEntity
                    .status(paginatedResponse.getStatusCode())
                    .headers(paginatedResponse.getHeaders())
                    .body(users);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            List<UserResponse> resultUsers = response.getBody();
            assertThat(resultUsers, hasSize(3));
            assertThat(resultUsers.get(0).getName(), is("John Doe"));
            assertThat(resultUsers.get(1).getName(), is("Jane Smith"));
            assertThat(resultUsers.get(2).getName(), is("John Richard"));
            // Using enum comparison instead of string
            assertThat(resultUsers.get(0).getRole(), is(Role.REGISTERED_USER));
            assertThat(resultUsers.get(1).getRole(), is(Role.REGISTERED_USER));
            assertThat(resultUsers.get(2).getRole(), is(Role.MODERATOR));

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
            ResponseEntity<Page<UserResponse>> paginatedResponse = searchController.searchUsersByName(query, 0, 10);
            Page<UserResponse> page = paginatedResponse.getBody();
            List<UserResponse> users = page != null ? page.getContent() : Collections.emptyList();

            ResponseEntity<List<UserResponse>> response = ResponseEntity
                    .status(paginatedResponse.getStatusCode())
                    .headers(paginatedResponse.getHeaders())
                    .body(users);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            List<UserResponse> resultUsers = response.getBody();
            assertThat(resultUsers, hasSize(3));
            assertThat(resultUsers.get(0).getName(), is("John Doe"));
            assertThat(resultUsers.get(1).getName(), is("Jane Smith"));
            assertThat(resultUsers.get(2).getName(), is("John Richard"));
            assertThat(resultUsers.get(0).getRole(), is(Role.REGISTERED_USER));
            assertThat(resultUsers.get(1).getRole(), is(Role.REGISTERED_USER));
            assertThat(resultUsers.get(2).getRole(), is(Role.MODERATOR));

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
            ResponseEntity<Page<UserResponse>> paginatedResponse = searchController.searchUsersByName(query, 0, 10);
            Page<UserResponse> page = paginatedResponse.getBody();
            List<UserResponse> users = page != null ? page.getContent() : Collections.emptyList();

            ResponseEntity<List<UserResponse>> response = ResponseEntity
                    .status(paginatedResponse.getStatusCode())
                    .headers(paginatedResponse.getHeaders())
                    .body(users);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            List<UserResponse> resultUsers = response.getBody();
            assertThat(resultUsers, hasSize(3));
            assertThat(resultUsers.get(0).getName(), is("John Doe"));
            assertThat(resultUsers.get(1).getName(), is("Jane Smith"));
            assertThat(resultUsers.get(2).getName(), is("John Richard"));
            assertThat(resultUsers.get(0).getRole(), is(Role.REGISTERED_USER));
            assertThat(resultUsers.get(1).getRole(), is(Role.REGISTERED_USER));
            assertThat(resultUsers.get(2).getRole(), is(Role.MODERATOR));

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
            ResponseEntity<Page<UserResponse>> paginatedResponse = searchController.searchUsersByName(query, 0, 10);
            Page<UserResponse> page = paginatedResponse.getBody();
            List<UserResponse> users = page != null ? page.getContent() : Collections.emptyList();

            ResponseEntity<List<UserResponse>> response = ResponseEntity
                    .status(paginatedResponse.getStatusCode())
                    .headers(paginatedResponse.getHeaders())
                    .body(users);

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
