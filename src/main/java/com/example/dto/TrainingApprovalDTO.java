package com.example.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingApprovalDTO {

    private Long enrollmentId;
    private String courseName;
    private String employeeFullName;
    private Integer approvalLevel;
    private String approvalStatusName;
    private String comments;
    private LocalDateTime actionDate;
}