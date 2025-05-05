package com.safetypin.authentication.constants;

public class ApiConstants {
    // Private constructor to prevent instantiation
    private ApiConstants() {
        // Utility class, do not instantiate
    }
    
    // Response status values
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";
    
    // Prefix constants
    public static final String BEARER_PREFIX = "Bearer ";
    
    // Common messages
    public static final String MSG_OPERATION_SUCCESS = "Operation completed successfully";
    public static final String MSG_PROFILE_RETRIEVED = "Profile retrieved successfully";
    public static final String MSG_PROFILE_UPDATED = "Profile updated successfully";
    public static final String MSG_PROFILE_VIEWS = "Profile views retrieved successfully";
    public static final String MSG_USERS_BATCH = "Users batch retrieved successfully";
    public static final String MSG_FOLLOWERS_RETRIEVED = "Followers retrieved successfully";
    public static final String MSG_FOLLOWING_RETRIEVED = "Following list retrieved successfully";
    public static final String MSG_FOLLOW_STATS = "Follow statistics retrieved successfully";
}