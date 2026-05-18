package com.example.service;

import com.example.dto.TrainingEnrollmentDTO;
import com.example.entity.*;
import com.example.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.cert.Certificate;
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
    private final SkillMatrixRepo skillMatrixRepo;
    private final CertificationStatusRepo certificationStatusRepo;
    private final CertificationRepo certificationRepo;
    private final AuditLogService auditLogService;

    public TrainingEnrollmentService(TrainingEnrollmentRepo trainingEnrollmentRepo, EmployeeRepo employeeRepo, TrainingCourseRepo courseRepo, EnrollmentStatusRepo statusRepo, ApprovalStatusRepo approvalStatusRepo, TrainingApprovalRepo trainingApprovalRepo, ApprovalWorkflowConfigRepo approvalWorkflowConfigRepo, AuditLogService auditLogService, SkillMatrixRepo skillMatrixRepo, CertificationStatusRepo certificationStatusRepo, CertificationRepo certificationRepo) {
        this.trainingEnrollmentRepo = trainingEnrollmentRepo;
        this.employeeRepo = employeeRepo;
        this.courseRepo = courseRepo;
        this.statusRepo = statusRepo;
        this.approvalStatusRepo = approvalStatusRepo;
        this.trainingApprovalRepo = trainingApprovalRepo;
        this.approvalWorkflowConfigRepo = approvalWorkflowConfigRepo;
        this.auditLogService = auditLogService;
        this.skillMatrixRepo = skillMatrixRepo;
        this.certificationStatusRepo = certificationStatusRepo;
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
        return trainingEnrollmentRepo.findByEmployeeManagerEmployeeIdAndCurrentApprovalLevelGreaterThan(managerId, 0).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<TrainingEnrollmentDTO> getPendingManagerApprovals(Long managerId) {

        List<TrainingEnrollment> allPendingRequests = trainingEnrollmentRepo.findByEnrollmentStatusEnrollmentStatusAndIsActiveTrue("Pending Approval");
        List<ApprovalWorkflowConfig> activeWorkflowRules = approvalWorkflowConfigRepo.findByIsActiveTrue();

        return allPendingRequests.stream().filter(enrollment -> enrollment.getEmployee() != null && enrollment.getEmployee().getManager() != null).filter(enrollment -> enrollment.getEmployee().getManager().getEmployeeId().equals(managerId)).filter(enrollment -> {
            // CHANGED: Direct match look-up instead of look-ahead math (+1)
            int currentLevel = enrollment.getCurrentApprovalLevel();
            BigDecimal cost = enrollment.getRequestedCost();

            ApprovalWorkflowConfig matchingRule = activeWorkflowRules.stream().filter(rule -> rule.getRequiredLevel() == currentLevel).filter(rule -> cost.compareTo(rule.getMinCost()) >= 0 && cost.compareTo(rule.getMaxCost()) <= 0).findFirst().orElse(null);

            // Fixed primitive comparison to safe object evaluation (.equals)
            return matchingRule != null && matchingRule.getApproverRole().getRoleId().equals(4L);
        }).map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void approveEnrollment(Long enrollmentId, Long approverId, BigDecimal approvedCost, String comments) {

        TrainingEnrollment enrollment = trainingEnrollmentRepo.findById(enrollmentId).orElseThrow(() -> new IllegalArgumentException("Enrollment record not found ID: " + enrollmentId));

        List<ApprovalWorkflowConfig> activeWorkflowRules = approvalWorkflowConfigRepo.findByIsActiveTrue();

        // 1. The current step that is being approved right now
        int currentActiveLevel = enrollment.getCurrentApprovalLevel();
        BigDecimal cost = enrollment.getRequestedCost();

        // 2. Look ahead: Is there a HIGHER approval level required after this one?
        int subsequentLevel = currentActiveLevel + 1;
        boolean hasMoreLevels = activeWorkflowRules.stream().anyMatch(rule -> rule.getRequiredLevel() == subsequentLevel && cost.compareTo(rule.getMinCost()) >= 0 && cost.compareTo(rule.getMaxCost()) <= 0);

        enrollment.setApprovedCost(approvedCost);

        if (!hasMoreLevels) {
            // No higher tiers exist -> Request is FULLY APPROVED
            // FIX: Keep the level at currentActiveLevel so it reflects the last acting rule step
            EnrollmentStatus approvedStatus = statusRepo.findByEnrollmentStatus("Approved").orElseThrow(() -> new IllegalStateException("Status 'Approved' not initialized in database."));
            enrollment.setEnrollmentStatus(approvedStatus);
        } else {
            // A higher tier exists (e.g., moving from Manager to HR, or HR to Director)
            enrollment.setCurrentApprovalLevel(subsequentLevel);
        }

        trainingEnrollmentRepo.save(enrollment);

        // 3. Save to Audit Trail
        TrainingApproval trainingApproval = new TrainingApproval();
        trainingApproval.setEnrollment(enrollment);

        Employee approver = new Employee();
        approver.setEmployeeId(approverId);
        trainingApproval.setApprover(approver);

        // Logs the exact step that was handled during this action
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
        // 1. Fetch the target enrollment record
        TrainingEnrollment enrollment = trainingEnrollmentRepo.findById(enrollmentId).orElseThrow(() -> new IllegalArgumentException("Enrollment record not found ID: " + enrollmentId));

        // 2. Terminate the process immediately
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
    public void completeEnrollment(Long enrollmentId, String remarks, Integer rating) {
        LocalDateTime now = LocalDateTime.now();

        TrainingEnrollment enrollment = trainingEnrollmentRepo.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment record not found ID: " + enrollmentId));

        Employee student = enrollment.getEmployee();
        TrainingCourse course = enrollment.getCourse();

        boolean isCertProvided = course.getCertificationProvided() != null && course.getCertificationProvided();
        String targetStatusName = isCertProvided ? "Certified" : "Completed";

        EnrollmentStatus finalStatus = statusRepo.findByEnrollmentStatus(targetStatusName)
                .orElseThrow(() -> new IllegalStateException("Status '" + targetStatusName + "' not initialized in database."));

        enrollment.setEnrollmentStatus(finalStatus);
        enrollment.setCompletionDate(now);
        enrollment.setCertificateIssued(true);
        enrollment.setRemarks(remarks);
        enrollment.setUpdatedAt(now);
        trainingEnrollmentRepo.save(enrollment);

        CertificationStatus activeCertStatus = certificationStatusRepo.findByCertificationStatus("Active")
                .orElse(null);

        Certification certificate = new Certification();
        certificate.setEmployee(student);
        certificate.setCourse(course);
        certificate.setEnrollment(enrollment);

        String uniqueSerial = "CE" + enrollmentId + student.getEmployeeId();
        if(uniqueSerial.length() > 10) {
            uniqueSerial = uniqueSerial.substring(0, 10);
        }
        certificate.setCertificateNumber(uniqueSerial);

        certificate.setIssueDate(now);
        certificate.setExpiryDate(now.plusYears(2));
        certificate.setStatus(activeCertStatus);
        certificate.setIssuedBy(course.getTrainer());
        certificate.setRemarks(remarks);
        certificate.setCreatedAt(now);
        certificate.setUpdatedAt(now);
        certificate.setIsActive(true);

        certificationRepo.save(certificate);

        SkillMatrix skillMatrix = new SkillMatrix();

        skillMatrix.setEmployee(student);
        skillMatrix.setCourse(course);
        skillMatrix.setCreatedAt(now);
        skillMatrix.setSkillName(course.getCourseName());
        skillMatrix.setProficiencyRating(rating);
        skillMatrix.setUpdatedAt(now);
        skillMatrix.setIsActive(true);

        skillMatrixRepo.save(skillMatrix);

        auditLogService.logAudit(enrollmentId, "UPDATE", "training_enrollments",
                String.format(" Generated Certification %s, Injected Skill Proficiency Rating: %d",
                        uniqueSerial, rating));
    }

    public List<TrainingEnrollmentDTO> getTrainerSpecificEnrollments(Long trainerId) {

        List<TrainingEnrollment> allEnrollments = trainingEnrollmentRepo.findAll();

        return allEnrollments.stream()
                .filter(enrollment -> enrollment.getIsActive() != null && enrollment.getIsActive())
                .filter(enrollment -> enrollment.getEnrollmentStatus() != null
                        && "Approved".equals(enrollment.getEnrollmentStatus().getEnrollmentStatus()))
                .filter(enrollment -> enrollment.getCourse() != null
                        && enrollment.getCourse().getTrainer() != null
                        && enrollment.getCourse().getTrainer().getEmployeeId().equals(trainerId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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