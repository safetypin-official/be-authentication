package com.safetypin.authentication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetypin.authentication.dto.FriendLocationResponse;
import com.safetypin.authentication.dto.LocationRequest;
import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.service.JwtService;
import com.safetypin.authentication.service.LocationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocationService locationService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void updateLocation_ShouldReturnOk() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserResponse userResponse = UserResponse.builder()
                .id(userId)
                .name("Test User")
                .email("test@example.com")
                .build();
        
        String token = "test-token";
        String authHeader = "Bearer " + token;
        
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setLatitude(40.7128);
        locationRequest.setLongitude(-74.0060);
        locationRequest.setAccuracy(10.0);
        
        when(jwtService.getUserFromJwtToken(token)).thenReturn(userResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/locations/update")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Location updated successfully"));
        
        verify(locationService).updateLocation(eq(userId), any(LocationRequest.class));
    }

    @Test
    void getFriendsLocations_WhenFriendsExist_ShouldReturnList() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserResponse userResponse = UserResponse.builder()
                .id(userId)
                .name("Test User")
                .email("test@example.com")
                .build();
        
        String token = "test-token";
        String authHeader = "Bearer " + token;
        
        UUID friend1Id = UUID.randomUUID();
        FriendLocationResponse friend1 = FriendLocationResponse.builder()
                .id(friend1Id)
                .name("Friend One")
                .profilePicture("friend1.jpg")
                .latitude(34.0522)
                .longitude(-118.2437)
                .updatedAt(LocalDateTime.now())
                .build();
        
        List<FriendLocationResponse> friendLocations = Arrays.asList(friend1);
        
        when(jwtService.getUserFromJwtToken(token)).thenReturn(userResponse);
        when(locationService.getFriendsLocations(userId)).thenReturn(friendLocations);
        
        // Act & Assert
        mockMvc.perform(get("/api/locations/friends")
                .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Friend One"));
        
        verify(locationService).getFriendsLocations(userId);
    }

    @Test
    void getFriendsLocations_WhenNoFriends_ShouldReturnEmptyList() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserResponse userResponse = UserResponse.builder()
                .id(userId)
                .name("Test User")
                .email("test@example.com")
                .build();
        
        String token = "test-token";
        String authHeader = "Bearer " + token;
        
        when(jwtService.getUserFromJwtToken(token)).thenReturn(userResponse);
        when(locationService.getFriendsLocations(userId)).thenReturn(Collections.emptyList());
        
        // Act & Assert
        mockMvc.perform(get("/api/locations/friends")
                .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
        
        verify(locationService).getFriendsLocations(userId);
    }
}