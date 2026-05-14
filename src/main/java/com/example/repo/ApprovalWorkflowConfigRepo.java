package com.example.repo;

import com.example.entity.ApprovalWorkflowConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApprovalWorkflowConfigRepo extends JpaRepository<ApprovalWorkflowConfig, UUID> {

    boolean existsByRequiredLevel(int i);
}
