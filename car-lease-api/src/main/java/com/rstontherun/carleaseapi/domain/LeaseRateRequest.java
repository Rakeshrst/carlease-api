package com.rstontherun.carleaseapi.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaseRateRequest {
    private String customerEmail;
    private Integer carId;
    private double mileage;
    private int duration;
    private double interestRate;
    private double nettPrice;
    private String loggedBy;
}
