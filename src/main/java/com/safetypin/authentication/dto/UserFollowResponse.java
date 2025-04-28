package com.safetypin.authentication.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserFollowResponse {
    private UUID userId;
    private String name;
    private String profilePicture;
    private boolean isFollowing; // Indicates if the current user is following this user
}
