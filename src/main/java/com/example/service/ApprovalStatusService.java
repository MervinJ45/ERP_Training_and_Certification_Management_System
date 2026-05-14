package com.example.service;

import com.example.entity.ApprovalStatus;
import com.example.repo.ApprovalStatusRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ApprovalStatusService {

    private final ApprovalStatusRepo approvalStatusRepo;

    public ApprovalStatusService(
            ApprovalStatusRepo approvalStatusRepo
    ) {
        this.approvalStatusRepo = approvalStatusRepo;
    }

    public List<ApprovalStatus> getAllApprovalStatuses() {
        return approvalStatusRepo.findAll();
    }

    public ApprovalStatus saveApprovalStatus(
            ApprovalStatus approvalStatus
    ) {
        return approvalStatusRepo.save(approvalStatus);
    }

    public void deleteApprovalStatus(UUID id) {
        approvalStatusRepo.deleteById(id);
    }

    public ApprovalStatus getApprovalStatusById(UUID id) {
        return approvalStatusRepo.findById(id)
                .orElse(null);
    }
}