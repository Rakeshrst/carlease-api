package com.rstontherun.carleaseapi.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("lease_contracts")
public class LeaseContracts {
    @Id
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
    private Integer quotationId;
    private LocalDateTime contractConfirmationTime;
}
