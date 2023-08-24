package com.rstontherun.carleaseapi.service;

import com.rstontherun.carleaseapi.domain.IamServiceProperties;
import com.rstontherun.carleaseapi.domain.UserRole;
import com.rstontherun.carleaseapi.exception.BadRequestException;
import com.rstontherun.carleaseapi.exception.ForbiddenException;
import com.rstontherun.carleaseapi.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
@Slf4j
@Service
@Configuration
@EnableConfigurationProperties(IamServiceProperties.class)
public class AuthorizationServiceImpl implements AuthorizationService {

    private final WebClient iamWebClient;

    private final IamServiceProperties iamProps;


    @Override
    public Mono<String> validateCustomerToken(String token, List<String> allowedRoles) {
        return iamWebClient.post()
                .uri(uriBuilder -> uriBuilder.path(iamProps.getIntrospectResource()).build())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(Map.of("email", iamProps.getClientId(),
                        "password", iamProps.getClientSecret())))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new BadRequestException("Authorization is not Valid")))
                .bodyToMono(UserRole.class)
                .doOnNext(userRole -> System.out.println(userRole))
                .doOnError(throwable -> log.error("Authentication Error: {}", throwable.getMessage()))
                .map(UserRole::getRole)
                .onErrorResume(ForbiddenException.class, e -> Mono.error(new UnauthorizedException("Unauthorized")))
                .onErrorResume(BadRequestException.class, e -> Mono.error(new UnauthorizedException("Unauthorized")));
              }

    private Boolean validateRole(String token, List<String> allowedRoles, UserRole role) {
             if (!hasValidRole(role, allowedRoles)) {
            Mono.error(new ForbiddenException("Invalid roles"));
            return false;
        } else {
            return true;
        }
    }

    private boolean hasValidRole(UserRole role, List<String> allowedRoles) {
        return allowedRoles.contains(role.getRole());
    }
}
