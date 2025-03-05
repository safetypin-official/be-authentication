package com.safetypin.authentication.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import com.safetypin.authentication.dto.GoogleAuthDTO;
import com.safetypin.authentication.exception.ApiException;
import com.safetypin.authentication.exception.UserAlreadyExistsException;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class GoogleAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OTPService otpService;

    @Mock
    private GoogleIdTokenVerifier mockVerifier;

    @Mock
    private GoogleIdToken mockIdToken;

    @Mock
    private GoogleIdToken.Payload mockPayload;

    @Mock
    private GoogleAuthorizationCodeTokenRequest mockTokenRequest;

    @Mock
    private TokenResponse mockTokenResponse;

    @Mock
    private HttpURLConnection mockConnection;

    @Mock
    private URL mockUrl;

    private GoogleAuthService googleAuthService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        googleAuthService = new GoogleAuthService(userRepository, passwordEncoder, otpService) {
            @Override
            protected GoogleIdTokenVerifier createIdTokenVerifier() {
                return mockVerifier;
            }

            @Override
            protected GoogleAuthorizationCodeTokenRequest createTokenRequest(
                    String tokenUrl, String clientId, String clientSecret) {
                return mockTokenRequest;
            }

            @Override
            protected URL createURL(String urlString) throws IOException {
                return mockUrl;
            }
        };

        // Use reflection to set private fields for testing
        setPrivateField("googleClientId", "test-client-id");
        setPrivateField("googleClientSecret", "test-client-secret");
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = GoogleAuthService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(googleAuthService, value);
    }

    private void setPrivateField(Object instance, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }


    @Test
    public void testVerifyIdToken_NullToken() {
        assertThrows(IllegalArgumentException.class, () -> {
            googleAuthService.verifyIdToken(null);
        });
    }

    @Test
    void testVerifyIdToken_ValidToken() throws Exception {
        // Prepare mock GoogleIdToken
        GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload mockPayload = new GoogleIdToken.Payload();
        mockPayload.setEmail("test@example.com");

        // Prepare a spy to inject mock verifier
        GoogleAuthService spyService = spy(googleAuthService);
        doReturn(mockVerifier).when(spyService).createIdTokenVerifier();

        when(mockVerifier.verify(anyString())).thenReturn(mockIdToken);
        when(mockIdToken.getPayload()).thenReturn(mockPayload);

        GoogleIdToken.Payload result = spyService.verifyIdToken("valid-token");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testVerifyIdToken_invalidIdToken() throws Exception {
        // Prepare a spy for GoogleAuthService
        GoogleAuthService spyService = spy(googleAuthService);

        // Mock the GoogleIdTokenVerifier and GoogleIdToken
        GoogleIdTokenVerifier mockVerifier = mock(GoogleIdTokenVerifier.class);
        GoogleIdToken mockIdToken = mock(GoogleIdToken.class);

        // Set up the behavior for the mockVerifier to return null when verify is called
        when(mockVerifier.verify(anyString())).thenReturn(null);  // Simulating invalid ID token

        // Mock createIdTokenVerifier to return the mockVerifier
        doReturn(mockVerifier).when(spyService).createIdTokenVerifier();

        // Call the method and verify that the exception is thrown
        assertThrows(Exception.class, () -> spyService.verifyIdToken("invalid-token"));
    }

    @Test
    void testFetchUserData_Successful() throws Exception {
        // Create a test GoogleAuthService with a protected method for URL creation
        GoogleAuthService spyService = spy(new GoogleAuthService(userRepository, passwordEncoder, otpService) {
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
        String response = spyService.fetchUserData("access-token", "birthdays");
        assertEquals("test response data", response);
    }

    @Test
    void testFetchUserData_ErrorResponse() throws Exception {
        // Create a test GoogleAuthService with a protected method for URL creation
        GoogleAuthService spyService = spy(new GoogleAuthService(userRepository, passwordEncoder, otpService) {
            @Override
            protected URL createURL(String urlString) throws IOException {
                URL mockUrl = mock(URL.class);
                HttpURLConnection mockConn = mock(HttpURLConnection.class);

                // Mock URL and connection behaviors
                when(mockUrl.openConnection()).thenReturn(mockConn);
                when(mockConn.getResponseCode()).thenReturn(HttpURLConnection.HTTP_UNAUTHORIZED);

                return mockUrl;
            }
        });

        // Verify that an ApiException is thrown
        ApiException thrown = assertThrows(ApiException.class, () ->
                spyService.fetchUserData("access-token", "birthdays")
        );

        assertEquals("Error fetching data from Google API", thrown.getMessage());
    }

    @Test
    public void testFetchUserData_ApiError() throws IOException {
        when(mockUrl.openConnection()).thenReturn(mockConnection);
        when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_UNAUTHORIZED);

        assertThrows(Exception.class, () -> {
            googleAuthService.fetchUserData("access-token", "fields");
        });
    }

    @Test
    void testGetAccessToken_Successful() throws Exception {
        // Create a spy for the service
        GoogleAuthService spyService = spy(googleAuthService);

        // Mock the GoogleTokenResponse creation
        GoogleTokenResponse mockTokenResponse = mock(GoogleTokenResponse.class);
        when(mockTokenResponse.getAccessToken()).thenReturn("test-access-token");

        // Mock the GoogleAuthorizationCodeTokenRequest creation
        GoogleAuthorizationCodeTokenRequest mockRequest = mock(GoogleAuthorizationCodeTokenRequest.class);

        // Mock the behavior of the request
        doReturn(mockRequest).when(spyService).createTokenRequest(anyString(), anyString(), anyString());

        // Mock setCode() to return the mockRequest to simulate method chaining
        when(mockRequest.setCode(anyString())).thenReturn(mockRequest);  // Return mockRequest itself for chaining

        // Mock the execute() method to return the mocked token response
        when(mockRequest.execute()).thenReturn(mockTokenResponse);

        // Execute and verify
        String accessToken = spyService.getAccessToken("test-server-auth-code");
        assertEquals("test-access-token", accessToken);
    }

    @Test
    void testAuthenticate_NewUser() throws Exception {
        // Prepare test scenario
        GoogleIdToken.Payload payload = createMockPayload();
        GoogleAuthDTO authDTO = createMockAuthDTO();

        // Mock dependencies
        GoogleAuthService spyService = spy(googleAuthService);

        // Prepare mocking chain
        doReturn(payload).when(spyService).verifyIdToken(anyString());
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        doReturn("access-token").when(spyService).getAccessToken(anyString());
        doReturn(LocalDate.now()).when(spyService).getUserBirthdate(anyString());

        // Prepare saved user
        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        doReturn("jwt-token").when(spyService).generateJwtToken(any(UUID.class));

        // Execute and verify
        String token = spyService.authenticate(authDTO);
        assertEquals("jwt-token", token);
    }

    @Test
    public void testAuthenticate_ExistingUserSameProvider() throws Exception {
        // Prepare mock data
        GoogleAuthDTO authDTO = new GoogleAuthDTO();
        authDTO.setIdToken("test-id-token");
        authDTO.setServerAuthCode("test-server-auth-code");

        // Setup existing user with ID
        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setProvider("GMAIL");
        existingUser.setEmail("test@example.com");

        // Setup ID token verification
        lenient().when(mockVerifier.verify(anyString())).thenReturn(mockIdToken);
        lenient().when(mockIdToken.getPayload()).thenReturn(mockPayload);
        when(mockPayload.getEmail()).thenReturn("test@example.com");
        when(mockPayload.get("name")).thenReturn("Test User");

        // Mock user repository
        when(userRepository.findByEmail(anyString())).thenReturn(existingUser);

        // Prepare token response
        GoogleTokenResponse mockTokenResponse = mock(GoogleTokenResponse.class);

        // Authenticate
        String token = googleAuthService.authenticate(authDTO);

        // Verify
        assertNotNull(token);
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    public void testAuthenticate_ExistingUserDifferentProvider() throws Exception {
        // Prepare mock data
        GoogleAuthDTO authDTO = new GoogleAuthDTO();
        authDTO.setIdToken("test-id-token");
        authDTO.setServerAuthCode("test-server-auth-code");

        // Setup existing user
        User existingUser = new User();
        existingUser.setProvider("FACEBOOK");

        // Setup ID token verification
        when(mockVerifier.verify(anyString())).thenReturn(mockIdToken);
        when(mockIdToken.getPayload()).thenReturn(mockPayload);
        when(mockPayload.getEmail()).thenReturn("test@example.com");
        when(mockPayload.get("name")).thenReturn("Test User");

        // Mock user repository
        when(userRepository.findByEmail(anyString())).thenReturn(existingUser);

        // Verify exception
        assertThrows(UserAlreadyExistsException.class, () -> {
            googleAuthService.authenticate(authDTO);
        });
    }

    @Test
    void testExtractBirthday_FullBirthdayInfo() {
        String fullBirthdayJson = "{\"birthdays\":[{\"date\":{\"year\":1990,\"month\":1,\"day\":15}}]}";
        LocalDate birthday = googleAuthService.extractBirthday(fullBirthdayJson);
        assertEquals(LocalDate.of(1990, 1, 15), birthday);
    }

    @Test
    void testExtractBirthday_NoYear() {
        String noYearJson = "{\"birthdays\":[{\"date\":{\"month\":1,\"day\":15}}]}";
        LocalDate birthday = googleAuthService.extractBirthday(noYearJson);
        assertEquals(LocalDate.of(Year.now().getValue(), 1, 15), birthday);
    }

    @Test
    void testExtractBirthday_EmptyBirthdays() {
        String emptyBirthdaysJson = "{\"birthdays\":[]}";
        assertNull(googleAuthService.extractBirthday(emptyBirthdaysJson));
    }

    @Test
    public void testExtractBirthday_NoBirthdays() {
        String jsonResponse = "{\"key\":\"value\"}";
        LocalDate birthday = googleAuthService.extractBirthday(jsonResponse);

        assertNull(birthday);
    }

    @Test
    public void testExtractBirthday_EmptyBirthdaysArray() {
        String jsonResponse = "{\"birthdays\":[]}";
        LocalDate birthday = googleAuthService.extractBirthday(jsonResponse);

        assertNull(birthday);
    }

    @Test
    public void testExtractBirthday_NoBirthdayDateField() {
        // JSON response where the first birthday object doesn't have a "date" field
        String jsonResponse = "{\"birthdays\":[{\"someOtherField\":\"value\"}]}";

        LocalDate birthday = googleAuthService.extractBirthday(jsonResponse);

        assertNull(birthday);
    }

    @Test
    void testCreateURL_Successful() throws Exception {
        GoogleAuthService realService = new GoogleAuthService(userRepository, passwordEncoder, otpService);
        setPrivateField(realService, "googleClientId", "test-client-id");
        setPrivateField(realService, "googleClientSecret", "test-client-secret");

        String testUrlString = "https://example.com";
        URL createdUrl = realService.createURL(testUrlString);

        assertNotNull(createdUrl);
        assertEquals(testUrlString, createdUrl.toString());
    }

    @Test
    void testCreateURL_InvalidURL() throws Exception {
        GoogleAuthService realService = new GoogleAuthService(userRepository, passwordEncoder, otpService);
        setPrivateField(realService, "googleClientId", "test-client-id");
        setPrivateField(realService, "googleClientSecret", "test-client-secret");

        assertThrows(MalformedURLException.class, () -> {
            realService.createURL("not a valid url");
        });
    }

    @Test
    void testGetUserBirthdate_Successful() throws IOException {
        // Create a spy of GoogleAuthService to mock the fetchUserData method
        GoogleAuthService spyService = spy(googleAuthService);

        // Prepare a mock JSON response with a birthday
        String mockBirthdayResponse = "{\"birthdays\":[{\"date\":{\"year\":1990,\"month\":1,\"day\":15}}]}";

        // Mock the fetchUserData method to return the prepared response
        doReturn(mockBirthdayResponse).when(spyService).fetchUserData(anyString(), eq("birthdays"));

        // Execute the method
        LocalDate birthday = spyService.getUserBirthdate("test-access-token");

        // Verify the result
        assertEquals(LocalDate.of(1990, 1, 15), birthday);

        // Verify that fetchUserData was called with correct parameters
        verify(spyService).fetchUserData("test-access-token", "birthdays");
    }

    @Test
    void testGetUserBirthdate_NoBirthdayData() throws IOException {
        // Create a spy of GoogleAuthService to mock the fetchUserData method
        GoogleAuthService spyService = spy(googleAuthService);

        // Prepare a mock JSON response without birthdays
        String mockEmptyResponse = "{\"key\":\"value\"}";

        // Mock the fetchUserData method to return the prepared response
        doReturn(mockEmptyResponse).when(spyService).fetchUserData(anyString(), eq("birthdays"));

        // Execute the method
        LocalDate birthday = spyService.getUserBirthdate("test-access-token");

        // Verify the result is null
        assertNull(birthday);

        // Verify that fetchUserData was called with correct parameters
        verify(spyService).fetchUserData("test-access-token", "birthdays");
    }

    // Helper methods for creating mock objects
    private GoogleIdToken.Payload createMockPayload() {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("test@example.com");
        payload.put("name", "Test User");
        return payload;
    }

    private GoogleAuthDTO createMockAuthDTO() {
        GoogleAuthDTO authDTO = new GoogleAuthDTO();
        authDTO.setIdToken("valid-token");
        authDTO.setServerAuthCode("server-code");
        return authDTO;
    }

    @Test
    void testGetAccessToken_WithOverriddenCreateTokenRequest() throws Exception {
        // Override method createTokenRequest untuk mengembalikan stub yang sudah dikonfigurasi
        GoogleAuthService spyService = spy(new GoogleAuthService(userRepository, passwordEncoder, otpService) {
            @Override
            protected GoogleAuthorizationCodeTokenRequest createTokenRequest(String tokenUrl, String clientId, String clientSecret) {
                GoogleAuthorizationCodeTokenRequest mockRequest = mock(GoogleAuthorizationCodeTokenRequest.class);
                // Gunakan GoogleTokenResponse agar sesuai dengan tipe kembalian execute()
                GoogleTokenResponse tokenResponse = new GoogleTokenResponse();
                tokenResponse.setAccessToken("test-access-token");

                try {
                    // Pastikan pemanggilan setCode() dan execute() mengembalikan nilai yang sesuai
                    when(mockRequest.setCode(anyString())).thenReturn(mockRequest);
                    when(mockRequest.execute()).thenReturn(tokenResponse);
                } catch (IOException e) {
                    // Tidak diharapkan terjadi exception
                    fail("IOException tidak diharapkan: " + e.getMessage());
                }
                return mockRequest;
            }
        });

        String accessToken = spyService.getAccessToken("dummy-code");
        assertEquals("test-access-token", accessToken);
    }

    @Test
    void testCreateIdTokenVerifier_Configurations() throws Exception {
        // Create a real instance of GoogleAuthService for this test
        GoogleAuthService realService = new GoogleAuthService(userRepository, passwordEncoder, otpService);

        // Use reflection to set the client ID for testing
        setPrivateField(realService, "googleClientId", "test-client-id");

        // Call the createIdTokenVerifier method
        GoogleIdTokenVerifier verifier = realService.createIdTokenVerifier();

        // Assertions to verify the verifier's configuration
        assertNotNull(verifier, "Verifier should not be null");
    }

    @Test
    void testCreateTokenRequests_Configurations() throws Exception {
        // Create a real instance of GoogleAuthService for this test
        GoogleAuthService realService = new GoogleAuthService(userRepository, passwordEncoder, otpService);

        // Use reflection to set the client ID for testing
        setPrivateField(realService, "googleClientId", "test-client-id");

        // Call the createIdTokenVerifier method
        assertThrows(TokenResponseException.class, () -> {
            realService.createTokenRequest("https://oauth2.googleapis.com/token", "test-client-id", "test-client-secret").setCode("sadf").execute();
        });
    }

}