package com.safetypin.authentication.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProfileViewDTO {
    private UUID viewerUserId;
    private String name;
    private String profilePicture;
    private LocalDateTime viewedAt;
}
