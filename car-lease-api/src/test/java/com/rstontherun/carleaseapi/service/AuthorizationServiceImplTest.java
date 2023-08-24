package com.rstontherun.carleaseapi.service;

import com.rstontherun.carleaseapi.domain.IamServiceProperties;
import com.rstontherun.carleaseapi.exception.BadRequestException;
import com.rstontherun.carleaseapi.exception.ForbiddenException;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceImplTest {

    private final MockWebServer mockWebServer = new MockWebServer();

    @Autowired
    private AuthorizationService authorizationService;

    @MockBean
    private WebClient iamWebClient;

    @BeforeEach
    @SneakyThrows
    void setUp() {
        mockWebServer.start(8085);
        mockWebServer.url("http://localhost:8085/api/auth/instrospect");
        authorizationService = new AuthorizationServiceImpl(
                WebClient.create(mockWebServer.url("").toString()), IamServiceProperties.builder()
                .clientId("clientid")
                .clientSecret("secret")
                .introspectResource("/api/auth/instrospect")
                .build());
    }

    @AfterEach
    @SneakyThrows
    void tearDown() {
        mockWebServer.shutdown();
    }

    @Test
    @SneakyThrows
    void validateCustomerToken_ValidToken_ValidRole() {
        enqueueResponse(HttpStatus.OK, 1, "{\"role\": \"Employee\"}");

        Mono<String> result = authorizationService.validateCustomerToken("validToken", Arrays.asList("Employee"));

        StepVerifier.create(result)
                .expectNext("validToken")
                .verifyComplete();

        assertEquals("/api/auth/instrospect", mockWebServer.takeRequest().getPath());
    }

    @Test
    @SneakyThrows
    void validateCustomerToken_ValidToken_InvalidRole_Forbidden() {
        enqueueResponse(HttpStatus.OK, 1, "{\"role\": \"Broker\"}");

        Mono<String> result = authorizationService.validateCustomerToken("validToken", Arrays.asList("Employee"));

        StepVerifier.create(result)
                .expectError(ForbiddenException.class)
                .verify();

        assertEquals("/api/auth/instrospect", mockWebServer.takeRequest().getPath());
    }

    @Test
    @SneakyThrows
    void validateCustomerToken_InvalidToken_BadRequest() {
        enqueueResponse(HttpStatus.FORBIDDEN, 1, "Bad Request");

        Mono<String> result = authorizationService.validateCustomerToken("invalidToken", Arrays.asList("Employee"));

        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();

        assertEquals("/api/auth/instrospect", mockWebServer.takeRequest().getPath());
    }

    private void enqueueResponse(HttpStatus status, int times, String response) {
        IntStream.range(0, times).boxed().forEach(i -> mockWebServer.enqueue(buildMockResponse(status, response)));
    }

    @SneakyThrows
    public MockResponse buildMockResponse(HttpStatus status, String response) {
        MockResponse mockResponse = new MockResponse().setResponseCode(status.value()).
                addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        return mockResponse.setBody(response);
    }
}


