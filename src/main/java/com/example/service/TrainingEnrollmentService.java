package com.example.service;

import com.example.dto.TrainingEnrollmentDTO;
import com.example.entity.TrainingApproval;
import com.example.entity.TrainingEnrollment;
import com.example.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingEnrollmentService {

    private final TrainingEnrollmentRepo trainingEnrollmentRepo;
    private final EmployeeRepo employeeRepo;
    private final TrainingCourseRepo courseRepo;
    private final EnrollmentStatusRepo statusRepo;
    private final ApprovalStatusRepo approvalStatusRepo;
    private final TrainingApprovalRepo trainingApprovalRepo;
    private final ApprovalWorkflowConfigRepo approvalWorkflowConfigRepo;
    private final AuditLogService auditLogService; // Added

    public TrainingEnrollmentService(TrainingEnrollmentRepo trainingEnrollmentRepo,
                                     EmployeeRepo employeeRepo,
                                     TrainingCourseRepo courseRepo,
                                     EnrollmentStatusRepo statusRepo,
                                     ApprovalStatusRepo approvalStatusRepo,
                                     TrainingApprovalRepo trainingApprovalRepo,
                                     ApprovalWorkflowConfigRepo approvalWorkflowConfigRepo,
                                     AuditLogService auditLogService) { // Added
        this.trainingEnrollmentRepo = trainingEnrollmentRepo;
        this.employeeRepo = employeeRepo;
        this.courseRepo = courseRepo;
        this.statusRepo = statusRepo;
        this.approvalStatusRepo = approvalStatusRepo;
        this.trainingApprovalRepo = trainingApprovalRepo;
        this.approvalWorkflowConfigRepo = approvalWorkflowConfigRepo;
        this.auditLogService = auditLogService;
    }

    public List<TrainingEnrollmentDTO> getAllEnrollmentDTOs() {
        return trainingEnrollmentRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public TrainingEnrollmentDTO getEnrollmentDTOById(Long id) {
        return trainingEnrollmentRepo.findById(id).map(this::convertToDTO).orElse(null);
    }

    @Transactional
    public void enrollEmployee(Long employeeId, Long courseId, Double requestedCost) {
        boolean alreadyEnrolled = trainingEnrollmentRepo.existsByEmployeeEmployeeIdAndCourseCourseId(employeeId, courseId);

        if (alreadyEnrolled) {
            throw new RuntimeException("Already enrolled in this course");
        }

        TrainingEnrollment enrollment = new TrainingEnrollment();
        enrollment.setEmployee(employeeRepo.findById(employeeId).orElseThrow(() -> new RuntimeException("Employee not found")));
        enrollment.setCourse(courseRepo.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found")));
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setRequestedCost(BigDecimal.valueOf(requestedCost));
        enrollment.setApprovedCost(null);
        enrollment.setCertificateIssued(false);
        enrollment.setCurrentApprovalLevel(0);
        enrollment.setCreatedAt(LocalDateTime.now());
        enrollment.setEnrollmentStatus(statusRepo.findByEnrollmentStatus("PENDING").orElseThrow(() -> new RuntimeException("Default status not found")));

        TrainingEnrollment saved = trainingEnrollmentRepo.save(enrollment);

        // Logic to enter audit log
        auditLogService.logAudit(
                saved.getEnrollmentId(),
                "CREATE_ENROLLMENT",
                "TRAINING_ENROLLMENTS",
                String.format("Employee: %s, Course: %s, Requested Cost: %s",
                        saved.getEmployee().getFirstName() + " " + saved.getEmployee().getLastName(),
                        saved.getCourse().getCourseName(),
                        requestedCost)
        );
    }

    @Transactional
    public void deleteEnrollment(Long id) {
        trainingEnrollmentRepo.findById(id).ifPresent(e -> {
            auditLogService.logAudit(
                    id,
                    "DELETE_ENROLLMENT",
                    "TRAINING_ENROLLMENTS",
                    "Deleted enrollment for " + e.getEmployee().getFirstName() + " in " + e.getCourse().getCourseName()
            );
        });
        trainingEnrollmentRepo.deleteById(id);
    }

    @Transactional
    public void approveEnrollment(Long enrollmentId, Long approverId, BigDecimal approvedCost, String comments) {
        TrainingEnrollment enrollment = trainingEnrollmentRepo.findById(enrollmentId).orElseThrow(() -> new RuntimeException("Enrollment not found"));

        int nextLevel = enrollment.getCurrentApprovalLevel() + 1;

        TrainingApproval approval = new TrainingApproval();
        approval.setEnrollment(enrollment);
        approval.setApprover(employeeRepo.findById(approverId).orElseThrow(() -> new RuntimeException("Approver not found")));
        approval.setApprovalLevel(nextLevel);
        approval.setApprovalStatus(approvalStatusRepo.findByApprovalStatus("APPROVED").orElseThrow(() -> new RuntimeException("Approval status not found")));
        approval.setComments(comments);
        approval.setActionDate(LocalDateTime.now());

        trainingApprovalRepo.save(approval);

        boolean nextLevelExists = approvalWorkflowConfigRepo.existsByRequiredLevel(nextLevel + 1);

        if (!nextLevelExists) {
            enrollment.setApprovedCost(approvedCost);
            enrollment.setEnrollmentStatus(statusRepo.findByEnrollmentStatus("APPROVED").orElseThrow(() -> new RuntimeException("Enrollment status not found")));

            // Logic to enter audit log (Final Approval)
            auditLogService.logAudit(
                    enrollment.getEnrollmentId(),
                    "FINAL_APPROVAL",
                    "TRAINING_ENROLLMENTS",
                    "Enrollment fully approved. Approved Cost: " + approvedCost
            );
        } else {
            enrollment.setCurrentApprovalLevel(nextLevel);

            // Logic to enter audit log (Level Approval)
            auditLogService.logAudit(
                    enrollment.getEnrollmentId(),
                    "LEVEL_APPROVAL",
                    "TRAINING_ENROLLMENTS",
                    "Approved level " + nextLevel + " by " + approval.getApprover().getFirstName()
            );
        }

        trainingEnrollmentRepo.save(enrollment);
    }

    // Standard DTO and Search methods below...

    public TrainingEnrollmentDTO convertToDTO(TrainingEnrollment entity) {
        if (entity == null) return null;
        return TrainingEnrollmentDTO.builder()
                .enrollmentId(entity.getEnrollmentId())
                .enrollmentDate(entity.getEnrollmentDate())
                .remarks(entity.getRemarks())
                .requestedCost(entity.getRequestedCost())
                .approvedCost(entity.getApprovedCost())
                .currentApprovalLevel(entity.getCurrentApprovalLevel())
                .completionDate(entity.getCompletionDate())
                .certificateIssued(entity.getCertificateIssued())
                .createdAt(entity.getCreatedAt())
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getEmployeeId() : null)
                .employeeFullName(entity.getEmployee() != null ? entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName() : "N/A")
                .courseId(entity.getCourse() != null ? entity.getCourse().getCourseId() : null)
                .courseName(entity.getCourse() != null ? entity.getCourse().getCourseName() : "N/A")
                .enrollmentStatusId(entity.getEnrollmentStatus() != null ? entity.getEnrollmentStatus().getEnrollmentStatusId() : null)
                .enrollmentStatusName(entity.getEnrollmentStatus() != null ? entity.getEnrollmentStatus().getEnrollmentStatus() : "PENDING")
                .build();
    }

    public List<TrainingEnrollmentDTO> getEnrollmentsByEmployee(Long employeeId) {
        return trainingEnrollmentRepo.findByEmployeeEmployeeId(employeeId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<TrainingEnrollmentDTO> getPendingManagerApprovals(Long managerId) {
        return trainingEnrollmentRepo.findByEmployeeManagerEmployeeIdAndCurrentApprovalLevelLessThanAndEnrollmentStatusEnrollmentStatus(managerId, 1, "PENDING").stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<TrainingEnrollmentDTO> getEnrollmentsApprovedByManager(Long managerId) {
        return trainingEnrollmentRepo.findByEmployeeManagerEmployeeIdAndCurrentApprovalLevelGreaterThan(managerId, 0).stream().map(this::convertToDTO).collect(Collectors.toList());
    }
}