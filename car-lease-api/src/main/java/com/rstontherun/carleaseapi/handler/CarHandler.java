package com.rstontherun.carleaseapi.handler;

import com.rstontherun.carleaseapi.data.Car;
import com.rstontherun.carleaseapi.exception.UnauthorizedException;
import com.rstontherun.carleaseapi.service.AuthorizationService;
import com.rstontherun.carleaseapi.service.CarService;
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
public class CarHandler {

    private final CarService carService;

    private final AuthorizationService authService;

    @Value("${car.read.allowedRoles}")
    private final List<String> readRoles;

    @Value("${car.update.allowedRoles}")
    private final List<String> updateRoles;

    public Mono<ServerResponse> getAllCars(ServerRequest request) {
        validateToken(request, readRoles);

        Flux<Car> cars = carService.getAll();
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(cars, Car.class);
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

    public Mono<ServerResponse> getCarById(ServerRequest request) {
        validateToken(request, readRoles);

        int carId = Integer.parseInt(request.pathVariable("id"));
        Mono<Car> carMono = carService.findById(carId);
        return carMono.flatMap(car ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(car))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createCar(ServerRequest request) {
        validateToken(request, updateRoles);

        Mono<Car> carMono = request.bodyToMono(Car.class);
        return carMono.flatMap(car ->
                carService.create(car)
                        .flatMap(createdCar ->
                                ServerResponse.created(URI.create("/cars/" + createdCar.getCarId()))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(createdCar))
        );
    }

    public Mono<ServerResponse> updateCar(ServerRequest request) {
        validateToken(request, updateRoles);

        int carId = Integer.parseInt(request.pathVariable("id"));
        Mono<Car> carMono = request.bodyToMono(Car.class);

        return carMono.flatMap(updatedCar ->
                carService.update(carId, updatedCar)
                        .flatMap(savedCar ->
                                ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(savedCar))
                        .switchIfEmpty(ServerResponse.notFound().build())
        );
    }

    public Mono<ServerResponse> deleteCar(ServerRequest request) {
        validateToken(request, updateRoles);

        int carId = Integer.parseInt(request.pathVariable("id"));
        return carService.delete(carId)
                .flatMap(Void ->
                        ServerResponse.noContent().build())
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}

