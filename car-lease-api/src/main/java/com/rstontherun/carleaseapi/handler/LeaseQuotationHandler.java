package com.rstontherun.carleaseapi.handler;

import com.rstontherun.carleaseapi.data.Car;
import com.rstontherun.carleaseapi.data.Customer;
import com.rstontherun.carleaseapi.data.LeaseQuotation;
import com.rstontherun.carleaseapi.domain.LeaseRateRequest;
import com.rstontherun.carleaseapi.exception.CarNotFoundException;
import com.rstontherun.carleaseapi.exception.CustomerNotFoundException;
import com.rstontherun.carleaseapi.exception.ForbiddenException;
import com.rstontherun.carleaseapi.exception.UnauthorizedException;
import com.rstontherun.carleaseapi.model.LeaseQuotationResponse;
import com.rstontherun.carleaseapi.service.AuthorizationService;
import com.rstontherun.carleaseapi.service.CarService;
import com.rstontherun.carleaseapi.service.CustomerService;
import com.rstontherun.carleaseapi.service.LeaseService;
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
import java.util.function.Function;

import static com.rstontherun.carleaseapi.utils.TokenDecoderutility.extractUserName;

@Component
@RequiredArgsConstructor
public class LeaseQuotationHandler {

    private final LeaseService leaseService;
    private final CustomerService customerService;
    private final CarService carService;

    private final AuthorizationService authService;

    @Value("${car-lease.allowedRoles}")
    private final List<String> allowedRoles;


    public Mono<ServerResponse> calculateLeaseRate(ServerRequest request) {
        var loggedBy = validateToken(request);
        return request.bodyToMono(LeaseRateRequest.class)
                .flatMap(leaseRateRequest -> carService.getNettPriceByCarId(leaseRateRequest.getCarId())
                        .flatMap(nettPrice -> {
                            leaseRateRequest.setLoggedBy(loggedBy);
                            leaseRateRequest.setNettPrice(nettPrice);

                            Mono<Customer> customerMono = customerService.getCustomerByEmail(leaseRateRequest.getCustomerEmail())
                                    .switchIfEmpty(Mono.error(new CustomerNotFoundException("Customer not found")));
                            Mono<Car> carMono = carService.findById(leaseRateRequest.getCarId())
                                    .switchIfEmpty(Mono.error(new CarNotFoundException("Car not found")));

                            return Mono.zip(customerMono, carMono)
                                    .flatMap(tuple -> {
                                        Customer customer = tuple.getT1();
                                        Car car = tuple.getT2();
                                        if (customer != null && car != null) {
                                            return leaseService.createLeaseQuotation(leaseRateRequest)
                                                    .flatMap(savedLeaseQuotation ->
                                                            ServerResponse.created(URI.create("/leaseQuotations/" + savedLeaseQuotation.getQuotationId()))
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .bodyValue(savedLeaseQuotation));
                                        } else {
                                            if (customer == null) {
                                                return ServerResponse.badRequest()
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue("Customer not found");
                                            } else {
                                                return ServerResponse.badRequest()
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue("Car not found");
                                            }
                                        }
                                    });
                        }));
    }

    private String validateToken(ServerRequest request) {
        List<String> authorizationHeaders = request.headers().header("Authorization");

        if (authorizationHeaders.isEmpty()) {
            throw new UnauthorizedException("Authorization header missing");
        }

        String authorizationHeader = authorizationHeaders.get(0);

        return extractUserName(authorizationHeader);
    }

    public Mono<ServerResponse> getLeaseQuotationByCustomerEmail(ServerRequest request) {
        String customerEmail = request.pathVariable("customerEmail");
        List<String> authorizationHeaders = request.headers().header("Authorization");

        if (authorizationHeaders.isEmpty()) {
            throw new UnauthorizedException("Authorization header missing");
        }

        String authorizationHeader = authorizationHeaders.get(0);

        return authService.validateCustomerToken(authorizationHeader, allowedRoles)
                .flatMap(role -> { System.out.println("Role: " +role);
                    if (allowedRoles.contains (role)) {
                        return leaseService.getLeaseQuotationByCustomerEmail(customerEmail)
                                .flatMap(getLeaseQuotationMonoResponse())
                                .collectList()
                                .flatMap(response -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(response))
                                .switchIfEmpty(ServerResponse.notFound().build());
                    }
                    return Mono.error(new ForbiddenException("Invalid roles"));
                });
    }

    private Function<LeaseQuotation, Mono<? extends LeaseQuotationResponse>> getLeaseQuotationMonoResponse() {
        return leaseQuotation -> {
            Mono<Customer> customerMono = customerService.getCustomerByEmail(leaseQuotation.getCustomerEmail());
            Mono<Car> carMono = carService.findById(Integer.valueOf(leaseQuotation.getCarId()));
            return Mono.zip(customerMono, carMono)
                    .map(tuple -> getLeaseQuotationResponse(leaseQuotation, tuple));
        };
    }

    private static LeaseQuotationResponse getLeaseQuotationResponse(LeaseQuotation leaseQuotation, Tuple2<Customer, Car> tuple) {

        return LeaseQuotationResponse.builder()
                .customer(tuple.getT1())
                .car(tuple.getT2())
                .quotationId(leaseQuotation.getQuotationId())
                .customerEmail(leaseQuotation.getCustomerEmail())
                .carId(leaseQuotation.getCarId())
                .mileage(leaseQuotation.getMileage())
                .expectedStartDate(leaseQuotation.getExpectedStartDate())
                .duration(leaseQuotation.getDuration())
                .interestRate(leaseQuotation.getInterestRate())
                .nettPrice(leaseQuotation.getNettPrice())
                .monthlyLeaseRate(leaseQuotation.getMonthlyLeaseRate())
                .contract(leaseQuotation.isContract())
                .quotationBy(leaseQuotation.getQuotationBy())
                .contractedBy(leaseQuotation.getContractedBy())
                .quotationCreateTime(leaseQuotation.getQuotationCreateTime())
                .build();
    }

    public Mono<ServerResponse> getLeaseQuotationByQuoteId(ServerRequest request) {
        validateToken(request);

        Integer quotationId = Integer.parseInt(request.pathVariable("quotationId"));
        return leaseService.getLeaseQuotationById(quotationId)
                .flatMap(getLeaseQuotationMonoResponse())
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

}

