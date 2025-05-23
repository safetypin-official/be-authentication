package com.safetypin.authentication.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserPostResponse {
    private UUID userId;
    private String name;
    private String profilePicture;
    private String profileBanner;
}
