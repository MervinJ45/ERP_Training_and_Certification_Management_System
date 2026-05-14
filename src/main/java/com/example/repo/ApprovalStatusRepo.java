package com.example.repo;

import com.example.entity.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApprovalStatusRepo extends JpaRepository<ApprovalStatus, UUID> {

    Optional<ApprovalStatus> findByApprovalStatus(String approved);
}
