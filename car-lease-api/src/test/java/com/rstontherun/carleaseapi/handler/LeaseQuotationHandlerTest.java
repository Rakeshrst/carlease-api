package com.rstontherun.carleaseapi.handler;

import com.rstontherun.carleaseapi.data.Car;
import com.rstontherun.carleaseapi.data.Customer;
import com.rstontherun.carleaseapi.data.LeaseQuotation;
import com.rstontherun.carleaseapi.domain.LeaseRateRequest;
import com.rstontherun.carleaseapi.exception.DataNotFoundException;
import com.rstontherun.carleaseapi.exception.UnauthorizedException;
import com.rstontherun.carleaseapi.router.LeaseQuotationRouter;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LeaseQuotationHandlerTest {
    @Mock
    private LeaseService leaseService;

    @Mock
    private AuthorizationService authService;

    @Mock
    private CustomerService customerService;

    @Mock
    private CarService carService;

    @InjectMocks
    private LeaseQuotationHandler handler;

    private WebTestClient client;

    private final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiRW1wbG95ZWUifV0sInN1YiI6ImNhcmxlYXNlYXBpQGF1dG9sZWFzZS5jb20iLCJpYXQiOjE2OTI3OTE0NTIsImV4cCI6MTY5Mjc5Mjg5Mn0.6TQayemPQYwwxVdJqqRw9G_7JokI_gSm8q3GSflw0L0";

    @BeforeEach
    void setUp() {
        handler = new LeaseQuotationHandler(leaseService, customerService, carService, authService, List.of("Employee", "Broker"));
        client = WebTestClient.bindToRouterFunction(new LeaseQuotationRouter().leaseQuotationRoutes(handler)).build();
    }

    @Test
    void calculateLeaseRate_Successful() {
        mockCarNettPrice();
        mockGetCustomerBYEmail();
        mockCar();
        mockSuccessfulAuthorization();
        when(leaseService.createLeaseQuotation(any())).thenReturn(Mono.just(LeaseQuotation.builder().quotationId(123).build()));

        LeaseRateRequest request = getLeaseRateRequest();

        client.post()
                .uri("/api/lease/quote")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + TEST_TOKEN)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueMatches("location", "/leaseQuotations/123");
    }

    private void mockCarNettPrice() {
        when(carService.getNettPriceByCarId(any())).thenReturn(Mono.just(1000.0));
    }

    private void mockGetCustomerBYEmail() {
        when(customerService.getCustomerByEmail(any())).thenReturn(Mono.just(Customer.builder()
                .customerId(123)
                .emailAddress("test@rmail.com")
                .build()));
    }

    private void mockCar() {
        when(carService.findById(any())).thenReturn(Mono.just(Car.builder().carId(123).nettPrice(45000).build()));
    }

    private void mockSuccessfulAuthorization() {
        when(authService.validateCustomerToken(anyString(), anyList())).thenReturn(Mono.empty());
    }

    private static LeaseRateRequest getLeaseRateRequest() {
        return LeaseRateRequest.builder()
                .carId(1)
                .customerEmail("rst@test123.com")
                .mileage(45000)
                .duration(36)
                .interestRate(4.5)
                .build();
    }

    @Test
    void calculateLeaseRate_AuthorizationTokenMissing() {
        LeaseRateRequest request = new LeaseRateRequest();
        client.post()
                .uri("/api/lease/quote")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void calculateLeaseRate_CustomerNotFound() {
        mockCarNettPrice();
        mockSuccessfulAuthorization();
        when(customerService.getCustomerByEmail(any())).thenReturn(Mono.empty());
        mockCar();
        LeaseRateRequest request = getLeaseRateRequest();

        client.post()
                .uri("/api/lease/quote")
                .header("Authorization", "Bearer " + TEST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void calculateLeaseRate_CarNotFound() {
        mockCarNettPrice();
        mockSuccessfulAuthorization();
        when(customerService.getCustomerByEmail(any())).thenReturn(Mono.empty());
        when(carService.findById(any())).thenReturn(Mono.empty());

        LeaseRateRequest request = getLeaseRateRequest();

        client.post()
                .uri("/api/lease/quote")
                .header("Authorization", "Bearer " + TEST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getLeaseQuotationByCustomerEmail_ValidEmail_NoQuotationsFound() {
        mockSuccessfulAuthorization();

        when(leaseService.getLeaseQuotationByCustomerEmail(anyString())).thenReturn(Flux.error(new DataNotFoundException("Quotation Not Found")));

        client.get()
                .uri("/api/lease/quote/customerEmail/jane@example.com")
                .header("Authorization", "Bearer " + TEST_TOKEN)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getLeaseQuotationByCustomerEmail_TokenValidationFailure_Unauthorized() {
        when(authService.validateCustomerToken(anyString(), anyList())).thenThrow(new UnauthorizedException("Unauthorized access"));

        client.get()
                .uri("/api/lease/quote/customerEmail/john@example.com")
                .header("Authorization", "Bearer " + TEST_TOKEN)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void getLeaseQuotationByQuoteId_QuoteNotFound() {
        mockSuccessfulAuthorization();
        when(leaseService.getLeaseQuotationById(any())).thenReturn(Mono.empty());

        client.get()
                .uri("/api/lease/quote/quotationId/123")
                .header("Authorization", "Bearer " + TEST_TOKEN)
                .exchange()
                .expectStatus().isNotFound();
    }

}
