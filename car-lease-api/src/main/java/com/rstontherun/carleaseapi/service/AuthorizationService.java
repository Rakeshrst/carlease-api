package com.rstontherun.carleaseapi.service;

import reactor.core.publisher.Mono;

import java.util.List;

public interface AuthorizationService {
    Mono<String> validateCustomerToken(String token, List<String> allowedRoles);
}
