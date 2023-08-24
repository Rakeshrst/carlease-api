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
@Table("lease_quotations")
public class LeaseQuotation {
    @Id
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
