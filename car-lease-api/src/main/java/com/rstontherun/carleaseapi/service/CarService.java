package com.rstontherun.carleaseapi.service;

import com.rstontherun.carleaseapi.data.Car;
import com.rstontherun.carleaseapi.repository.CarRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public Mono<Car> findById(Integer carId) {
        return carRepository.findById(carId);
    }

    public Flux<Car> getAll() {
        return carRepository.findAll();
    }

    public Mono<Car> create(Car car) {
        return carRepository.save(car);
    }

    public Mono<Car> update(Integer carId, Car car) {
        return carRepository.findById(carId)
                .flatMap(existingCar -> {
                    existingCar.setMake(car.getMake());
                    existingCar.setModel(car.getModel());
                    // Set other properties as needed
                    return carRepository.save(existingCar);
                });
    }

    public Mono<Void> delete(Integer carId) {
        return carRepository.deleteById(carId);
    }

    public Mono<Double> getNettPriceByCarId(Integer carId) {
            return carRepository.findById(carId)
                    .map(Car::getNettPrice);
    }
}

