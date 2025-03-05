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
        return new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    public GoogleIdToken.Payload verifyIdToken(String idTokenString) throws Exception {
        if (idTokenString == null) {
            throw new IllegalArgumentException("ID Token cannot be null");
        }

        GoogleIdTokenVerifier verifier = createIdTokenVerifier();
        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken == null) {
            throw new Exception("Invalid ID Token");
        }
        return idToken.getPayload();
    }

    protected GoogleAuthorizationCodeTokenRequest createTokenRequest(
            String tokenUrl, String clientId, String clientSecret) {
        return new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                tokenUrl,
                clientId,
                clientSecret,
                "",
                "");
    }

    public String getAccessToken(String serverAuthCode) throws IOException {
        TokenResponse tokenResponse = createTokenRequest(
                "https://oauth2.googleapis.com/token",
                googleClientId,
                googleClientSecret)
                .setCode(serverAuthCode)
                .execute();

        return tokenResponse.getAccessToken();
    }

    protected URL createURL(String urlString) throws IOException {
        return new URL(urlString);
    }

    public String fetchUserData(String accessToken, String personFields) throws IOException {
        String apiUrl = PEOPLE_API_BASE_URL + "?personFields=" + personFields;

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
                throw new ApiException("Error fetching data from Google API", responseCode);
            }
        } finally {
            conn.disconnect();
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

    public LocalDate getUserBirthdate(String accessToken) throws IOException {
        String response = fetchUserData(accessToken, "birthdays");
        return extractBirthday(response);
    }

    public String authenticate(GoogleAuthDTO googleAuthDTO) throws Exception {
        GoogleIdToken.Payload payload = verifyIdToken(googleAuthDTO.getIdToken());

        String email = payload.getEmail();
        String name = (String) payload.get("name");

        Optional<User> existingUser = Optional.ofNullable(super.getUserRepository().findByEmail(email));
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            String userProvider = user.getProvider();
            if (!EMAIL_PROVIDER.equals(userProvider)) {
                throw new UserAlreadyExistsException("An account with this email exists. Please sign in using " + userProvider);
            }
            return super.generateJwtToken(user.getId());
        }

        String accessToken = getAccessToken(googleAuthDTO.getServerAuthCode());
        LocalDate userBirthdate = getUserBirthdate(accessToken);

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setPassword(null);
        newUser.setProvider("GOOGLE");
        newUser.setVerified(true);
        newUser.setRole("USER");
        newUser.setBirthdate(userBirthdate);

        User user = super.getUserRepository().save(newUser);

        return super.generateJwtToken(user.getId());
    }

    LocalDate extractBirthday(String jsonResponse) {
        JsonElement rootElement = JsonParser.parseString(jsonResponse);
        JsonObject rootObj = rootElement.getAsJsonObject();

        if (!rootObj.has("birthdays")) {
            return null;
        }

        JsonArray birthdaysArray = rootObj.getAsJsonArray("birthdays");
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