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
import com.safetypin.authentication.dto.AuthToken;
import com.safetypin.authentication.dto.GoogleAuthDTO;
import com.safetypin.authentication.exception.ApiException;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.UserAlreadyExistsException;
import com.safetypin.authentication.model.RefreshToken;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.util.Collections;
import java.util.Optional;

@Service
public class GoogleAuthService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthService.class);
    private static final String EMAIL_PROVIDER = "GOOGLE";
    private static final String PEOPLE_API_BASE_URL = "https://people.googleapis.com/v1/people/me";
    private static final String BIRTHDAY = "birthdays";

    private final UserService userService;
    private final JwtService jwtService;

    private final String httpsProxy;
    private final String httpProxy;

    private final RefreshTokenService refreshTokenService;

    @Value("${google.client.id:default}")
    private String googleClientId;
    @Value("${google.client.secret:default}")
    private String googleClientSecret;

    public GoogleAuthService(UserService userService, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;

        // Cache proxy settings
        this.httpsProxy = getEnv("HTTPS_PROXY");
        this.httpProxy = getEnv("HTTP_PROXY");

        logger.info("GoogleAuthService initialized - HTTPS Proxy: {}, HTTP Proxy: {}",
                this.httpsProxy, this.httpProxy);
    }

    public AuthToken authenticate(GoogleAuthDTO googleAuthDTO) throws ApiException {
        try {
            GoogleIdToken.Payload payload = verifyIdToken(googleAuthDTO.getIdToken());
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            Optional<User> existingUser = userService.findByEmail(email);
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                String userProvider = user.getProvider();
                if (!EMAIL_PROVIDER.equals(userProvider)) {
                    throw new UserAlreadyExistsException(
                            "An account with this email exists. Please sign in using " + userProvider);
                }
                String accessToken = jwtService.generateToken(user.getId());
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

                logger.info("User logged in with Google Auth at {}", java.time.LocalDateTime.now());
                return new AuthToken(user.getId(), accessToken, refreshToken.getToken());
            }

            String accessToken = getAccessToken(googleAuthDTO.getServerAuthCode());

            // Try retrieving the user's birthdate from Google API first
            LocalDate userBirthdate = getUserBirthdate(accessToken);
            // If the birthdate is not available, use the one provided in the request (if any)
            if (userBirthdate == null) {
                // Check if the birthdate is null
                if (googleAuthDTO.getBirthdate() == null) {
                    throw new IllegalArgumentException("Permission denied: Birthdate not provided");
                }
                userBirthdate = googleAuthDTO.getBirthdate();
            }

            if (Period.between(userBirthdate, LocalDate.now()).getYears() < 16)
                throw new IllegalArgumentException("User must be at least 16 years old");

            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setPassword(null);
            newUser.setProvider(EMAIL_PROVIDER);
            newUser.setVerified(true);
            newUser.setRole(Role.REGISTERED_USER);
            newUser.setBirthdate(userBirthdate);

            User user = userService.save(newUser);
            logger.info("New user registered via Google authentication: {}", email);

            String jwtAccessToken = jwtService.generateToken(user.getId());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

            return new AuthToken(user.getId(), jwtAccessToken, refreshToken.getToken());
        } catch (UserAlreadyExistsException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Google authentication failed: {}", e.getMessage());
            throw new ApiException("Authentication failed");
        }
    }

    GoogleIdTokenVerifier createIdTokenVerifier() {
        return new GoogleIdTokenVerifier.Builder(
                createNetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    GoogleAuthorizationCodeTokenRequest createAuthorizationCodeTokenRequest(String serverAuthCode) {
        return new GoogleAuthorizationCodeTokenRequest(
                createNetHttpTransport(),
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

    protected String getEnv(String name) {
        return System.getenv(name);
    }

    protected HttpURLConnection openDirectConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    protected HttpURLConnection openProxyConnection(URL url, java.net.Proxy proxy) throws IOException {
        return (HttpURLConnection) url.openConnection(proxy);
    }

    private NetHttpTransport createNetHttpTransport() {
        NetHttpTransport.Builder builder = new NetHttpTransport.Builder();

        // Simple approach: Use HTTPS proxy if available, otherwise direct connection
        if (httpsProxy != null && !httpsProxy.isEmpty()) {
            try {
                URL proxyUrl = createURL(httpsProxy);
                java.net.Proxy proxy = new java.net.Proxy(
                        java.net.Proxy.Type.HTTP,
                        new java.net.InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort())
                );
                logger.info("Using HTTPS proxy for Google API client: {}:{}", proxyUrl.getHost(), proxyUrl.getPort());
                builder.setProxy(proxy);
            } catch (IOException e) {
                logger.error("Invalid HTTPS proxy URL: {}. Using direct connection.", httpsProxy, e);
            }
        } else {
            logger.info("No HTTPS proxy configured. Using direct connection for Google API client.");
        }

        return builder.build();
    }

    String getAccessToken(String serverAuthCode) throws IOException {
        TokenResponse tokenResponse = createAuthorizationCodeTokenRequest(serverAuthCode)
                .execute();

        return tokenResponse.getAccessToken();
    }

    String fetchUserData(String accessToken) {
        try {
            String apiUrl = PEOPLE_API_BASE_URL + "?personFields=" + BIRTHDAY;
            URL url = createURL(apiUrl);
            logger.info("Attempting to connect to Google API: {}", apiUrl);

            HttpURLConnection conn = createConnectionWithProxy(url);
            return executeRequest(conn, accessToken);
        } catch (IOException e) {
            logger.error("IO error when fetching user data: {}", e.getMessage(), e);
            return null;
        }
    }

    private HttpURLConnection createConnectionWithProxy(URL url) throws IOException {
        // Try HTTPS proxy
        if (httpsProxy != null && !httpsProxy.isEmpty()) {
            HttpURLConnection conn = tryProxyConnection(url, httpsProxy, "HTTPS");
            if (conn != null) return conn;
        }

        // Try HTTP proxy
        if (httpProxy != null && !httpProxy.isEmpty()) {
            HttpURLConnection conn = tryProxyConnection(url, httpProxy, "HTTP");
            if (conn != null) return conn;
        }

        // Use direct connection
        logger.info("No proxy configured, using direct connection");
        return openDirectConnection(url);
    }

    private HttpURLConnection tryProxyConnection(URL url, String proxyUrl, String proxyType) throws IOException {
        try {
            URL parsedProxyUrl = createURL(proxyUrl);
            java.net.Proxy proxy = new java.net.Proxy(
                    java.net.Proxy.Type.HTTP,
                    new java.net.InetSocketAddress(parsedProxyUrl.getHost(), parsedProxyUrl.getPort())
            );
            logger.info("Using {} proxy: {}:{}", proxyType, parsedProxyUrl.getHost(), parsedProxyUrl.getPort());
            return openProxyConnection(url, proxy);
        } catch (MalformedURLException e) {
            logger.error("Invalid {} proxy URL, falling back to direct connection", proxyType, e);
            return null;
        }
    }

    private String executeRequest(HttpURLConnection conn, String accessToken) throws IOException {
        try {
            // Set connection properties
            conn.setConnectTimeout(10000); // 10 seconds
            conn.setReadTimeout(10000);    // 10 seconds
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Accept", "application/json");

            logger.info("Opening connection to Google API...");

            int responseCode = conn.getResponseCode();
            logger.info("Google API response code: {}", responseCode);

            return handleResponse(conn, responseCode);
        } finally {
            conn.disconnect();
        }
    }

    private String handleResponse(HttpURLConnection conn, int responseCode) throws IOException {
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = readResponse(conn.getInputStream());
            logger.info("Successfully received data from Google API");
            return response;
        } else {
            return handleErrorResponse(conn, responseCode);
        }
    }

    private String handleErrorResponse(HttpURLConnection conn, int responseCode) {
        String errorResponse = null;
        try {
            errorResponse = readResponse(conn.getErrorStream());
        } catch (Exception e) {
            logger.error("Could not read error response", e);
        }
        logger.error("Error fetching data from Google API: HTTP {}, Error: {}",
                responseCode, errorResponse);
        return null;
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

        for (JsonElement birthdayElement : birthdaysArray) {
            JsonObject birthdayObj = birthdayElement.getAsJsonObject();
            if (birthdayObj.has("metadata")) {
                JsonObject metadata = birthdayObj.getAsJsonObject("metadata");
                if (metadata.has("source")) {
                    JsonObject source = metadata.getAsJsonObject("source");
                    if ("ACCOUNT".equals(source.get("type").getAsString())) {
                        return parseDate(birthdayObj);
                    }
                }
            }
        }

        // Fallback to the first birthday if "ACCOUNT" type is not found
        return parseDate(birthdaysArray.get(0).getAsJsonObject());
    }

    LocalDate parseDate(JsonObject birthdayObj) {
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
