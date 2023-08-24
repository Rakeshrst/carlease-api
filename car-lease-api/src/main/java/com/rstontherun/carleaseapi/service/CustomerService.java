package com.rstontherun.carleaseapi.service;

import com.rstontherun.carleaseapi.data.Customer;
import com.rstontherun.carleaseapi.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Mono<Customer> findById(Integer customerId) {
        return customerRepository.findById(customerId);
    }

    public Flux<Customer> getAll() {
        return customerRepository.findAll();
    }

    public Mono<Customer> create(Customer customer) {
        return customerRepository.save(customer);
    }

    public Mono<Customer> update(Integer customerId, Customer customer) {
        return customerRepository.findById(customerId)
                .flatMap(existingCustomer -> {
                    existingCustomer.setName(customer.getName());
                    existingCustomer.setStreet(customer.getStreet());
                    existingCustomer.setPlace(customer.getPlace());
                    existingCustomer.setHouseNumber(customer.getHouseNumber());
                    existingCustomer.setPhoneNumber(customer.getPhoneNumber());
                    return customerRepository.save(existingCustomer);
                });
    }

    public Mono<Void> delete(Integer customerId) {
        return customerRepository.deleteById(customerId);
    }

    public Mono<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmailAddress(email);
    }
}
