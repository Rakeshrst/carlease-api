package com.rstontherun.carleaseapi.handler;

import com.rstontherun.carleaseapi.data.Customer;
import com.rstontherun.carleaseapi.service.AuthorizationService;
import com.rstontherun.carleaseapi.service.CustomerService;
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

import static com.rstontherun.carleaseapi.utils.TokenDecoderutility.getAuthorizationHeader;

@Component
@RequiredArgsConstructor
public class CustomerHandler {

    private final CustomerService customerService;

    private final AuthorizationService authService;

    @Value("${customer.allowedRoles}")
    private final List<String> allowedRoles;


    public Mono<ServerResponse> getAllCustomers(ServerRequest request) {
        String authorizationHeader = getAuthorizationHeader(request);

        Flux<Customer> customers = customerService.getAll();
        return  authService.validateCustomerToken(authorizationHeader, allowedRoles)
                .then(ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(customers, Customer.class));
    }

    public Mono<ServerResponse> getCustomerById(ServerRequest request) {
        String authorizationHeader = getAuthorizationHeader(request);

        int customerId = Integer.parseInt(request.pathVariable("id"));
        Mono<Customer> customerMono = customerService.findById(customerId);
        return authService.validateCustomerToken(authorizationHeader, allowedRoles)
                .then(customerMono.flatMap(customer ->
                                ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(customer))
                        .switchIfEmpty(ServerResponse.notFound().build()));
    }

    public Mono<ServerResponse> createCustomer(ServerRequest request) {
        String authorizationHeader = getAuthorizationHeader(request);


        Mono<Customer> customerMono = request.bodyToMono(Customer.class);
        return authService.validateCustomerToken(authorizationHeader, allowedRoles)
                .then(customerMono.flatMap(customer ->
                        customerService.create(customer)
                                .flatMap(createdCustomer ->
                                        ServerResponse.created(URI.create("/customers/" + createdCustomer.getCustomerId()))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(createdCustomer))
                ));
    }

    public Mono<ServerResponse> updateCustomer(ServerRequest request) {
        String authorizationHeader = getAuthorizationHeader(request);

        int customerId = Integer.parseInt(request.pathVariable("id"));
        Mono<Customer> customerMono = request.bodyToMono(Customer.class);
        return authService.validateCustomerToken(authorizationHeader, allowedRoles)
                .then(customerMono.flatMap(updatedCustomer ->
                        customerService.update(customerId, updatedCustomer)
                                .flatMap(savedCustomer ->
                                        ServerResponse.ok()
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(savedCustomer))
                                .switchIfEmpty(ServerResponse.notFound().build())
                ));
    }

    public Mono<ServerResponse> deleteCustomer(ServerRequest request) {
        String authorizationHeader = getAuthorizationHeader(request);

        int customerId = Integer.parseInt(request.pathVariable("id"));
        return authService.validateCustomerToken(authorizationHeader, allowedRoles)
                .then(customerService.delete(customerId)
                        .flatMap(Void ->
                                ServerResponse.noContent().build())
                        .switchIfEmpty(ServerResponse.notFound().build()));
    }

    public Mono<ServerResponse> getCustomerByEmail(ServerRequest request) {
        String authorizationHeader = getAuthorizationHeader(request);

        String email = request.pathVariable("email");
        Mono<Customer> customerMono = customerService.getCustomerByEmail(email);
        return authService.validateCustomerToken(authorizationHeader, allowedRoles)
                .then(customerMono.flatMap(customer ->
                                ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(customer))
                        .switchIfEmpty(ServerResponse.notFound().build()));
    }
}
