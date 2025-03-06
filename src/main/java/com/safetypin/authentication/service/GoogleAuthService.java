package com.safetypin.authentication.service;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.safetypin.authentication.dto.GoogleAuthDTO;
import com.safetypin.authentication.exception.ApiException;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.UserAlreadyExistsException;
import com.safetypin.authentication.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.*;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.Year;
import java.util.Collections;
import java.util.Optional;

@Service
public class GoogleAuthService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthService.class);

    private final UserService userService;
    private final JwtService jwtService;

    @Value("${google.client.id:default}")
    private String googleClientId;

    @Value("${google.client.secret:default}")
    private String googleClientSecret;

    private static final String EMAIL_PROVIDER = "GOOGLE";
    private static final String PEOPLE_API_BASE_URL = "https://people.googleapis.com/v1/people/me";

    private static final String BIRTHDAY = "birthdays";

    public GoogleAuthService(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    public String authenticate(GoogleAuthDTO googleAuthDTO) throws ApiException {
        try {
            GoogleIdToken.Payload payload = verifyIdToken(googleAuthDTO.getIdToken());
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            Optional<User> existingUser = userService.findByEmail(email);
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                String userProvider = user.getProvider();
                if (!EMAIL_PROVIDER.equals(userProvider)) {
                    throw new UserAlreadyExistsException("An account with this email exists. Please sign in using " + userProvider);
                }
                return jwtService.generateToken(user.getId());
            }

            String accessToken = getAccessToken(googleAuthDTO.getServerAuthCode());
            LocalDate userBirthdate = getUserBirthdate(accessToken);

            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setPassword(null);
            newUser.setProvider(EMAIL_PROVIDER);
            newUser.setVerified(true);
            newUser.setRole("USER");
            newUser.setBirthdate(userBirthdate);

            User user = userService.save(newUser);
            logger.info("New user registered via Google authentication: {}", email);

            return jwtService.generateToken(user.getId());
        } catch (UserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Google authentication failed: {}", e.getMessage());
            throw new ApiException("Authentication failed");
        }
    }

    GoogleIdTokenVerifier createIdTokenVerifier() {
        return new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    GoogleAuthorizationCodeTokenRequest createAuthorizationCodeTokenRequest(String serverAuthCode) {
        return new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                "https://oauth2.googleapis.com/token",
                googleClientId,
                googleClientSecret,
                serverAuthCode,
                "");
    }

    GoogleIdToken.Payload verifyIdToken(String idTokenString) throws IllegalArgumentException, InvalidCredentialsException, GeneralSecurityException, IOException {
        if (idTokenString == null) {
            throw new IllegalArgumentException("ID Token cannot be null");
        }

        GoogleIdTokenVerifier verifier = createIdTokenVerifier();
        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null) {
            throw new InvalidCredentialsException("Invalid ID Token");
        }
        return idToken.getPayload();
    }

    protected URL createURL(String urlString) throws IOException {
        URI uri = URI.create(urlString);
        return uri.toURL();
    }

    String getAccessToken(String serverAuthCode) throws IOException {
        TokenResponse tokenResponse = createAuthorizationCodeTokenRequest(serverAuthCode)
                .execute();

        return tokenResponse.getAccessToken();
    }

    String fetchUserData(String accessToken) {
        try {
            String apiUrl = PEOPLE_API_BASE_URL + "?personFields=" + GoogleAuthService.BIRTHDAY;
            URL url = createURL(apiUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return readResponse(conn.getInputStream());
                } else {
                    logger.error("Error fetching data from Google API: HTTP {}", responseCode);
                    return null;
                }
            } finally {
                conn.disconnect();
            }
        } catch (MalformedURLException e) {
            logger.error("Invalid API URL", e);
            return null;
        } catch (IOException e) {
            logger.error("IO error when fetching user data", e);
            return null;
        }
    }


    private String readResponse(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    LocalDate getUserBirthdate(String accessToken) {
        String response = fetchUserData(accessToken);
        return extractBirthday(response);
    }

    LocalDate extractBirthday(String jsonResponse) {
        JsonElement rootElement = JsonParser.parseString(jsonResponse);
        JsonObject rootObj = rootElement.getAsJsonObject();

        if (!rootObj.has(BIRTHDAY)) {
            return null;
        }

        JsonArray birthdaysArray = rootObj.getAsJsonArray(BIRTHDAY);
        if (birthdaysArray.isEmpty()) {
            return null;
        }

        JsonObject birthdayObj = birthdaysArray.get(0).getAsJsonObject();
        if (!birthdayObj.has("date")) {
            return null;
        }

        JsonObject dateObj = birthdayObj.getAsJsonObject("date");

        int year = dateObj.has("year") ? dateObj.get("year").getAsInt() : 0;
        int month = dateObj.get("month").getAsInt();
        int day = dateObj.get("day").getAsInt();

        if (year > 0) {
            return LocalDate.of(year, month, day);
        } else {
            return LocalDate.of(Year.now().getValue(), month, day);
        }
    }
}
