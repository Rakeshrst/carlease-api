package com.rstontherun.carleaseapi.config;

import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ExternalServiceWebClientTest {

    private final ExternalServiceWebClient externalServiceWebClient = new ExternalServiceWebClient();
    private final MockWebServer mockWebServer = new MockWebServer();

    @SneakyThrows
    @BeforeEach
    void setup() {
        mockWebServer.start();
        String mockResponseBody = "Hallo wereld!";
        mockWebServer.enqueue(new MockResponse().setBody(mockResponseBody));
    }

    @SneakyThrows
    @AfterEach
    void tearDown() {
        mockWebServer.shutdown();
    }

    @Test
    @SneakyThrows
    void testXRequestHeader() {
        var url = mockWebServer.url("/test").toString();
        var client = externalServiceWebClient.iamWebClient(url);
        client.get().exchangeToMono(t -> Mono.just("response body")).block();
        var request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertEquals("/test", request.getPath());

    }

}
