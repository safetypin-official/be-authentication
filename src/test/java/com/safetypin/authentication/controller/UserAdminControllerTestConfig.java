package com.safetypin.authentication.controller;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import com.safetypin.authentication.repository.FollowRepository;
import com.safetypin.authentication.repository.ProfileViewRepository;
import com.safetypin.authentication.repository.RefreshTokenRepository;
import com.safetypin.authentication.repository.UserRepository;
import com.safetypin.authentication.service.JwtService;
import com.safetypin.authentication.service.UserAdminService;

@TestConfiguration
public class UserAdminControllerTestConfig {

    @Bean
    public UserAdminService userAdminService() {
        return Mockito.mock(UserAdminService.class);
    }

    @Bean
    public JwtService jwtService() {
        return Mockito.mock(JwtService.class);
    }

    @Bean
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    public RefreshTokenRepository refreshTokenRepository() {
        return Mockito.mock(RefreshTokenRepository.class);
    }

    @Bean
    public ProfileViewRepository profileViewRepository() {
        return Mockito.mock(ProfileViewRepository.class);
    }

    @Bean
    public FollowRepository followRepository() {
        return Mockito.mock(FollowRepository.class);
    }

    @Bean
    public RestTemplate restTemplate() {
        return Mockito.mock(RestTemplate.class);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/**").hasRole("MODERATOR")
                        .anyRequest().permitAll());
        return http.build();
    }
}
