package com.safetypin.authentication.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UpdateProfileRequest {
    private String instagram;
    private String twitter;
    private String line;
    private String tiktok;
    private String discord;
    private String profilePicture;
    private String profileBanner;
}
