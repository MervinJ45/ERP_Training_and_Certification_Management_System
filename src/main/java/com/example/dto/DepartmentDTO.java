package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {
    private Long departmentId;
    private String departmentName;
    private BigDecimal annualBudget;
    private BigDecimal availableBalance;
    private Boolean isActive;
}