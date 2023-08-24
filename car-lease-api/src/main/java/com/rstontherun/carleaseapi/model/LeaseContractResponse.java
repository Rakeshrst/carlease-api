package com.rstontherun.carleaseapi.model;

import com.rstontherun.carleaseapi.data.Car;
import com.rstontherun.carleaseapi.data.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaseContractResponse {
    private Customer customer;
    private Car car;
    private Integer contractId;
    private String customerEmail;
    private Integer carId;
    private double mileage;
    private LocalDate startDate;
    private LocalDate endDate;
    private int duration;
    private double interestRate;
    private double nettPrice;
    private double monthlyLeaseRate;
    private boolean active;
    private String contractedBy;
    private LocalDateTime contractConfirmationTime;

}
