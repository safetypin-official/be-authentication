package com.safetypin.authentication.controller;

import com.safetypin.authentication.dto.PostedByData;
import com.safetypin.authentication.service.JwtService;
import com.safetypin.authentication.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileController Batch Endpoint Unit Tests")
class ProfileControllerBatchEndpointTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private JwtService jwtService;

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
        Map<UUID, PostedByData> responseMap = profileController.getUsersBatch(userIds);

        // Assert
        assertNotNull(responseMap);
        assertThat(responseMap, aMapWithSize(2));
        assertThat(responseMap, hasEntry(is(userId1), is(profile1)));
        assertThat(responseMap, hasEntry(is(userId2), is(profile2)));
        assertEquals("User One", responseMap.get(userId1).getName());
        assertEquals("pic2.jpg", responseMap.get(userId2).getProfilePicture());

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
        Map<UUID, PostedByData> responseMap = profileController.getUsersBatch(emptyList);

        // Assert
        assertNotNull(responseMap);
        assertThat(responseMap, aMapWithSize(0));

        // Verify service method was called with the empty list
        verify(profileService, times(1)).getUsersBatch(emptyList);
    }

    @Test
    @DisplayName("Request with null user ID list returns empty map from service")
    void getUsersBatch_nullList_returnsEmptyMap() {
        // Arrange
        when(profileService.getUsersBatch(isNull())).thenReturn(Collections.emptyMap());

        // Act
        Map<UUID, PostedByData> responseMap = profileController.getUsersBatch(null);

        // Assert
        assertNotNull(responseMap);
        assertThat(responseMap, aMapWithSize(0));

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
        Map<UUID, PostedByData> responseMap = profileController.getUsersBatch(requestedIds);

        // Assert
        assertNotNull(responseMap);
        assertThat(responseMap, aMapWithSize(1));
        assertThat(responseMap, hasEntry(is(userId1), is(profile1)));
        assertThat(responseMap, not(hasKey(userId3)));

        // Verify the service method was called
        verify(profileService, times(1)).getUsersBatch(requestedIds);
    }

    @Test
    @DisplayName("Service returns empty map (no IDs found)")
    void getUsersBatch_noResultsFromService_returnsEmptyMap() {
        // Arrange
        when(profileService.getUsersBatch(userIds)).thenReturn(Collections.emptyMap());

        // Act
        Map<UUID, PostedByData> responseMap = profileController.getUsersBatch(userIds);

        // Assert
        assertNotNull(responseMap);
        assertThat(responseMap, aMapWithSize(0));

        // Verify the service method was called
        verify(profileService, times(1)).getUsersBatch(userIds);
    }
}
