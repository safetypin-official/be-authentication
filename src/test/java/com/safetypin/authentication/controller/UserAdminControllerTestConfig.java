package com.safetypin.authentication.controller;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.safetypin.authentication.service.JwtService;
import com.safetypin.authentication.service.UserAdminService;

@Configuration
public class UserAdminControllerTestConfig {

    @Bean
    public UserAdminService userAdminService() {
        return mock(UserAdminService.class);
    }

    @Bean
    public JwtService jwtService() {
        return mock(JwtService.class);
    }
}
