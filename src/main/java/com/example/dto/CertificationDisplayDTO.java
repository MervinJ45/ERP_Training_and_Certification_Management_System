package com.example.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationDisplayDTO {

    private Long certificationId;
    private String certificateNumber;
    private String courseName;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Long daysRemaining; // Dynamically calculated time delta
    private String statusName;
    private String certificateUrl;
}