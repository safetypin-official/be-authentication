package com.safetypin.authentication.dto;

import com.safetypin.authentication.model.User;
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
    private String name;
    private String profilePicture;
    private String profileBanner;


    public static ProfileResponse fromUser(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .isVerified(user.isVerified())
                .instagram(user.getInstagram())
                .twitter(user.getTwitter())
                .line(user.getLine())
                .tiktok(user.getTiktok())
                .discord(user.getDiscord())
                .name(user.getName())
                .profilePicture(user.getProfilePicture())
                .profileBanner(user.getProfileBanner())
                .build();
    }
}
