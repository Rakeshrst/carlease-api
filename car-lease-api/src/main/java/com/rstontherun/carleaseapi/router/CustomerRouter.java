package com.rstontherun.carleaseapi.router;

import com.rstontherun.carleaseapi.handler.CarHandler;
import com.rstontherun.carleaseapi.handler.CustomerHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class CustomerRouter {
    @Bean
    public RouterFunction<ServerResponse> customerRoutes(CustomerHandler customerHandler) {
        return route()
                .GET("/customers", accept(MediaType.APPLICATION_JSON), customerHandler::getAllCustomers)
                .GET("/customers/{id}", accept(MediaType.APPLICATION_JSON), customerHandler::getCustomerById)
                .GET("/customers/{email}", accept(MediaType.APPLICATION_JSON), customerHandler::getCustomerByEmail)
                .POST("/customers", contentType(MediaType.APPLICATION_JSON), customerHandler::createCustomer)
                .PUT("/customers/{id}", contentType(MediaType.APPLICATION_JSON), customerHandler::updateCustomer)
                .DELETE("/customers/{id}", customerHandler::deleteCustomer)
                .build();
    }
}
