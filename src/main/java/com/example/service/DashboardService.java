package com.example.service;

import com.example.entity.Certification;
import com.example.entity.Employee;
import com.example.entity.TrainingApproval;
import com.example.entity.TrainingCourse;
import com.example.entity.TrainingEnrollment;
import com.example.repo.CertificationRepo;
import com.example.repo.DepartmentRepo;
import com.example.repo.EmployeeRepo;
import com.example.repo.TrainingApprovalRepo;
import com.example.repo.TrainingCourseRepo;
import com.example.repo.TrainingEnrollmentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeeRepo employeeRepo;
    private final DepartmentRepo departmentRepo;
    private final TrainingCourseRepo trainingCourseRepo;
    private final TrainingEnrollmentRepo trainingEnrollmentRepo;
    private final TrainingApprovalRepo trainingApprovalRepo;
    private final CertificationRepo certificationRepo;


    public long getTotalEmployees() {
        return employeeRepo.count();
    }

    public long getTotalDepartments() {
        return departmentRepo.count();
    }

    public long getTotalCourses() {
        return trainingCourseRepo.count();
    }

    public long getTotalEnrollments() {
        return trainingEnrollmentRepo.count();
    }

    public long getTotalCertifications() {
        return certificationRepo.count();
    }


    public List<TrainingApproval> getPendingApprovals() {
        return trainingApprovalRepo.findAll()
                .stream()
                .filter(a ->
                        a.getApprovalStatus() != null &&
                                a.getApprovalStatus().getApprovalStatus().equalsIgnoreCase("Pending")
                )
                .toList();
    }


    public List<Certification> getExpiringCertifications() {

        LocalDateTime next30Days = LocalDateTime.now().plusDays(30);

        return certificationRepo.findAll()
                .stream()
                .filter(c ->
                        c.getExpiryDate() != null &&
                                c.getExpiryDate().isBefore(next30Days)
                )
                .toList();
    }

    public BigDecimal getTotalTrainingCost() {

        return trainingEnrollmentRepo.findAll()
                .stream()
                .map(TrainingEnrollment::getApprovedCost)
                .filter(cost -> cost != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}