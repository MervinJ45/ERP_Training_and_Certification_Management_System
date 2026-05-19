package com.example.repo;

import com.example.entity.ApprovalWorkflowConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApprovalWorkflowConfigRepo extends JpaRepository<ApprovalWorkflowConfig, Long> {
    List<ApprovalWorkflowConfig> findByIsActiveTrue();
}
