package com.safetypin.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class PostedByData {
    private UUID userId; // Changed from userId to id
    private String name;
    private String profilePicture; // URL or base64, depending on your system

}