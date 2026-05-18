package com.example.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingEnrollmentDTO {

    private Long enrollmentId;

    private Long employeeId;
    private String employeeFullName;

    private Long courseId;
    private String courseName;

    private Long enrollmentStatusId;
    private String enrollmentStatusName;

    private LocalDateTime enrollmentDate;
    private String remarks;
    private BigDecimal requestedCost;
    private BigDecimal approvedCost;
    private Integer currentApprovalLevel;
    private LocalDateTime completionDate;
    private Boolean certificateIssued;
    private LocalDateTime createdAt;
}