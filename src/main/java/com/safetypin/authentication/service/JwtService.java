package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private static final long EXPIRATION_TIME = 86400000; // 1 day

    private final Key key;

    private final UserService userService;

    public JwtService(@Value("${jwt.secret:biggerboysandstolensweethearts}") String secretKey, UserService userService) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.userService = userService;
    }

    public String generateToken(UUID userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public UserResponse getUserFromJwtToken(String token) throws InvalidCredentialsException {
        Claims claims = parseToken(token);

        boolean isExpired = claims.getExpiration().before(new Date(System.currentTimeMillis()));
        UUID userId = UUID.fromString(claims.getSubject());

        if (isExpired) {
            throw new InvalidCredentialsException("Token expired");
        } else {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new InvalidCredentialsException("User not found"));

            return user.generateUserResponse();
        }
    }
}
