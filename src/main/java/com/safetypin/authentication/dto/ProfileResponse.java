package com.safetypin.authentication.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Data
@Getter
@Setter
@Builder
public class ProfileResponse {
    private UUID id;
    private String role;
    private boolean isVerified;
    private String instagram;
    private String twitter;
    private String line;
    private String tiktok;
    private String discord;
}
