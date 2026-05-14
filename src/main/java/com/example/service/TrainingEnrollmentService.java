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
import java.util.UUID;
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

    public TrainingEnrollmentService(TrainingEnrollmentRepo trainingEnrollmentRepo, EmployeeRepo employeeRepo, TrainingCourseRepo courseRepo, EnrollmentStatusRepo statusRepo, ApprovalStatusRepo approvalStatusRepo, TrainingApprovalRepo trainingApprovalRepo, ApprovalWorkflowConfigRepo approvalWorkflowConfigRepo) {
        this.trainingEnrollmentRepo = trainingEnrollmentRepo;
        this.employeeRepo = employeeRepo;
        this.courseRepo = courseRepo;
        this.statusRepo = statusRepo;
        this.approvalStatusRepo = approvalStatusRepo;
        this.trainingApprovalRepo = trainingApprovalRepo;
        this.approvalWorkflowConfigRepo = approvalWorkflowConfigRepo;
    }

    public List<TrainingEnrollmentDTO> getAllEnrollmentDTOs() {
        return trainingEnrollmentRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public TrainingEnrollmentDTO getEnrollmentDTOById(UUID id) {
        return trainingEnrollmentRepo.findById(id).map(this::convertToDTO).orElse(null);
    }

    @Transactional
    public void enrollEmployee(UUID employeeId, UUID courseId, Double requestedCost) {

        boolean alreadyEnrolled = trainingEnrollmentRepo.existsByEmployeeEmployeeIdAndCourseCourseId(employeeId, courseId);

        if (alreadyEnrolled) {
            throw new RuntimeException("Already enrolled in this course");
        }

        TrainingEnrollment enrollment = new TrainingEnrollment();

        enrollment.setEmployee(employeeRepo.findById(employeeId).orElseThrow(() -> new RuntimeException("Employee not found")));
        enrollment.setCourse(courseRepo.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found")));
        enrollment.setEnrollmentDate(java.time.LocalDate.now());
        enrollment.setRequestedCost(java.math.BigDecimal.valueOf(requestedCost));
        enrollment.setApprovedCost(null);
        enrollment.setCertificateIssued(false);
        enrollment.setCurrentApprovalLevel(0);
        enrollment.setCreatedAt(LocalDateTime.now());

        enrollment.setEnrollmentStatus(statusRepo.findByEnrollmentStatus("PENDING").orElseThrow(() -> new RuntimeException("Default status not found")));

        trainingEnrollmentRepo.save(enrollment);
    }

    public void deleteEnrollment(UUID id) {
        trainingEnrollmentRepo.deleteById(id);
    }

    public TrainingEnrollmentDTO convertToDTO(TrainingEnrollment entity) {
        if (entity == null) return null;
        return TrainingEnrollmentDTO.builder().enrollmentId(entity.getEnrollmentId()).enrollmentDate(entity.getEnrollmentDate()).remarks(entity.getRemarks()).requestedCost(entity.getRequestedCost()).approvedCost(entity.getApprovedCost()).currentApprovalLevel(entity.getCurrentApprovalLevel()).completionDate(entity.getCompletionDate()).certificateIssued(entity.getCertificateIssued()).createdAt(entity.getCreatedAt()).employeeId(entity.getEmployee() != null ? entity.getEmployee().getEmployeeId() : null).employeeFullName(entity.getEmployee() != null ? entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName() : "N/A").courseId(entity.getCourse() != null ? entity.getCourse().getCourseId() : null).courseName(entity.getCourse() != null ? entity.getCourse().getCourseName() : "N/A").enrollmentStatusId(entity.getEnrollmentStatus() != null ? entity.getEnrollmentStatus().getEnrollmentStatusId() : null).enrollmentStatusName(entity.getEnrollmentStatus() != null ? entity.getEnrollmentStatus().getEnrollmentStatus() : "PENDING").build();
    }

    public List<TrainingEnrollmentDTO> getEnrollmentsByEmployee(UUID employeeId) {
        return trainingEnrollmentRepo.findByEmployeeEmployeeId(employeeId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<TrainingEnrollmentDTO> getPendingManagerApprovals(UUID managerId) {
        return trainingEnrollmentRepo.findByEmployeeManagerEmployeeIdAndCurrentApprovalLevelLessThanAndEnrollmentStatusEnrollmentStatus(managerId, 1, "PENDING").stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void approveEnrollment(UUID enrollmentId, UUID approverId, BigDecimal approvedCost, String comments) {

        TrainingEnrollment enrollment = trainingEnrollmentRepo.findById(enrollmentId).orElseThrow(() -> new RuntimeException("Enrollment not found"));
        enrollment.setApprovedCost(approvedCost);

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
        } else {
            enrollment.setCurrentApprovalLevel(nextLevel);
        }

        trainingEnrollmentRepo.save(enrollment);
    }

    //to do reject enrollment

    public List<TrainingEnrollmentDTO> getEnrollmentsApprovedByManager(UUID managerId) {
        return trainingEnrollmentRepo.findByEmployeeManagerEmployeeIdAndCurrentApprovalLevelGreaterThan(managerId, 0).stream().map(this::convertToDTO).collect(Collectors.toList());
    }
}