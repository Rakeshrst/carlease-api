package com.rstontherun.carleaseapi.service;

import com.rstontherun.carleaseapi.data.LeaseContracts;
import com.rstontherun.carleaseapi.data.LeaseQuotation;
import com.rstontherun.carleaseapi.domain.LeaseContractRequest;
import com.rstontherun.carleaseapi.domain.LeaseRateRequest;
import com.rstontherun.carleaseapi.exception.DataNotFoundException;
import com.rstontherun.carleaseapi.repository.LeaseContractsRepository;
import com.rstontherun.carleaseapi.repository.LeaseQuotationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeaseService {

    private final LeaseQuotationRepository leaseQuotationRepository;
    private final LeaseContractsRepository leaseContractsRepository;


    public Mono<LeaseQuotation> createLeaseQuotation(LeaseRateRequest leaseRateRequest) {
        return leaseQuotationRepository.save(buildLeaseQuotation(leaseRateRequest));
    }

    private static LeaseQuotation buildLeaseQuotation(LeaseRateRequest leaseRateRequest) {
        return LeaseQuotation.builder()
                .customerEmail(leaseRateRequest.getCustomerEmail())
                .carId(leaseRateRequest.getCarId())
                .mileage(leaseRateRequest.getMileage())
                .expectedStartDate(LocalDate.from(LocalDate.now().atStartOfDay()))
                .duration(leaseRateRequest.getDuration())
                .interestRate(leaseRateRequest.getInterestRate())
                .nettPrice(leaseRateRequest.getNettPrice())
                .monthlyLeaseRate(getLeaseRate(leaseRateRequest))
                .contract(false)
                .quotationBy(leaseRateRequest.getLoggedBy())
                .quotationCreateTime(LocalDateTime.now())
                .build();
    }

    private static double getLeaseRate(LeaseRateRequest leaseRateRequest) {

        return ((leaseRateRequest.getMileage() / 12) * leaseRateRequest.getDuration() / leaseRateRequest.getNettPrice())
                + ((leaseRateRequest.getInterestRate() / 100) * leaseRateRequest.getNettPrice()) / 12;
    }

    public Mono<LeaseContracts> confirmContract(LeaseContractRequest contractRequest, String contractedBy) {
        return leaseQuotationRepository.findById(contractRequest.getQuotationId())
                .flatMap(quotation -> {
                    quotation.setContract(true);
                    quotation.setContractedBy(contractedBy);
                    return leaseQuotationRepository.save(quotation)
                            .flatMap(savedQuotation ->
                                    leaseContractsRepository.save(buildLeaseContract(savedQuotation, contractedBy)));
                });
    }

    private LeaseContracts buildLeaseContract(LeaseQuotation quotation, String contractedBy) {
        return LeaseContracts.builder()
                .customerEmail(quotation.getCustomerEmail())
                .carId(quotation.getCarId())
                .quotationId(quotation.getQuotationId())
                .mileage(quotation.getMileage())
                .startDate(quotation.getExpectedStartDate())
                .endDate(calculateEndDate(quotation.getExpectedStartDate(), quotation.getDuration()))
                .duration(quotation.getDuration())
                .interestRate(quotation.getInterestRate())
                .nettPrice(quotation.getNettPrice())
                .monthlyLeaseRate(quotation.getMonthlyLeaseRate())
                .active(true)
                .contractedBy(contractedBy)
                .contractConfirmationTime(LocalDateTime.now())
                .build();
    }

    private LocalDate calculateEndDate(LocalDate startDate, int duration) {
        long millisecondsInDay = 24L * 60 * 60 * 1000;
        long totalMilliseconds = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                + duration * millisecondsInDay;

        Instant instant = Instant.ofEpochMilli(totalMilliseconds);
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public Mono<LeaseContracts> endLease(Integer contractId, String contractedBy) {
        return leaseContractsRepository.findById(contractId)
                .flatMap(leaseContract -> {
                    if (leaseContract.isActive()) {
                        leaseContract.setActive(false);
                        leaseContract.setContractConfirmationTime(LocalDate.now().atStartOfDay());
                        leaseContract.setEndDate(LocalDate.now());
                        leaseContract.setContractedBy(contractedBy);
                        return leaseContractsRepository.save(leaseContract);
                    } else {
                        return Mono.empty();
                    }
                });
    }

    public Mono<LeaseQuotation> getLeaseQuotationById(Integer quotationId) {
        return leaseQuotationRepository.findById(quotationId)
                .switchIfEmpty(Mono.error(new DataNotFoundException("Lease Quotation not Found")));
    }

    public Flux<LeaseQuotation> getLeaseQuotationByCustomerEmail(String customerEmail) {
        return leaseQuotationRepository.findByCustomerEmail(customerEmail)
                .switchIfEmpty(Flux.error(new DataNotFoundException("Lease Quotation not Found")));
    }

    public Flux<LeaseContracts> getLeaseContractByCustomerEmail(String customerEmail) {
        return leaseContractsRepository.findByCustomerEmail(customerEmail)
                .switchIfEmpty(Flux.error(new DataNotFoundException("Lease Contract not Found")));
    }


    public Mono<LeaseContracts> getLeaseContractById(Integer contractId) {
        return leaseContractsRepository.findById(contractId)
                .switchIfEmpty(Mono.error(new DataNotFoundException("Lease Contract not Found")));
    }
}

