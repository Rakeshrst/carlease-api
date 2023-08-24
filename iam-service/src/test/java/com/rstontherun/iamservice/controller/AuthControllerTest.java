package com.rstontherun.iamservice.controller;

import com.rstontherun.iamservice.domain.AppUser;
import com.rstontherun.iamservice.domain.AuthRequest;
import com.rstontherun.iamservice.domain.Role;
import com.rstontherun.iamservice.model.AuthResponse;
import com.rstontherun.iamservice.model.UserRole;
import com.rstontherun.iamservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddNewUser() {
        AppUser appUser = new AppUser();
        AuthResponse expectedResponse = new AuthResponse();
        when(authService.registerUser(appUser)).thenReturn(expectedResponse);

        AuthResponse actualResponse = authController.addNewUser(appUser);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testAddExistingUser() {
        AppUser existingUser = new AppUser();
        existingUser.setEmail("user@example.com");

        when(authService.registerUser(existingUser))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "User already exists"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authController.addNewUser(existingUser)
        );

        assertEquals("User already exists", exception.getReason());
    }

    @Test
    public void testGetToken() {
        AuthRequest authRequest = new AuthRequest("user@example.com", "password");
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        AuthResponse expectedResponse = new AuthResponse(); // Create an example response
        when(authService.login(authRequest)).thenReturn(expectedResponse);

        AuthResponse actualResponse = authController.getToken(authRequest);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testValidateToken() {
        String authHeader = "Bearer token123";
        AuthRequest authRequest = new AuthRequest("user@example.com", "password");
        String token = "token123";
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authService.validate(token)).thenReturn(UserRole.builder().userRole(Role.Employee).build()); // Or your expected UserRole

        UserRole userRole = authController.validateToken(authHeader, authRequest);

        assertEquals(Role.Employee, userRole.getUserRole());
    }

    @Test
    public void testInvalidToken() {
        String authHeader = "Bearer invalid-token";
        AuthRequest authRequest = new AuthRequest("user@example.com", "password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(authService.validate(anyString())).thenThrow(new RuntimeException("invalid access"));

        assertThrows(RuntimeException.class, () -> authController.validateToken(authHeader, authRequest));
    }

    @ParameterizedTest
    @MethodSource("invalidAuthRequests")
    public void testInvalidToken(AuthRequest authRequest) {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(authService.validate(anyString())).thenThrow(new RuntimeException("invalid access"));

        assertThrows(RuntimeException.class, () -> authController.validateToken("Bearer token123", authRequest));
    }

    static Stream<AuthRequest> invalidAuthRequests() {
        return Stream.of(
                new AuthRequest("user@example.com", "wrong_password"),
                new AuthRequest("nonexistent@example.com", "password")
        );
    }
}
