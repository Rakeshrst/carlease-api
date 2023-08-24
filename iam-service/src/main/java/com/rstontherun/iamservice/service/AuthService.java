package com.rstontherun.iamservice.service;

import com.rstontherun.iamservice.domain.AppUser;
import com.rstontherun.iamservice.domain.AuthRequest;
import com.rstontherun.iamservice.model.AuthResponse;
import com.rstontherun.iamservice.model.UserRole;

public interface AuthService {
    AuthResponse registerUser(AppUser appUser);

    AuthResponse login(AuthRequest authRequest);

    UserRole validate(String token);
}
