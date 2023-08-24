package com.rstontherun.carleaseapi.repository;

import com.rstontherun.carleaseapi.data.Customer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerRepository extends ReactiveCrudRepository<Customer, Integer> {
    Mono<Customer> findByEmailAddress(String emailAddress);
}
