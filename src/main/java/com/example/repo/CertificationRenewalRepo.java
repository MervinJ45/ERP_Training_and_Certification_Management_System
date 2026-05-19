package com.example.repo;

import com.example.entity.ApprovalStatus;
import com.example.entity.CertificationRenewal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CertificationRenewalRepo extends JpaRepository<CertificationRenewal, Long> {

    List<CertificationRenewal> findByApprovalStatus_approvalStatusId(Long statusId);
}