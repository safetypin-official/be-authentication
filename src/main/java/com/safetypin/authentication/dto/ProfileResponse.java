package com.safetypin.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private UUID id;
    private String role;
    private boolean isVerified;
    private String name;
    private String instagram;
    private String twitter;
    private String line;
    private String tiktok;
    private String discord;
    private String profilePicture;
    private String profileBanner;
    private long followersCount;
    private long followingCount;
    private boolean isFollowing;
}
