package com.rstontherun.carleaseapi.repository;

import com.rstontherun.carleaseapi.data.Car;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends ReactiveCrudRepository<Car, Integer> {

}
