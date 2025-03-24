package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    private static final long EXPIRATION_TIME = 1000L * 60 * 10; // 1000 milliseconds * 60 seconds * 10 minutes

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final UserService userService;

    @Autowired
    public JwtService(KeyPair rsaKeyPair, UserService userService) {
        this.privateKey = rsaKeyPair.getPrivate();
        this.publicKey = rsaKeyPair.getPublic();
        this.userService = userService;
    }

    public String generateToken(UUID userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("name", user.getName());
        claims.put("isVerified", user.isVerified());
        claims.put("role", user.getRole().toString());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(privateKey, SignatureAlgorithm.RS256)  // Fixed: Changed order of params
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
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
