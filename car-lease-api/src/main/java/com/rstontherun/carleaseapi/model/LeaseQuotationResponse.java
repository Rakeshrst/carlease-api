package com.rstontherun.carleaseapi.model;

import com.rstontherun.carleaseapi.data.Car;
import com.rstontherun.carleaseapi.data.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaseQuotationResponse {
    private Customer customer;
    private Car car;
    private Integer quotationId;
    private String customerEmail;
    private Integer carId;
    private double mileage;
    private LocalDate expectedStartDate;
    private int duration;
    private double interestRate;
    private double nettPrice;
    private double monthlyLeaseRate;
    private boolean contract;
    private String quotationBy;
    private String contractedBy;
    private LocalDateTime quotationCreateTime;
}
