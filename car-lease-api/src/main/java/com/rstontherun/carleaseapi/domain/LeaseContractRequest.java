package com.rstontherun.carleaseapi.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaseContractRequest {
    private Integer quotationId;
    private String customerEmail;
    private Integer carId;
    private double mileage;
    private LocalDate expectedStartDate;
    private int duration;
    private double interestRate;
    private double nettPrice;
    private double monthlyLeaseRate;
}
