package com.rstontherun.carleaseapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class ExternalServiceWebClient {

    @Bean
    WebClient iamWebClient(@Value("${iam-service.url}") String iamUrl) {
        return createWebClient(iamUrl);
    }

    private WebClient createWebClient(String baseurl) {
        return webClientBuilder()
                .baseUrl(baseurl)
                .build();
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector())
                .filter(logRequest())
                .filter(logResponse());
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
           log.info ("Request {}", clientRequest);// Log request information here
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info ("Response {}", clientResponse);
            return Mono.just(clientResponse);
        });
    }
}
