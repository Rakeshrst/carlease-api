package com.rstontherun.iamservice.service.impl;

import com.rstontherun.iamservice.domain.AppUser;
import com.rstontherun.iamservice.domain.AuthRequest;
import com.rstontherun.iamservice.domain.Role;
import com.rstontherun.iamservice.entity.UserData;
import com.rstontherun.iamservice.exception.UserAlreadyExistsException;
import com.rstontherun.iamservice.model.AuthResponse;
import com.rstontherun.iamservice.model.UserRole;
import com.rstontherun.iamservice.repository.UserRepository;
import com.rstontherun.iamservice.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUser() {
        AppUser appUser = new AppUser();
        appUser.setName("Test User");
        appUser.setEmail("user@example.com");
        appUser.setPassword("password");
        appUser.setUserRole("Employee");

        UserData savedUser = UserData.builder()
                .name(appUser.getName())
                .email(appUser.getEmail())
                .password("encodedPassword")
                .userRole(Role.Employee)
                .build();

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserData.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("mockedToken");

        AuthResponse response = authService.registerUser(appUser);

        assertNotNull(response);
        assertEquals("mockedToken", response.getToken());
    }

    @Test
    public void testLogin_ValidUser() {
        AuthRequest authRequest = new AuthRequest("user@example.com", "password");
        UserData user = UserData.builder()
                .email(authRequest.getEmail())
                .password("encodedPassword")
                .userRole(Role.Employee)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(java.util.Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mockedToken");

        AuthResponse response = authService.login(authRequest);

        assertNotNull(response);
        assertEquals("mockedToken", response.getToken());
    }

    @Test
    public void testValidate_ValidToken() {
        String token = "validToken";
        String email = "user@example.com";

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUserName(token)).thenReturn(email);

        UserData user = UserData.builder()
                .email(email)
                .userRole(Role.Employee)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(user));

        UserRole userRole = authService.validate(token);

        assertNotNull(userRole);
        assertEquals(Role.Employee, userRole.getUserRole());
    }


    @Test
    public void testRegisterUser_UniqueConstraintViolation() {
        AppUser appUser = new AppUser();
        appUser.setName("Test User");
        appUser.setEmail("user@example.com");
        appUser.setPassword("password");
        appUser.setUserRole("Employee");

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserData.class))).thenThrow(DataIntegrityViolationException.class);

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerUser(appUser));
    }

    @Test
    public void testLogin_UserNotFound() {
        AuthRequest authRequest = new AuthRequest("nonexistent@example.com", "password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(java.util.Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(authRequest));
    }

    @Test
    public void testValidate_InvalidToken() {
        String invalidToken = "invalidToken";

        when(jwtService.isTokenValid(invalidToken)).thenReturn(false);

        assertNull(authService.validate(invalidToken));
    }
}
