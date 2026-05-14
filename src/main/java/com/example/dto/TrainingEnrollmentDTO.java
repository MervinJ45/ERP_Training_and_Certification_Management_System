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

    private UUID enrollmentId;

    private UUID employeeId;
    private String employeeFullName;

    private UUID courseId;
    private String courseName;

    private UUID enrollmentStatusId;
    private String enrollmentStatusName;

    private LocalDate enrollmentDate;
    private String remarks;
    private BigDecimal requestedCost;
    private BigDecimal approvedCost;
    private Integer currentApprovalLevel;
    private LocalDate completionDate;
    private Boolean certificateIssued;
    private LocalDateTime createdAt;
}