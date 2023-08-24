package com.rstontherun.iamservice.controller;

import com.rstontherun.iamservice.domain.AppUser;
import com.rstontherun.iamservice.domain.AuthRequest;
import com.rstontherun.iamservice.model.AuthResponse;
import com.rstontherun.iamservice.model.UserRole;
import com.rstontherun.iamservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    @Autowired
    private AuthService service;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public AuthResponse addNewUser(@RequestBody AppUser appUser) {
        return service.registerUser(appUser);
    }

    @PostMapping("/token")
    public AuthResponse getToken(@RequestBody AuthRequest authRequest) {
        Authentication authenticate = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
        if (authenticate.isAuthenticated()) {
            return service.login(authRequest);
        } else {
            throw new RuntimeException("invalid access");
        }
    }

    @PostMapping("/introspect")
    public UserRole validateToken(@RequestHeader("Authorization") String authHeader, @RequestBody AuthRequest authRequest) {
        String token;
        if (authHeader.contains("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            token = authHeader;
        }
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
        if (authenticate.isAuthenticated()) {
            return service.validate(token);
        } else {
            throw new RuntimeException("invalid access");
        }

    }
}

