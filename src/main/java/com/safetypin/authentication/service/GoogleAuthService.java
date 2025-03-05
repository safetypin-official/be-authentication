package com.safetypin.authentication.service;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.time.LocalDate;
import java.time.Year;
import java.util.Collections;
import java.util.Optional;

import com.safetypin.authentication.dto.GoogleAuthDTO;
import com.safetypin.authentication.exception.ApiException;
import com.safetypin.authentication.exception.UserAlreadyExistsException;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class GoogleAuthService extends AuthenticationService {

    @Value("${google.client.id:default}")
    private String googleClientId;
    @Value("${google.client.secret:default}")
    private String googleClientSecret;
    public static final String EMAIL_PROVIDER = "GMAIL";
    private static final String PEOPLE_API_BASE_URL = "https://people.googleapis.com/v1/people/me";

    public GoogleAuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, OTPService otpService) {
        super(userRepository, passwordEncoder, otpService);
    }

    protected GoogleIdTokenVerifier createIdTokenVerifier() {
        return null;
    }

    public GoogleIdToken.Payload verifyIdToken(String idTokenString) throws Exception {
        return null;
    }

    protected GoogleAuthorizationCodeTokenRequest createTokenRequest(
            String tokenUrl, String clientId, String clientSecret) {
        return null;
    }

    public String getAccessToken(String serverAuthCode) throws IOException {
        return null;
    }

    protected URL createURL(String urlString) throws IOException {
        return null;
    }

    public String fetchUserData(String accessToken, String personFields) throws IOException {
        return null;
    }

    private String readResponse(InputStream inputStream) throws IOException {
        return null;
    }

    public LocalDate getUserBirthdate(String accessToken) throws IOException {
        return null;
    }

    public String authenticate(GoogleAuthDTO googleAuthDTO) throws Exception {
        return null;
    }

    LocalDate extractBirthday(String jsonResponse) {
        return null;
    }
}
