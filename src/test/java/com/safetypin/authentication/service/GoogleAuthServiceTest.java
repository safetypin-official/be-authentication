package com.safetypin.authentication.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.safetypin.authentication.dto.GoogleAuthDTO;
import com.safetypin.authentication.exception.ApiException;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.UserAlreadyExistsException;
import com.safetypin.authentication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoogleAuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private GoogleIdToken idToken;

    @Mock
    private GoogleIdToken.Payload payload;

    @Mock
    private GoogleIdTokenVerifier verifier;

    @Mock
    private GoogleAuthorizationCodeTokenRequest tokenRequest;

    @Mock
    private GoogleTokenResponse tokenResponse;

    @Spy
    @InjectMocks
    private GoogleAuthService googleAuthService;

    private GoogleAuthDTO googleAuthDTO;
    private UUID testUserId;

    private final String TEST_ACCESS_TOKEN = "test-access-token";

    private final String TEST_GOOGLE_CLIENT_ID = "test-client-id";

    private final String TEST_ID_TOKEN = "test-id-token";

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(googleAuthService, "googleClientId", TEST_GOOGLE_CLIENT_ID);
        String TEST_GOOGLE_CLIENT_SECRET = "test-client-secret";
        ReflectionTestUtils.setField(googleAuthService, "googleClientSecret", TEST_GOOGLE_CLIENT_SECRET);

        googleAuthDTO = new GoogleAuthDTO();
        googleAuthDTO.setIdToken(TEST_ID_TOKEN);
        googleAuthDTO.setServerAuthCode("test-auth-code");

        testUserId = UUID.randomUUID();
    }

    private void setPrivateField(Object instance, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }

    @Test
    void authenticate_NewUser_Success() throws Exception {
        // Mock verify ID token
        doReturn(payload).when(googleAuthService).verifyIdToken(anyString());
        when(payload.getEmail()).thenReturn("test@example.com");
        when(payload.get("name")).thenReturn("Test User");

        // Mock user service
        when(userService.findByEmail(anyString())).thenReturn(Optional.empty());

        // Mock getAccessToken
        doReturn(TEST_ACCESS_TOKEN).when(googleAuthService).getAccessToken(anyString());

        // Mock getUserBirthdate to return a specific date
        LocalDate birthdate = LocalDate.of(1990, 1, 1);
        doReturn(birthdate).when(googleAuthService).getUserBirthdate(anyString());

        // Mock user save
        User savedUser = new User();
        savedUser.setId(testUserId);
        when(userService.save(any(User.class))).thenReturn(savedUser);

        // Mock JWT generation
        when(jwtService.generateToken(any(UUID.class))).thenReturn("test-jwt-token");

        // Execute
        String result = googleAuthService.authenticate(googleAuthDTO);

        // Verify
        assertEquals("test-jwt-token", result);
        verify(userService).findByEmail("test@example.com");
        verify(userService).save(any(User.class));
        verify(jwtService).generateToken(testUserId);
    }

    @Test
    void authenticate_ExistingGoogleUser_Success() throws Exception {
        // Mock verify ID token
        doReturn(payload).when(googleAuthService).verifyIdToken(anyString());
        when(payload.getEmail()).thenReturn("test@example.com");

        // Mock existing user
        User existingUser = new User();
        existingUser.setId(testUserId);
        existingUser.setProvider("GOOGLE");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        // Mock JWT generation
        when(jwtService.generateToken(any(UUID.class))).thenReturn("test-jwt-token");

        // Execute
        String result = googleAuthService.authenticate(googleAuthDTO);

        // Verify
        assertEquals("test-jwt-token", result);
        verify(userService).findByEmail("test@example.com");
        verify(userService, never()).save(any(User.class));
        verify(jwtService).generateToken(testUserId);
    }

    @Test
    void authenticate_ExistingUserWithDifferentProvider_ThrowsException() throws Exception {
        // Mock verify ID token
        doReturn(payload).when(googleAuthService).verifyIdToken(anyString());
        when(payload.getEmail()).thenReturn("test@example.com");

        // Mock existing user with different provider
        User existingUser = new User();
        existingUser.setProvider("EMAIL");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        // Execute and verify
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> googleAuthService.authenticate(googleAuthDTO)
        );

        assertTrue(exception.getMessage().contains("Please sign in using EMAIL"));
    }

    @Test
    void verifyIdToken_NullToken_ThrowsException() {
        // Execute and verify
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> googleAuthService.verifyIdToken(null)
        );

        assertEquals("ID Token cannot be null", exception.getMessage());
    }

    @Test
    void verifyIdToken_ValidToken_ReturnsPayload() throws Exception {
        // Mock the verifier creation
        doReturn(verifier).when(googleAuthService).createIdTokenVerifier();

        // Set up the verifier to return our mock ID token
        when(verifier.verify(TEST_ID_TOKEN)).thenReturn(idToken);
        when(idToken.getPayload()).thenReturn(payload);

        // Execute
        GoogleIdToken.Payload result = googleAuthService.verifyIdToken(TEST_ID_TOKEN);

        // Verify
        assertSame(payload, result);
        verify(verifier).verify(TEST_ID_TOKEN);
    }

    @Test
    void verifyIdToken_InvalidToken_ThrowsException() throws Exception {
        // Create a spy on the service to override the verifier creation

        // Mock the verifier creation
        doReturn(verifier).when(googleAuthService).createIdTokenVerifier();

        // Set up the verifier to return null (invalid token)
        when(verifier.verify(anyString())).thenReturn(null);

        // Execute and verify
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> googleAuthService.verifyIdToken(TEST_ID_TOKEN)
        );

        assertEquals("Invalid ID Token", exception.getMessage());
        verify(verifier).verify(TEST_ID_TOKEN);
    }

    @Test
    void getAccessToken_Success() throws Exception {

        // Mock the token request creation
        doReturn(tokenRequest).when(googleAuthService).createAuthorizationCodeTokenRequest("test-auth-code");

        // Set up the token request to return our mock token response
        when(tokenRequest.execute()).thenReturn((tokenResponse));
        when(tokenResponse.getAccessToken()).thenReturn(TEST_ACCESS_TOKEN);

        // Execute
        String result = googleAuthService.getAccessToken("test-auth-code");

        // Verify
        assertEquals(TEST_ACCESS_TOKEN, result);
        verify(tokenRequest).execute();
    }

    @Test
    void extractBirthday_ValidResponse_ReturnsBirthdate() {
        String jsonResponse = "{"
                + "\"birthdays\": ["
                + "  {"
                + "    \"date\": {"
                + "      \"year\": 1990,"
                + "      \"month\": 1,"
                + "      \"day\": 15"
                + "    }"
                + "  }"
                + "]"
                + "}";

        LocalDate result = googleAuthService.extractBirthday(jsonResponse);

        assertEquals(LocalDate.of(1990, 1, 15), result);
    }

    @Test
    void extractBirthday_NoYearProvided_ReturnsCurrentYear() {
        String jsonResponse = "{"
                + "\"birthdays\": ["
                + "  {"
                + "    \"date\": {"
                + "      \"month\": 1,"
                + "      \"day\": 15"
                + "    }"
                + "  }"
                + "]"
                + "}";

        LocalDate result = googleAuthService.extractBirthday(jsonResponse);

        assertEquals(LocalDate.of(LocalDate.now().getYear(), 1, 15), result);
    }

    @Test
    void extractBirthday_NoBirthdayField_ReturnsNull() {
        String jsonResponse = "{}";

        LocalDate result = googleAuthService.extractBirthday(jsonResponse);

        assertNull(result);
    }

    @Test
    void extractBirthday_EmptyBirthdaysArray_ReturnsNull() {
        String jsonResponse = "{\"birthdays\": []}";

        LocalDate result = googleAuthService.extractBirthday(jsonResponse);

        assertNull(result);
    }

    @Test
    void extractBirthday_NoDateField_ReturnsNull() {
        String jsonResponse = "{"
                + "\"birthdays\": ["
                + "  {}"
                + "]"
                + "}";

        LocalDate result = googleAuthService.extractBirthday(jsonResponse);

        assertNull(result);
    }

    @Test
    void getUserBirthdate_Success() throws Exception {
        // Setup private method mocking
        doReturn("test-json-response").when(googleAuthService).fetchUserData(anyString());

        // Mock extract birthday
        LocalDate birthdate = LocalDate.of(1990, 1, 15);
        doReturn(birthdate).when(googleAuthService).extractBirthday(anyString());

        // Execute
        LocalDate result = googleAuthService.getUserBirthdate(TEST_ACCESS_TOKEN);

        // Verify
        assertEquals(birthdate, result);
    }

    @Test
    void authenticate_Exception_ThrowsApiException() throws Exception {
        // Mock verify ID token to throw exception
        doThrow(new IOException("Test exception")).when(googleAuthService).verifyIdToken(anyString());

        // Execute and verify
        ApiException exception = assertThrows(
                ApiException.class,
                () -> googleAuthService.authenticate(googleAuthDTO)
        );

        assertEquals("Authentication failed", exception.getMessage());
    }

    @Test
    void testCreateIdTokenVerifier_Configurations() throws Exception {
        // Create a real instance of GoogleAuthService for this test
        GoogleAuthService realService = new GoogleAuthService(userService, jwtService);

        // Use reflection to set the client ID for testing
        setPrivateField(realService, "googleClientId", TEST_GOOGLE_CLIENT_ID);

        // Call the createIdTokenVerifier method
        GoogleIdTokenVerifier verifier = realService.createIdTokenVerifier();

        // Assertions to verify the verifier's configuration
        assertNotNull(verifier, "Verifier should not be null");
    }

    @Test
    void testCreateTokenRequests_Configurations() throws Exception {
        // Create a real instance of GoogleAuthService for this test
        GoogleAuthService realService = new GoogleAuthService(userService, jwtService);

        // Use reflection to set the client ID for testing
        setPrivateField(realService, "googleClientId", TEST_GOOGLE_CLIENT_ID);

        assertNotNull(realService.createAuthorizationCodeTokenRequest("dumb-bunny"));
    }

    @Test
    void testFetchUserData_Successful() throws Exception {
        // Create a test GoogleAuthService with a protected method for URL creation
        GoogleAuthService spyService = spy(new GoogleAuthService(userService, jwtService) {
            @Override
            protected URL createURL(String urlString) throws IOException {
                URL mockUrl = mock(URL.class);
                HttpURLConnection mockConn = mock(HttpURLConnection.class);

                // Prepare input stream
                String testResponse = "test response data";
                InputStream inputStream = new ByteArrayInputStream(testResponse.getBytes());

                // Mock URL and connection behaviors
                when(mockUrl.openConnection()).thenReturn(mockConn);
                when(mockConn.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
                when(mockConn.getInputStream()).thenReturn(inputStream);

                return mockUrl;
            }
        });

        // Execute and verify
        String response = spyService.fetchUserData(TEST_ACCESS_TOKEN);
        assertEquals("test response data", response);
    }

    @Test
    void testFetchUserData_ApiError() {

        assertThrows(ApiException.class, () -> {
            googleAuthService.fetchUserData(TEST_ACCESS_TOKEN);
        });
    }

    @Test
    void testFetchUserData_InvalidAPIFormat() throws IOException {
        // Arrange
        doThrow(new MalformedURLException("Invalid URL format"))
                .when(googleAuthService).createURL(anyString());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> googleAuthService.fetchUserData(TEST_ACCESS_TOKEN)
        );

        // Verify the exception contains the expected message
        assert(exception.getMessage().contains("Invalid API URL"));
        assert(exception.getCause() instanceof MalformedURLException);
    }
}