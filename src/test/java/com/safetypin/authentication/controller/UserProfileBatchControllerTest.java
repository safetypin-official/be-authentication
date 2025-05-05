package com.safetypin.authentication.controller;

import com.safetypin.authentication.constants.ApiConstants;
import com.safetypin.authentication.dto.ApiResponse;
import com.safetypin.authentication.dto.PostedByData;
import com.safetypin.authentication.service.ProfileService;
import com.safetypin.authentication.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileController Batch Endpoint Unit Tests")
class ProfileControllerBatchEndpointTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private ProfileController profileController;

    private UUID userId1;
    private UUID userId2;
    private List<UUID> userIds;
    private Map<UUID, PostedByData> postedByDataMap;
    private PostedByData profile1;
    private PostedByData profile2;

    @BeforeEach
    void setUp() {
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();
        userIds = Arrays.asList(userId1, userId2);

        profile1 = PostedByData.builder()
                .userId(userId1)
                .name("User One")
                .profilePicture("pic1.jpg")
                .build();
        profile2 = PostedByData.builder()
                .userId(userId2)
                .name("User Two")
                .profilePicture("pic2.jpg")
                .build();
        postedByDataMap = Map.of(userId1, profile1, userId2, profile2);
    }

    @Test
    @DisplayName("Valid request with user IDs returns corresponding profiles map")
    void getUsersBatch_validRequest_returnsProfilesMap() {
        // Arrange
        when(profileService.getUsersBatch(userIds)).thenReturn(postedByDataMap);

        // Act
        ResponseEntity<ApiResponse<Map<UUID, PostedByData>>> response = profileController.getUsersBatch(userIds);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ApiResponse<Map<UUID, PostedByData>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertEquals(ApiConstants.STATUS_SUCCESS, apiResponse.getStatus());
        assertEquals(ApiConstants.MSG_USERS_BATCH, apiResponse.getMessage());
        
        Map<UUID, PostedByData> responseData = apiResponse.getData();
        assertThat(responseData, aMapWithSize(2));
        assertThat(responseData, hasEntry(is(userId1), is(profile1)));
        assertThat(responseData, hasEntry(is(userId2), is(profile2)));
        assertEquals("User One", responseData.get(userId1).getName());
        assertEquals("pic2.jpg", responseData.get(userId2).getProfilePicture());

        // Verify the service method was called
        verify(profileService, times(1)).getUsersBatch(userIds);
    }

    @Test
    @DisplayName("Request with empty user ID list returns empty map from service")
    void getUsersBatch_emptyList_returnsEmptyMap() {
        // Arrange
        List<UUID> emptyList = Collections.emptyList();
        when(profileService.getUsersBatch(emptyList)).thenReturn(Collections.emptyMap());

        // Act
        ResponseEntity<ApiResponse<Map<UUID, PostedByData>>> response = profileController.getUsersBatch(emptyList);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ApiResponse<Map<UUID, PostedByData>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertEquals(ApiConstants.STATUS_SUCCESS, apiResponse.getStatus());
        assertEquals(ApiConstants.MSG_USERS_BATCH, apiResponse.getMessage());
        
        Map<UUID, PostedByData> responseData = apiResponse.getData();
        assertThat(responseData, aMapWithSize(0));

        // Verify service method was called with the empty list
        verify(profileService, times(1)).getUsersBatch(emptyList);
    }

    @Test
    @DisplayName("Request with null user ID list returns empty map from service")
    void getUsersBatch_nullList_returnsEmptyMap() {
        // Arrange
        when(profileService.getUsersBatch(isNull())).thenReturn(Collections.emptyMap());

        // Act
        ResponseEntity<ApiResponse<Map<UUID, PostedByData>>> response = profileController.getUsersBatch(null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ApiResponse<Map<UUID, PostedByData>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertEquals(ApiConstants.STATUS_SUCCESS, apiResponse.getStatus());
        assertEquals(ApiConstants.MSG_USERS_BATCH, apiResponse.getMessage());
        
        Map<UUID, PostedByData> responseData = apiResponse.getData();
        assertThat(responseData, aMapWithSize(0));

        // Verify service method was called with null
        verify(profileService, times(1)).getUsersBatch(isNull());
    }

    @Test
    @DisplayName("Service returns partial results (some IDs not found)")
    void getUsersBatch_partialResults_returnsPartialMap() {
        // Arrange
        UUID userId3 = UUID.randomUUID();
        List<UUID> requestedIds = Arrays.asList(userId1, userId3);

        // Simulate service returning only profile for user1 in a map
        Map<UUID, PostedByData> partialMap = Map.of(userId1, profile1);
        when(profileService.getUsersBatch(requestedIds)).thenReturn(partialMap);

        // Act
        ResponseEntity<ApiResponse<Map<UUID, PostedByData>>> response = profileController.getUsersBatch(requestedIds);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ApiResponse<Map<UUID, PostedByData>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertEquals(ApiConstants.STATUS_SUCCESS, apiResponse.getStatus());
        assertEquals(ApiConstants.MSG_USERS_BATCH, apiResponse.getMessage());
        
        Map<UUID, PostedByData> responseData = apiResponse.getData();
        assertThat(responseData, aMapWithSize(1));
        assertThat(responseData, hasEntry(is(userId1), is(profile1)));
        assertThat(responseData, not(hasKey(userId3)));

        // Verify the service method was called
        verify(profileService, times(1)).getUsersBatch(requestedIds);
    }

    @Test
    @DisplayName("Service returns empty map (no IDs found)")
    void getUsersBatch_noResultsFromService_returnsEmptyMap() {
        // Arrange
        when(profileService.getUsersBatch(userIds)).thenReturn(Collections.emptyMap());

        // Act
        ResponseEntity<ApiResponse<Map<UUID, PostedByData>>> response = profileController.getUsersBatch(userIds);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ApiResponse<Map<UUID, PostedByData>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertEquals(ApiConstants.STATUS_SUCCESS, apiResponse.getStatus());
        assertEquals(ApiConstants.MSG_USERS_BATCH, apiResponse.getMessage());
        
        Map<UUID, PostedByData> responseData = apiResponse.getData();
        assertThat(responseData, aMapWithSize(0));

        // Verify the service method was called
        verify(profileService, times(1)).getUsersBatch(userIds);
    }
}
