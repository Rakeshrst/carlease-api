package com.rstontherun.carleaseapi.handler;

import com.rstontherun.carleaseapi.data.Car;
import com.rstontherun.carleaseapi.data.Customer;
import com.rstontherun.carleaseapi.data.LeaseContracts;
import com.rstontherun.carleaseapi.domain.LeaseContractRequest;
import com.rstontherun.carleaseapi.exception.CarNotFoundException;
import com.rstontherun.carleaseapi.exception.CustomerNotFoundException;
import com.rstontherun.carleaseapi.exception.DataNotFoundException;
import com.rstontherun.carleaseapi.exception.UnauthorizedException;
import com.rstontherun.carleaseapi.model.LeaseContractResponse;
import com.rstontherun.carleaseapi.service.AuthorizationService;
import com.rstontherun.carleaseapi.service.CarService;
import com.rstontherun.carleaseapi.service.CustomerService;
import com.rstontherun.carleaseapi.service.LeaseService;
import com.rstontherun.carleaseapi.utils.TokenDecoderutility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.net.URI;
import java.util.List;

import static com.rstontherun.carleaseapi.utils.TokenDecoderutility.getAuthorizationHeader;

@Component
@RequiredArgsConstructor
public class LeaseContractHandler {

    private final LeaseService leaseService;
    private final CustomerService customerService;
    private final CarService carService;
    private final AuthorizationService authService;

    @Value("${car-lease.allowedRoles}")
    private final List<String> allowedRoles;

    public Mono<ServerResponse> confirmContract(ServerRequest request) {
        List<String> authorizationHeaders = request.headers().header("Authorization");

        if (authorizationHeaders.isEmpty()) {
            throw new UnauthorizedException("Authorization header missing");
        }

        String authorizationHeader = authorizationHeaders.get(0);
        authService.validateCustomerToken(authorizationHeader, allowedRoles);

        var contractedBy = extractUserName(authorizationHeader);

        return request.bodyToMono(LeaseContractRequest.class)
                .flatMap(confirmationRequest -> Mono.zip(getCustomer(confirmationRequest.getCustomerEmail()),
                                getCar(confirmationRequest.getCarId()))
                        .flatMap(tuple -> leaseService.confirmContract(confirmationRequest, contractedBy)
                                .flatMap(confirmedContract ->
                                        ServerResponse.created(URI.create("/leaseContracts/" + confirmedContract.getContractId()))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(confirmedContract))
                                .switchIfEmpty(ServerResponse.notFound().build())));
    }

    private String extractUserName(String token) {
        return (String) TokenDecoderutility.getClaimSets(token).get("sub");
    }

    private Mono<Customer> getCustomer(String leaseContract) {
        return customerService.getCustomerByEmail(leaseContract)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException("Customer not found")));
    }

    private Mono<Car> getCar(Integer carId) {
        return carService.findById(carId)
                .switchIfEmpty(Mono.error(new CarNotFoundException("Car not found")));
    }

    public Mono<ServerResponse> endLease(ServerRequest request) {
        String authorizationHeader = getAuthorizationHeader(request);

        Integer contractId = Integer.parseInt(request.pathVariable("contractId"));

        return authService.validateCustomerToken(authorizationHeader, allowedRoles)
                .then(leaseService.endLease(contractId, extractUserName(authorizationHeader))
                        .flatMap(endedContract ->
                                ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(endedContract))
                        .switchIfEmpty(ServerResponse.notFound().build()));
    }

    public Mono<ServerResponse> getLeaseContract(ServerRequest request) {
        String authorizationHeader = getAuthorizationHeader(request);

        Integer contractId = Integer.valueOf(request.pathVariable("contractId"));
        return authService.validateCustomerToken(authorizationHeader, allowedRoles)
                .then(leaseService.getLeaseContractById(contractId)
                        .flatMap(leaseContract ->
                                Mono.zip(getCustomer(leaseContract.getCustomerEmail()),
                                                getCar(leaseContract.getCarId()))
                                        .map(tuple -> getLeaseContractResponse(leaseContract, tuple)))
                        .flatMap(response -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response))
                        .switchIfEmpty(ServerResponse.notFound().build()));
    }

    private static LeaseContractResponse getLeaseContractResponse(LeaseContracts leaseContract, Tuple2<Customer, Car> tuple) {

        return LeaseContractResponse.builder()
                .customer(tuple.getT1())
                .car(tuple.getT2())
                .contractId(leaseContract.getContractId())
                .customerEmail(leaseContract.getCustomerEmail())
                .carId(leaseContract.getCarId())
                .mileage(leaseContract.getMileage())
                .startDate(leaseContract.getStartDate())
                .endDate(leaseContract.getEndDate())
                .duration(leaseContract.getDuration())
                .interestRate(leaseContract.getInterestRate())
                .nettPrice(leaseContract.getNettPrice())
                .monthlyLeaseRate(leaseContract.getMonthlyLeaseRate())
                .active(leaseContract.isActive())
                .contractedBy(leaseContract.getContractedBy())
                .contractConfirmationTime(leaseContract.getContractConfirmationTime())
                .build();
    }

    public Mono<ServerResponse> getLeaseContractByCustomerEmail(ServerRequest request) {
        String authorizationHeader = getAuthorizationHeader(request);

        String customerEmail = request.pathVariable("customerEmail");
        return authService.validateCustomerToken(authorizationHeader, allowedRoles)
                .then(leaseService.getLeaseContractByCustomerEmail(customerEmail)
                        .flatMap(leaseContract -> {
                            if (leaseContract != null) {
                                return Mono.zip(getCustomer(leaseContract.getCustomerEmail()),
                                                getCar(leaseContract.getCarId()))
                                        .map(tuple -> getLeaseContractResponse(leaseContract, tuple));
                            } else {
                                return Mono.error(new DataNotFoundException("Lease contract not found"));
                            }
                        })
                        .collectList()
                        .flatMap(response -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response))
                        .switchIfEmpty(ServerResponse.notFound().build()));
    }
}

