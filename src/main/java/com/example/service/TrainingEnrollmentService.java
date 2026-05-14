package com.example.service;

import com.example.dto.TrainingEnrollmentDTO;
import com.example.entity.TrainingEnrollment;
import com.example.repo.EmployeeRepo;
import com.example.repo.TrainingCourseRepo;
import com.example.repo.EnrollmentStatusRepo;
import com.example.repo.TrainingEnrollmentRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public TrainingEnrollmentService(TrainingEnrollmentRepo trainingEnrollmentRepo, EmployeeRepo employeeRepo, TrainingCourseRepo courseRepo, EnrollmentStatusRepo statusRepo) {
        this.trainingEnrollmentRepo = trainingEnrollmentRepo;
        this.employeeRepo = employeeRepo;
        this.courseRepo = courseRepo;
        this.statusRepo = statusRepo;
    }

    public List<TrainingEnrollmentDTO> getAllEnrollmentDTOs() {
        return trainingEnrollmentRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public TrainingEnrollmentDTO getEnrollmentDTOById(UUID id) {
        return trainingEnrollmentRepo.findById(id).map(this::convertToDTO).orElse(null);
    }

    @Transactional
    public TrainingEnrollmentDTO saveEnrollment(TrainingEnrollmentDTO dto) {
        TrainingEnrollment enrollment;

        if (dto.getEnrollmentId() != null) {
            enrollment = trainingEnrollmentRepo.findById(dto.getEnrollmentId()).orElseThrow(() -> new RuntimeException("Enrollment not found"));
        } else {
            enrollment = new TrainingEnrollment();
            enrollment.setCreatedAt(LocalDateTime.now());
        }

        enrollment.setEnrollmentDate(dto.getEnrollmentDate());
        enrollment.setRemarks(dto.getRemarks());
        enrollment.setRequestedCost(dto.getRequestedCost());
        enrollment.setApprovedCost(dto.getApprovedCost());
        enrollment.setCurrentApprovalLevel(dto.getCurrentApprovalLevel());
        enrollment.setCompletionDate(dto.getCompletionDate());
        enrollment.setCertificateIssued(dto.getCertificateIssued());

        if (dto.getEmployeeId() != null) {
            enrollment.setEmployee(employeeRepo.findById(dto.getEmployeeId()).orElseThrow(() -> new RuntimeException("Employee not found")));
        }

        if (dto.getCourseId() != null) {
            enrollment.setCourse(courseRepo.findById(dto.getCourseId()).orElseThrow(() -> new RuntimeException("Course not found")));
        }

        if (dto.getEnrollmentStatusId() != null) {
            enrollment.setEnrollmentStatus(statusRepo.findById(dto.getEnrollmentStatusId()).orElseThrow(() -> new RuntimeException("Status not found")));
        }

        TrainingEnrollment saved = trainingEnrollmentRepo.save(enrollment);
        return convertToDTO(saved);
    }

    public void deleteEnrollment(UUID id) {
        trainingEnrollmentRepo.deleteById(id);
    }

    public TrainingEnrollmentDTO convertToDTO(TrainingEnrollment entity) {
        if (entity == null) return null;

        return TrainingEnrollmentDTO.builder().enrollmentId(entity.getEnrollmentId()).enrollmentDate(entity.getEnrollmentDate()).remarks(entity.getRemarks()).requestedCost(entity.getRequestedCost()).approvedCost(entity.getApprovedCost()).currentApprovalLevel(entity.getCurrentApprovalLevel()).completionDate(entity.getCompletionDate()).certificateIssued(entity.getCertificateIssued()).createdAt(entity.getCreatedAt())
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getEmployeeId() : null).employeeFullName(entity.getEmployee() != null ? entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName() : "N/A")
                .courseId(entity.getCourse() != null ? entity.getCourse().getCourseId() : null).courseName(entity.getCourse() != null ? entity.getCourse().getCourseName() : "N/A")
                .enrollmentStatusId(entity.getEnrollmentStatus() != null ? entity.getEnrollmentStatus().getEnrollmentStatusId() : null).enrollmentStatusName(entity.getEnrollmentStatus() != null ? entity.getEnrollmentStatus().getEnrollmentStatus() : "PENDING").build();
    }
}