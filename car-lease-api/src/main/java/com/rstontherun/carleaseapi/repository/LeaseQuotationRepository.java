package com.rstontherun.carleaseapi.repository;

import com.rstontherun.carleaseapi.data.LeaseQuotation;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface LeaseQuotationRepository extends ReactiveCrudRepository<LeaseQuotation, Integer> {
    Flux<LeaseQuotation> findByCustomerEmail(String customerEmail);
}