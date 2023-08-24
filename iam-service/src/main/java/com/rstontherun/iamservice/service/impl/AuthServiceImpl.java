package com.rstontherun.iamservice.service.impl;

import com.rstontherun.iamservice.domain.AppUser;
import com.rstontherun.iamservice.domain.AuthRequest;
import com.rstontherun.iamservice.domain.Role;
import com.rstontherun.iamservice.entity.UserData;
import com.rstontherun.iamservice.exception.UserAlreadyExistsException;
import com.rstontherun.iamservice.model.AuthResponse;
import com.rstontherun.iamservice.model.UserRole;
import com.rstontherun.iamservice.repository.UserRepository;
import com.rstontherun.iamservice.service.AuthService;
import com.rstontherun.iamservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse registerUser(AppUser request) {
        try {
            var user = userRepository.save(createUserDataDto(request));
            var jwt = jwtService.generateToken(user);
            return AuthResponse.builder().token(jwt).build();
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException("User with the same email already exists");
        }
    }

    private UserData createUserDataDto(AppUser request) {
        return UserData.builder().name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userRole(Role.valueOf(request.getUserRole()))
                .build();
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),
                        request.getPassword()));

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        var jwt = jwtService.generateToken(user);

        return AuthResponse.builder().token(jwt).build();
    }

    @Override
    public UserRole validate(String token) {
        if (jwtService.isTokenValid(token)) {
            var email = jwtService.extractUserName(token);
            UserData user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
            return UserRole.builder().userRole(user.getUserRole()).build();
        }
        return null;
    }
}
