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
@Table("cars")
public class Car {
    @Id
    private Integer carId;
    private String make;
    private String model;
    private String version;
    private String carNumberPlate;
    private int numberOfDoors;
    private int co2Emission;
    private double grossPrice;
    private double nettPrice;
}
