package com.rstontherun.carleaseapi.handler;

import com.rstontherun.carleaseapi.data.Car;
import com.rstontherun.carleaseapi.data.Customer;
import com.rstontherun.carleaseapi.data.LeaseContracts;
import com.rstontherun.carleaseapi.domain.LeaseContractRequest;
import com.rstontherun.carleaseapi.router.LeaseContractRouter;
import com.rstontherun.carleaseapi.service.AuthorizationService;
import com.rstontherun.carleaseapi.service.CarService;
import com.rstontherun.carleaseapi.service.CustomerService;
import com.rstontherun.carleaseapi.service.LeaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaseContractHandlerTest {
    private final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiRW1wbG95ZWUifV0sInN1YiI6ImNhcmxlYXNlYXBpQGF1dG9sZWFzZS5jb20iLCJpYXQiOjE2OTI3OTE0NTIsImV4cCI6MTY5Mjc5Mjg5Mn0.6TQayemPQYwwxVdJqqRw9G_7JokI_gSm8q3GSflw0L0";
    @Mock
    private LeaseService leaseService;
    @Mock
    private AuthorizationService authService;
    @Mock
    private CustomerService customerService;
    @Mock
    private CarService carService;
    @InjectMocks
    private LeaseContractHandler handler;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        handler = new LeaseContractHandler(leaseService, customerService, carService, authService, List.of("Employee", "Broker"));
        client = WebTestClient.bindToRouterFunction(new LeaseContractRouter().leaseContractRoutes(handler)).build();
    }


    @Test
    void confirmContract_Successful() {
        mockSuccessfulTokenValidation();
        mockCustomerAailable();
        mockCarAvailable();

        when(leaseService.confirmContract(any(), any())).thenReturn(Mono.just(getLeaseContractsData()));

        LeaseContractRequest request = getLeaseContractRequestData();

        WebTestClient
                .bindToRouterFunction(new LeaseContractRouter().leaseContractRoutes(handler))
                .build()
                .post()
                .uri("/api/lease/contract")
                .header("Authorization", "Bearer " + TEST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueMatches("location", "/leaseContracts/123");
    }

    private void mockSuccessfulTokenValidation() {
        when(authService.validateCustomerToken(anyString(), anyList())).thenReturn(Mono.empty());
    }

    private void mockCustomerAailable() {
        when(customerService.getCustomerByEmail(any())).thenReturn(Mono.just(getCustomerData()));
    }

    private void mockCarAvailable() {
        when(carService.findById(anyInt())).thenReturn(Mono.just(getCarData()));
    }

    private static LeaseContracts getLeaseContractsData() {
        return LeaseContracts.builder().contractId(123).customerEmail("jane@emaple.com").carId(4).active(true).build();
    }

    private static LeaseContractRequest getLeaseContractRequestData() {
        return LeaseContractRequest.builder()
                .carId(4)
                .customerEmail("info@Test.com")
                .monthlyLeaseRate(185.00)
                .quotationId(2)
                .interestRate(4.5)
                .duration(50)
                .nettPrice(4152.00)
                .build();
    }

    private static Customer getCustomerData() {
        return Customer.builder().customerId(123).emailAddress("test@testexample.com").build();
    }

    private static Car getCarData() {
        return Car.builder().carId(4).build();
    }

    @Test
    void confirmContract_CustomerNotFound_ReturnsBadRequest() {
        mockSuccessfulTokenValidation();
        when(customerService.getCustomerByEmail(anyString())).thenReturn(Mono.empty());
        mockCarAvailable();

        LeaseContractRequest request = getLeaseContractRequestData();
        WebTestClient
                .bindToRouterFunction(new LeaseContractRouter().leaseContractRoutes(handler))
                .build()
                .post()
                .uri("/api/lease/contract")
                .header("Authorization", "Bearer " + TEST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();

    }

    @Test
    void getLeaseContract_ValidContractId_Successful() {
        mockSuccessfulTokenValidation();
        when(leaseService.getLeaseContractById(anyInt())).thenReturn(Mono.just(getLeaseContractsData()));
        mockCustomerAailable();
        mockCarAvailable();

        client.get()
                .uri("/api/lease/contract/contractId/1")
                .header("Authorization", "Bearer " + TEST_TOKEN)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);

    }

    @Test
    void getLeaseContract_InvalidContractId_NotFound() {
        mockSuccessfulTokenValidation();
        when(leaseService.getLeaseContractById(anyInt())).thenReturn(Mono.empty());

        client.get()
                .uri("/api/lease/contract/contractId/999")
                .header("Authorization", "Bearer " + TEST_TOKEN)
                .exchange()
                .expectStatus().isNotFound();

    }

    @Test
    void getLeaseContractByCustomerEmail_ValidEmail_ContractsFound() {
        mockSuccessfulTokenValidation();
        mockCustomerAailable();
        mockCarAvailable();
        when(leaseService.getLeaseContractByCustomerEmail(anyString())).thenReturn(Flux.just(getLeaseContractsData()));

        client.get()
                .uri("/api/lease/contract/customerEmail/jane@example.com")
                .header("Authorization", "Bearer " + TEST_TOKEN)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);

        verify(authService).validateCustomerToken(anyString(), anyList());
        verify(leaseService).getLeaseContractByCustomerEmail(anyString());
        verify(customerService).getCustomerByEmail(any());
        verify(carService).findById(anyInt());
    }
}
