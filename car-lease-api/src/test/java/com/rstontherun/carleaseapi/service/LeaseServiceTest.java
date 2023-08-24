package com.rstontherun.carleaseapi.service;

import com.rstontherun.carleaseapi.data.LeaseContracts;
import com.rstontherun.carleaseapi.data.LeaseQuotation;
import com.rstontherun.carleaseapi.domain.LeaseContractRequest;
import com.rstontherun.carleaseapi.domain.LeaseRateRequest;
import com.rstontherun.carleaseapi.exception.DataNotFoundException;
import com.rstontherun.carleaseapi.repository.LeaseContractsRepository;
import com.rstontherun.carleaseapi.repository.LeaseQuotationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaseServiceTest {

    @Mock
    private LeaseQuotationRepository quotationRepository;

    @Mock
    private LeaseContractsRepository contractsRepository;

    @InjectMocks
    private LeaseService leaseService;

    @Test
    void createLeaseQuotation_Successful() {
        LeaseRateRequest request = new LeaseRateRequest();
        when(quotationRepository.save(any())).thenReturn(Mono.just(new LeaseQuotation()));

        StepVerifier.create(leaseService.createLeaseQuotation(request))
                .expectNextCount(1)
                .verifyComplete();

        verify(quotationRepository, times(1)).save(any());
    }

    @Test
    void confirmContract_Successful() {
        LeaseContractRequest contractRequest = getLeaseContractRequest();
        LeaseQuotation quotation = getLeaseQuotation();
        when(quotationRepository.findById(anyInt())).thenReturn(Mono.just(quotation));
        when(quotationRepository.save(any())).thenReturn(Mono.just(getLeaseQuotation()));
        when(contractsRepository.save(any())).thenReturn(Mono.just(getLeaseContractsData()));

        StepVerifier.create(leaseService.confirmContract(contractRequest, "user"))
                .expectNextCount(1)
                .verifyComplete();

        verify(quotationRepository, times(1)).findById(anyInt());
        verify(quotationRepository, times(1)).save(any());
        verify(contractsRepository, times(1)).save(any());
    }

    private static LeaseQuotation getLeaseQuotation() {
        return LeaseQuotation.builder().quotationId(1).expectedStartDate(LocalDate.now()).customerEmail("test@test.com").carId(123).build();
    }

    private static LeaseContractRequest getLeaseContractRequest() {
        return LeaseContractRequest.builder().expectedStartDate(LocalDate.now()).quotationId(1).customerEmail("test@test.com").carId(123).build();
    }

    @Test
    void endLease_Successful() {
        when(contractsRepository.findById(anyInt())).thenReturn(Mono.just(getLeaseContractsData()));
        when(contractsRepository.save(any())).thenReturn(Mono.just(new LeaseContracts()));

        StepVerifier.create(leaseService.endLease(1, "user"))
                .expectNextCount(1)
                .verifyComplete();

        verify(contractsRepository, times(1)).findById(anyInt());
        verify(contractsRepository, times(1)).save(any());
    }

    private static LeaseContracts getLeaseContractsData() {
        return LeaseContracts.builder().contractId(123).customerEmail("jane@emaple.com").carId(4).active(true).build();
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
    void getLeaseQuotationByCustomerEmail_Successful() {
        String customerEmail = "test@example.com";
        List<LeaseQuotation> quotations = List.of(getLeaseQuotation());
        when(quotationRepository.findByCustomerEmail(anyString())).thenReturn(Flux.fromIterable(quotations));
        StepVerifier.create(leaseService.getLeaseQuotationByCustomerEmail(customerEmail))
                .expectNextCount(quotations.size())
                .verifyComplete();
        verify(quotationRepository, times(1)).findByCustomerEmail(anyString());
    }

    @Test
    void getLeaseQuotationByCustomerEmail_NotFound() {
        String customerEmail = "nonexistent@example.com";
        when(quotationRepository.findByCustomerEmail(anyString())).thenReturn(Flux.empty());

        StepVerifier.create(leaseService.getLeaseQuotationByCustomerEmail(customerEmail))
                .expectError(DataNotFoundException.class)
                .verify();

        verify(quotationRepository, times(1)).findByCustomerEmail(anyString());
    }

    @Test
    void getLeaseContractById_Successful() {
        LeaseContracts leaseContract = new LeaseContracts();
        when(contractsRepository.findById(anyInt())).thenReturn(Mono.just(leaseContract));

        StepVerifier.create(leaseService.getLeaseContractById(1))
                .expectNext(leaseContract)
                .verifyComplete();

        verify(contractsRepository, times(1)).findById(anyInt());
    }

    @Test
    void getLeaseContractById_NotFound() {
        when(contractsRepository.findById(anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(leaseService.getLeaseContractById(1))
                .expectError(DataNotFoundException.class)
                .verify();

        verify(contractsRepository, times(1)).findById(anyInt());
    }
}
