package com.example.repo;

import com.example.entity.TrainingApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingApprovalRepo extends JpaRepository<TrainingApproval, Long> {

    List<TrainingApproval> findByApprover_EmployeeIdAndIsActiveTrue(Long employeeId);
}