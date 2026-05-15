package com.example.service;

import com.example.entity.ApprovalStatus;
import com.example.repo.ApprovalStatusRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApprovalStatusService {

    private final ApprovalStatusRepo approvalStatusRepo;

    public ApprovalStatusService(ApprovalStatusRepo approvalStatusRepo) {
        this.approvalStatusRepo = approvalStatusRepo;
    }

    public List<ApprovalStatus> getAllApprovalStatuses() {
        return approvalStatusRepo.findAll();
    }

    public ApprovalStatus saveApprovalStatus(ApprovalStatus approvalStatus) {
        return approvalStatusRepo.save(approvalStatus);
    }

    public void deleteApprovalStatus(Long id) {
        approvalStatusRepo.deleteById(id);
    }

    public ApprovalStatus getApprovalStatusById(Long id) {
        return approvalStatusRepo.findById(id).orElse(null);
    }
}