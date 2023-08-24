package com.rstontherun.carleaseapi.router;

import com.rstontherun.carleaseapi.handler.CarHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class CarRouter {
    @Bean
    public RouterFunction<ServerResponse> carRoutes(CarHandler carHandler) {
        return route()
                .GET("/cars", accept(MediaType.APPLICATION_JSON), carHandler::getAllCars)
                .GET("/cars/{id}", accept(MediaType.APPLICATION_JSON), carHandler::getCarById)
                .POST("/cars", contentType(MediaType.APPLICATION_JSON), carHandler::createCar)
                .PUT("/cars/{id}", contentType(MediaType.APPLICATION_JSON), carHandler::updateCar)
                .DELETE("/cars/{id}", carHandler::deleteCar)
                .build();
    }
}
