package com.safetypin.authentication.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PostedByData {
    private UUID userId; // Changed from userId to id
    private String name;
    private String profilePicture; // URL or base64, depending on your system

}