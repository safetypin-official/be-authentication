package com.safetypin.authentication.service;

import com.safetypin.authentication.repository.FollowRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class LocationServiceTestConfig {
    
    @Bean
    @Primary
    public FollowRepository followRepository() {
        return Mockito.mock(FollowRepository.class);
    }
}