package com.example.service;

import com.example.dto.TrainingEnrollmentDTO;
import com.example.entity.*;
import com.example.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final SkillMatrixService skillMatrixService;
    private final CertificationService certificationService;
    private final CertificatePdfService certificatePdfService;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final CertificationRepo certificationRepo;
    private final AuditLogService auditLogService;


    public TrainingEnrollmentService(TrainingEnrollmentRepo trainingEnrollmentRepo, EmployeeRepo employeeRepo, TrainingCourseRepo courseRepo, EnrollmentStatusRepo statusRepo, ApprovalStatusRepo approvalStatusRepo, TrainingApprovalRepo trainingApprovalRepo, ApprovalWorkflowConfigRepo approvalWorkflowConfigRepo, AuditLogService auditLogService, SkillMatrixService skillMatrixService, CertificationService certificationService, CertificatePdfService certificatePdfService, CloudinaryStorageService cloudinaryStorageService, CertificationRepo certificationRepo) {
        this.trainingEnrollmentRepo = trainingEnrollmentRepo;
        this.employeeRepo = employeeRepo;
        this.courseRepo = courseRepo;
        this.statusRepo = statusRepo;
        this.approvalStatusRepo = approvalStatusRepo;
        this.trainingApprovalRepo = trainingApprovalRepo;
        this.approvalWorkflowConfigRepo = approvalWorkflowConfigRepo;
        this.auditLogService = auditLogService;
        this.skillMatrixService = skillMatrixService;
        this.certificationService = certificationService;
        this.certificatePdfService = certificatePdfService;
        this.cloudinaryStorageService = cloudinaryStorageService;
        this.certificationRepo = certificationRepo;
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
        enrollment.setIsActive(true);
        enrollment.setEnrollmentStatus(statusRepo.findByEnrollmentStatus("Pending Approval").orElseThrow(() -> new RuntimeException("Default status not found")));

        TrainingEnrollment saved = trainingEnrollmentRepo.save(enrollment);

        auditLogService.logAudit(saved.getEnrollmentId(), "INSERT", "training_enrollments", String.format("Employee: %s %s enrolled in Course: %s. Requested Cost: %s", saved.getEmployee().getFirstName(), saved.getEmployee().getLastName(), saved.getCourse().getCourseName(), requestedCost));
    }

    @Transactional
    public void deleteEnrollment(Long id) {
        trainingEnrollmentRepo.findById(id).ifPresent(e -> {
            String employeeName = e.getEmployee() != null ? e.getEmployee().getFirstName() + " " + e.getEmployee().getLastName() : "Unknown";
            String courseName = e.getCourse() != null ? e.getCourse().getCourseName() : "Unknown";

            auditLogService.logAudit(id, "DELETE", "training_enrollments", "Deleted enrollment for " + employeeName + " in course: " + courseName);

            trainingEnrollmentRepo.delete(e);
        });
    }

    public TrainingEnrollmentDTO convertToDTO(TrainingEnrollment entity) {
        if (entity == null) return null;
        return TrainingEnrollmentDTO.builder().enrollmentId(entity.getEnrollmentId()).enrollmentDate(entity.getEnrollmentDate()).remarks(entity.getRemarks()).requestedCost(entity.getRequestedCost()).approvedCost(entity.getApprovedCost()).currentApprovalLevel(entity.getCurrentApprovalLevel()).completionDate(entity.getCompletionDate()).certificateIssued(entity.getCertificateIssued()).createdAt(entity.getCreatedAt()).employeeId(entity.getEmployee() != null ? entity.getEmployee().getEmployeeId() : null).employeeFullName(entity.getEmployee() != null ? entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName() : "N/A").courseId(entity.getCourse() != null ? entity.getCourse().getCourseId() : null).courseName(entity.getCourse() != null ? entity.getCourse().getCourseName() : "N/A").enrollmentStatusId(entity.getEnrollmentStatus() != null ? entity.getEnrollmentStatus().getEnrollmentStatusId() : null).enrollmentStatusName(entity.getEnrollmentStatus() != null ? entity.getEnrollmentStatus().getEnrollmentStatus() : "PENDING").build();
    }

    public List<TrainingEnrollmentDTO> getEnrollmentsByEmployee(Long employeeId) {
        return trainingEnrollmentRepo.findByEmployeeEmployeeId(employeeId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<TrainingEnrollmentDTO> getEnrollmentsApprovedByManager(Long managerId) {
        List<TrainingEnrollment> managerTeamEnrollments = trainingEnrollmentRepo.findByEmployeeManagerEmployeeId(managerId);

        return managerTeamEnrollments.stream().filter(enrollment -> enrollment.getCurrentApprovalLevel() >= 1 || (enrollment.getEnrollmentStatus() != null && "Approved".equals(enrollment.getEnrollmentStatus().getEnrollmentStatus()))).map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<TrainingEnrollmentDTO> getPendingManagerApprovals(Long managerId) {

        List<TrainingEnrollment> allPendingRequests = trainingEnrollmentRepo.findByEnrollmentStatusEnrollmentStatusAndIsActiveTrue("Pending Approval");
        List<ApprovalWorkflowConfig> activeWorkflowRules = approvalWorkflowConfigRepo.findByIsActiveTrue();

        return allPendingRequests.stream().filter(enrollment -> enrollment.getEmployee() != null && enrollment.getEmployee().getManager() != null).filter(enrollment -> enrollment.getEmployee().getManager().getEmployeeId().equals(managerId)).filter(enrollment -> {
            int currentLevel = enrollment.getCurrentApprovalLevel();
            BigDecimal cost = enrollment.getRequestedCost();

            ApprovalWorkflowConfig matchingRule = activeWorkflowRules.stream().filter(rule -> rule.getRequiredLevel() == currentLevel).filter(rule -> cost.compareTo(rule.getMinCost()) >= 0 && cost.compareTo(rule.getMaxCost()) <= 0).findFirst().orElse(null);

            return matchingRule != null && matchingRule.getApproverRole().getRoleId().equals(4L);
        }).map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void approveEnrollment(Long enrollmentId, Long approverId, BigDecimal approvedCost, String comments) {

        TrainingEnrollment enrollment = trainingEnrollmentRepo.findById(enrollmentId).orElseThrow(() -> new IllegalArgumentException("Enrollment record not found ID: " + enrollmentId));

        List<ApprovalWorkflowConfig> activeWorkflowRules = approvalWorkflowConfigRepo.findByIsActiveTrue();

        int currentActiveLevel = enrollment.getCurrentApprovalLevel();
        BigDecimal cost = enrollment.getRequestedCost();

        int subsequentLevel = currentActiveLevel + 1;
        boolean hasMoreLevels = activeWorkflowRules.stream().anyMatch(rule -> rule.getRequiredLevel() == subsequentLevel && cost.compareTo(rule.getMinCost()) >= 0 && cost.compareTo(rule.getMaxCost()) <= 0);

        enrollment.setApprovedCost(approvedCost);

        if (!hasMoreLevels) {
            EnrollmentStatus approvedStatus = statusRepo.findByEnrollmentStatus("Approved").orElseThrow(() -> new IllegalStateException("Status 'Approved' not initialized in database."));
            enrollment.setEnrollmentStatus(approvedStatus);
        } else {
            enrollment.setCurrentApprovalLevel(subsequentLevel);
        }

        trainingEnrollmentRepo.save(enrollment);

        TrainingApproval trainingApproval = new TrainingApproval();
        trainingApproval.setEnrollment(enrollment);

        Employee approver = new Employee();
        approver.setEmployeeId(approverId);
        trainingApproval.setApprover(approver);

        trainingApproval.setApprovalLevel(currentActiveLevel);
        trainingApproval.setApprovalStatus(approvalStatusRepo.findByApprovalStatus("Approved").orElse(null));
        trainingApproval.setComments(comments);

        LocalDateTime now = LocalDateTime.now();
        trainingApproval.setActionDate(now);
        trainingApproval.setCreatedAt(now);
        trainingApproval.setUpdatedAt(now);
        trainingApproval.setIsActive(true);

        trainingApprovalRepo.save(trainingApproval);
    }

    @Transactional
    public void rejectEnrollment(Long enrollmentId, Long approverId, String comments) {
        TrainingEnrollment enrollment = trainingEnrollmentRepo.findById(enrollmentId).orElseThrow(() -> new IllegalArgumentException("Enrollment record not found ID: " + enrollmentId));

        EnrollmentStatus rejectedStatus = statusRepo.findByEnrollmentStatus("Rejected").orElseThrow(() -> new IllegalStateException("Status 'REJECTED' not initialized in database."));

        enrollment.setEnrollmentStatus(rejectedStatus);
        trainingEnrollmentRepo.save(enrollment);

        TrainingApproval trainingApproval = new TrainingApproval();
        trainingApproval.setEnrollment(enrollment);

        Employee approver = new Employee();
        approver.setEmployeeId(approverId);
        trainingApproval.setApprover(approver);

        trainingApproval.setApprovalLevel(enrollment.getCurrentApprovalLevel());
        trainingApproval.setApprovalStatus(approvalStatusRepo.findByApprovalStatus("Rejected").orElse(null));
        trainingApproval.setComments(comments);

        LocalDateTime now = LocalDateTime.now();
        trainingApproval.setActionDate(now);
        trainingApproval.setCreatedAt(now);
        trainingApproval.setUpdatedAt(now);
        trainingApproval.setIsActive(true);

        trainingApprovalRepo.save(trainingApproval);
    }

    @Transactional
    public Certification completeEnrollment(Long enrollmentId, String remarks, Integer rating) {
        LocalDateTime now = LocalDateTime.now();

        TrainingEnrollment enrollment = trainingEnrollmentRepo.findById(enrollmentId).orElseThrow(() -> new IllegalArgumentException("Enrollment record not found ID: " + enrollmentId));

        Employee student = enrollment.getEmployee();
        TrainingCourse course = enrollment.getCourse();

        boolean isCertProvided = course.getCertificationProvided() != null && course.getCertificationProvided();
        String targetStatusName = isCertProvided ? "Certified" : "Completed";

        EnrollmentStatus finalStatus = statusRepo.findByEnrollmentStatus(targetStatusName).orElseThrow(() -> new IllegalStateException("Status '" + targetStatusName + "' not initialized in database."));

        enrollment.setEnrollmentStatus(finalStatus);
        enrollment.setCompletionDate(now);
        enrollment.setCertificateIssued(true);
        enrollment.setRemarks(remarks);
        enrollment.setUpdatedAt(now);
        trainingEnrollmentRepo.save(enrollment);

        Certification savedCertificate = certificationService.createCertification(enrollment, remarks, now);

        skillMatrixService.createSkillEntry(student, course, rating, now);

        auditLogService.logAudit(enrollmentId, "UPDATE", "training_enrollments", String.format("Generated Certification %s, Injected Skill Proficiency Rating: %d", savedCertificate.getCertificateNumber(), rating));

        return savedCertificate;
    }

    public Certification finalizeAndGenerateCertificate(Long enrollmentId, String remarks, Integer rating) {

        Certification certRecord = completeEnrollment(enrollmentId, remarks, rating);

        if (certRecord == null) {
            return null;
        }

        try {
            String studentName = certRecord.getEmployee().getFirstName() + " " + certRecord.getEmployee().getLastName();
            String courseName = certRecord.getCourse().getCourseName();
            String certNumber = certRecord.getCertificateNumber();
            String formattedDate = certRecord.getIssueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            byte[] pdfBytes = certificatePdfService.generateCertificatePdf(studentName, courseName, certNumber, formattedDate);

            String url = cloudinaryStorageService.uploadCertificate(pdfBytes, certNumber);

            certRecord.setCertificateUrl(url);
            certificationRepo.save(certRecord);

        } catch (Exception e) {
            System.err.println("Database states saved, but Cloud document generation pipelines encountered an error: " + e.getMessage());
        }

        return certRecord;
    }

    public List<TrainingEnrollmentDTO> getTrainerSpecificEnrollments(Long trainerId) {
        List<TrainingEnrollment> allEnrollments = trainingEnrollmentRepo.findAll();
        return allEnrollments.stream().filter(enrollment -> enrollment.getIsActive() != null && enrollment.getIsActive()).filter(enrollment -> enrollment.getEnrollmentStatus() != null && "Approved".equals(enrollment.getEnrollmentStatus().getEnrollmentStatus())).filter(enrollment -> enrollment.getCourse() != null && enrollment.getCourse().getTrainer() != null && enrollment.getCourse().getTrainer().getEmployeeId().equals(trainerId)).map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<TrainingEnrollmentDTO> getHrOrDirectorApprovals(Long roleId) {

        List<TrainingEnrollment> allPendingRequests = trainingEnrollmentRepo.findByEnrollmentStatusEnrollmentStatusAndIsActiveTrue("Pending Approval");
        List<ApprovalWorkflowConfig> activeWorkflowRules = approvalWorkflowConfigRepo.findByIsActiveTrue();

        return allPendingRequests.stream().filter(enrollment -> enrollment.getEmployee() != null).filter(enrollment -> {
            int currentLevel = enrollment.getCurrentApprovalLevel();
            BigDecimal cost = enrollment.getRequestedCost();

            ApprovalWorkflowConfig matchingRule = activeWorkflowRules.stream().filter(rule -> rule.getRequiredLevel() == currentLevel).filter(rule -> cost.compareTo(rule.getMinCost()) >= 0 && cost.compareTo(rule.getMaxCost()) <= 0).findFirst().orElse(null);

            return matchingRule != null && matchingRule.getApproverRole().getRoleId().equals(roleId);
        }).map(this::convertToDTO).collect(Collectors.toList());
    }


}