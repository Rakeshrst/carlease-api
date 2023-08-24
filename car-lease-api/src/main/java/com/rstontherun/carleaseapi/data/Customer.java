package com.rstontherun.carleaseapi.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("customers")
public class Customer {
    @Id
    private Integer customerId;
    private String name;
    private String street;
    private String houseNumber;
    private String zipCode;
    private String place;
    private String emailAddress;
    private String phoneNumber;
}