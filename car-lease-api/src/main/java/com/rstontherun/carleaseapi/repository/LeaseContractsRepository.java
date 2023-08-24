package com.rstontherun.carleaseapi.repository;

import com.rstontherun.carleaseapi.data.LeaseContracts;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface LeaseContractsRepository extends ReactiveCrudRepository<LeaseContracts, Integer> {
    Flux<LeaseContracts> findByCustomerEmail(String customerEmail);
}
