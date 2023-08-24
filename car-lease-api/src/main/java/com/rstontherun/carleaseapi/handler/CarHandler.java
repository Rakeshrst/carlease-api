package com.rstontherun.carleaseapi.handler;

import com.rstontherun.carleaseapi.data.Car;
import com.rstontherun.carleaseapi.exception.ForbiddenException;
import com.rstontherun.carleaseapi.service.AuthorizationService;
import com.rstontherun.carleaseapi.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
public class CarHandler {

    private final CarService carService;

    private final AuthorizationService authService;

    @Value("${car.read.allowedRoles}")
    private final List<String> readRoles;

    @Value("${car.update.allowedRoles}")
    private final List<String> updateRoles;

    public Mono<ServerResponse> getAllCars(ServerRequest request) {
        String authorizationHeader = getAuthorizationHeader(request);

        Flux<Car> cars = carService.getAll();

        return authService.validateCustomerToken(authorizationHeader, readRoles)
                .then(ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(cars, Car.class));
    }

    public Mono<ServerResponse> getCarById(ServerRequest request) {

        String authorizationHeader = getAuthorizationHeader(request);
        int carId = Integer.parseInt(request.pathVariable("id"));
        Mono<Car> carMono = carService.findById(carId);
        return authService.validateCustomerToken(authorizationHeader, readRoles)
                .then(carMono.flatMap(car ->
                                ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(car))
                        .switchIfEmpty(ServerResponse.notFound().build()));
    }

    public Mono<ServerResponse> createCar(ServerRequest request) {

        String authorizationHeader = getAuthorizationHeader(request);
        Mono<Car> carMono = request.bodyToMono(Car.class);

        return authService.validateCustomerToken(authorizationHeader, updateRoles)
                .then(carMono.flatMap(car ->
                        carService.create(car)
                                .flatMap(createdCar ->
                                        ServerResponse.created(URI.create("/cars/" + createdCar.getCarId()))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(createdCar))
                ))
                .onErrorResume(ForbiddenException.class, exception -> ServerResponse.status(HttpStatus.FORBIDDEN).build());
    }

    public Mono<ServerResponse> updateCar(ServerRequest request) {
        String authorizationHeader = getAuthorizationHeader(request);

        int carId = Integer.parseInt(request.pathVariable("id"));
        Mono<Car> carMono = request.bodyToMono(Car.class);

        return authService.validateCustomerToken(authorizationHeader, updateRoles)
                .then(carMono.flatMap(updatedCar ->
                        carService.update(carId, updatedCar)
                                .flatMap(savedCar ->
                                        ServerResponse.ok()
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(savedCar))
                                .switchIfEmpty(ServerResponse.notFound().build())
                ));
    }

    public Mono<ServerResponse> deleteCar(ServerRequest request) {
        String authorizationHeader = getAuthorizationHeader(request);

        int carId = Integer.parseInt(request.pathVariable("id"));
        return authService.validateCustomerToken(authorizationHeader, updateRoles)
                .then(carService.delete(carId)
                        .flatMap(Void ->
                                ServerResponse.noContent().build())
                        .switchIfEmpty(ServerResponse.notFound().build()));
    }
}

