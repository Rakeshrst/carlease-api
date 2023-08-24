package com.rstontherun.carleaseapi.handler;

import com.rstontherun.carleaseapi.data.Customer;
import com.rstontherun.carleaseapi.exception.UnauthorizedException;
import com.rstontherun.carleaseapi.service.AuthorizationService;
import com.rstontherun.carleaseapi.service.CustomerService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static com.rstontherun.carleaseapi.utils.TokenDecoderutility.extractUserName;

@Component
@RequiredArgsConstructor
public class CustomerHandler {

    private final CustomerService customerService;

    private final AuthorizationService authService;

    @Value("${customer.allowedRoles}")
    private final List<String> allowedRoles;


    public Mono<ServerResponse> getAllCustomers(ServerRequest request) {
        validateToken(request, allowedRoles);

        Flux<Customer> customers = customerService.getAll();
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(customers, Customer.class);
    }

    private String validateToken(ServerRequest request, List<String> acceptedRoles) {
        List<String> authorizationHeaders = request.headers().header("Authorization");

        if (authorizationHeaders.isEmpty()) {
            throw new UnauthorizedException("Authorization header missing");
        }

        String authorizationHeader = authorizationHeaders.get(0);
        authService.validateCustomerToken(authorizationHeader, acceptedRoles);
        return extractUserName(authorizationHeader);
    }

    public Mono<ServerResponse> getCustomerById(ServerRequest request) {
        validateToken(request, allowedRoles);

        int customerId = Integer.parseInt(request.pathVariable("id"));
        Mono<Customer> customerMono = customerService.findById(customerId);
        return customerMono.flatMap(customer ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(customer))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createCustomer(ServerRequest request) {
        validateToken(request, allowedRoles);

        Mono<Customer> customerMono = request.bodyToMono(Customer.class);
        return customerMono.flatMap(customer ->
                customerService.create(customer)
                        .flatMap(createdCustomer ->
                                ServerResponse.created(URI.create("/customers/" + createdCustomer.getCustomerId()))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(createdCustomer))
        );
    }

    public Mono<ServerResponse> updateCustomer(ServerRequest request) {
        validateToken(request, allowedRoles);

        int customerId = Integer.parseInt(request.pathVariable("id"));
        Mono<Customer> customerMono = request.bodyToMono(Customer.class);
        return customerMono.flatMap(updatedCustomer ->
                customerService.update(customerId, updatedCustomer)
                        .flatMap(savedCustomer ->
                                ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(savedCustomer))
                        .switchIfEmpty(ServerResponse.notFound().build())
        );
    }

    public Mono<ServerResponse> deleteCustomer(ServerRequest request) {
        validateToken(request, allowedRoles);

        int customerId = Integer.parseInt(request.pathVariable("id"));
        return customerService.delete(customerId)
                .flatMap(Void ->
                        ServerResponse.noContent().build())
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getCustomerByEmail(ServerRequest request) {
        validateToken(request, allowedRoles);

        String email = request.pathVariable("email");
        Mono<Customer> customerMono = customerService.getCustomerByEmail(email);
        return customerMono.flatMap(customer ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(customer))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
